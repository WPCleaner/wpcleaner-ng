#!/usr/bin/env python3
# SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
# SPDX-License-Identifier: Apache-2.0
"""Compare the NG bot output with legacy WPCleaner dump analysis and Check Wiki.

All commands take the work directory <work> (in the scratchpad) as first argument.
Run them in this order:

  init <work> <ng_output_dir> <wiki> <date>   record the run (wiki: frwiki|enwiki, date: 2026-11-01)
  fetch <work>                                legacy analysis pages (all revisions since the dump) + Check Wiki lists
  compare <work>                              overview table (markdown) + compare.json
  extract-dump <work> <content_dir>           texts of the compared pages from the *.xml.bz2 content files
  fetch-current <work> [sample]               current texts of differing pages (+ sample of unchecked NG pages)
  harness install|run|remove <work>           temporary apiTest analysing dump and current texts with NG
  classify <work> [algos] [examples] [filter] cause classes per algorithm, examples per class
  patterns <work> <algo> <cause_filter> [length]       most frequent reported texts in matching classes
  context <work> <title> <needle> [width] [--current]   text around a needle in the dump (or current) text
  try <work> <title> <text>                   add a hand-written text for the next harness run
"""

import bz2
import collections
import concurrent.futures
import glob
import html
import json
import os
import pathlib
import random
import re
import shutil
import subprocess
import sys
import time
import urllib.parse
import urllib.request

USER_AGENT = "WPCleanerNG-compare/1.0 (https://github.com/WPCleaner/wpcleaner-ng)"
CHECKWIKI = "https://checkwiki.toolforge.org/cgi-bin/checkwiki_bots.cgi"
CHECKWIKI_LAST_ALGORITHM = 500
WIKIS = {
    "frwiki": ("https://fr.wikipedia.org/w/api.php", "Projet:Correction syntaxique/Analyse {}", "FR"),
    "enwiki": ("https://en.wikipedia.org/w/api.php", "Wikipedia:CHECKWIKI/WPC {} dump", "EN"),
}
REPO = pathlib.Path(__file__).resolve().parents[4]
HARNESS_JAVA = REPO / "wpcleaner-lib/wpcleaner-lib-check/src/apiTest/java/org/wpcleaner/lib/check/comparison/HarnessAnalysisTest.java"
HARNESS_TEMPLATE = pathlib.Path(__file__).with_name("HarnessAnalysisTest.java.template")
LINE = re.compile(r"^\* \[\[:?(.*?)\]\]: (.*)$")
NOWIKI = re.compile(r"<nowiki>(.*?)</nowiki>")
TITLE = re.compile(r"<title>(.*?)</title>")


def load(work, name):
    return json.loads((pathlib.Path(work) / name).read_text(encoding="utf-8"))


def save(work, name, data):
    (pathlib.Path(work) / name).write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")


def jsonl(path):
    if not os.path.exists(path):
        return {}
    records = {}
    for line in open(path, encoding="utf-8"):
        record = json.loads(line)
        records[record["title"]] = record
    return records


def post(url, parameters, as_json=True):
    data = urllib.parse.urlencode(parameters).encode()
    for attempt in range(6):
        try:
            request = urllib.request.Request(url, data=data, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(request, timeout=120) as response:
                return json.load(response) if as_json else response.read().decode("utf-8")
        except OSError:
            time.sleep(5 * (attempt + 1))
    raise SystemExit(f"failed {url} {parameters}")


def api(work, parameters):
    config = load(work, "config.json")
    return post(WIKIS[config["wiki"]][0], {**parameters, "format": "json", "formatversion": 2})


def parse_list(text):
    result = {}
    for line in text.splitlines():
        match = LINE.match(line)
        if match:
            result[match.group(1).replace("_", " ")] = NOWIKI.findall(match.group(2))
    return result


def init(work, ng_dir, wiki, date):
    folder = pathlib.Path(work)
    folder.mkdir(parents=True, exist_ok=True)
    prefix = f"{wiki}-{date}-error-"
    files = sorted(glob.glob(f"{ng_dir}/{prefix}*.txt"))
    algos = [pathlib.Path(f).stem[len(prefix):] for f in files]
    run_start = min(os.path.getmtime(f) for f in files + [f"{ng_dir}/{wiki}-{date}-errors.jsonl"])
    config = {
        "wiki": wiki, "date": date, "ng_dir": str(pathlib.Path(ng_dir).resolve()), "algos": algos,
        "ng_run": time.strftime("%Y-%m-%dT%H:%M", time.gmtime(run_start - 3600)),
    }
    save(work, "config.json", config)
    print(json.dumps(config, indent=1))


def fetch(work):
    config = load(work, "config.json")
    folder = pathlib.Path(work)
    (folder / "legacy").mkdir(exist_ok=True)
    (folder / "cw").mkdir(exist_ok=True)
    marker = f"{config['wiki']}-{config['date'].replace('-', '')}"
    for algo in config["algos"]:
        title = WIKIS[config["wiki"]][1].format(algo)
        result = api(work, {
            "action": "query", "prop": "revisions", "titles": title, "rvprop": "ids|timestamp|user|content",
            "rvslots": "main", "rvlimit": 50, "rvend": config["date"] + "T00:00:00Z",
        })
        revisions = sorted(result["query"]["pages"][0].get("revisions", []), key=lambda r: r["timestamp"])
        kept = []
        for revision in revisions:
            content = revision["slots"]["main"]["content"]
            if marker not in content:
                continue
            begin = content.find("<!-- BOT BEGIN -->")
            end = content.find("<!-- BOT END -->")
            kept.append({"timestamp": revision["timestamp"], "revid": revision["revid"],
                         "pages": parse_list(content[begin:end])})
        save(work, f"legacy/{algo}.json", kept)
        cw_titles = None
        if int(algo) <= CHECKWIKI_LAST_ALGORITHM:
            checkwiki = post(CHECKWIKI, {"project": config["wiki"], "action": "list", "id": int(algo),
                                         "offset": 0, "limit": 100000}, as_json=False)
            cw_titles = sorted(line.strip()[6:].replace("_", " ") for line in checkwiki.splitlines()
                               if line.startswith("title="))
        save(work, f"cw/{algo}.json", cw_titles)
        print(algo, "legacy revisions:", [(r["timestamp"][:10], len(r["pages"])) for r in kept],
              "check wiki:", "unknown algorithm" if cw_titles is None else len(cw_titles))
        time.sleep(1)


def compare(work):
    config = load(work, "config.json")
    namespaces = {}
    for line in open(f"{config['ng_dir']}/{config['wiki']}-{config['date']}-errors.jsonl", encoding="utf-8"):
        record = json.loads(line)
        namespaces[record["title"]] = record["namespace"]
    data = {}
    print("| Algo | NG | Legacy | Both | NG only | Legacy only | Legacy first | Check Wiki | CW ∩ NG | CW only |")
    print("|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|")
    for algo in config["algos"]:
        ng = parse_list(pathlib.Path(f"{config['ng_dir']}/{config['wiki']}-{config['date']}-error-{algo}.txt")
                        .read_text(encoding="utf-8"))
        revisions = load(work, f"legacy/{algo}.json")
        legacy = revisions[-1]["pages"] if revisions else None
        first = revisions[0]["pages"] if revisions else None
        cw_titles = load(work, f"cw/{algo}.json")
        cw = set(cw_titles or [])
        data[algo] = {"ng": ng, "lg": legacy, "lg0": first, "cw": sorted(cw)}
        row = [algo, len(ng)]
        if legacy is None:
            row += ["–"] * 5
        else:
            row += [len(legacy), len(ng.keys() & legacy.keys()), len(ng.keys() - legacy.keys()),
                    len(legacy.keys() - ng.keys()), len(first)]
        if cw_titles is None:
            row += ["–"] * 3
        else:
            row += [len(cw), len(cw & ng.keys()), len(cw - ng.keys())]
        print("| " + " | ".join(str(v) for v in row) + " |")
    save(work, "compare.json", data)
    save(work, "namespaces.json", namespaces)
    print()
    for algo, values in data.items():
        by_ns = collections.Counter(namespaces.get(t, "?") for t in values["ng"])
        if len(by_ns) > 1:
            print(f"NG {algo} by namespace: {dict(sorted(by_ns.items(), key=str))}")
        if values["lg"] is not None:
            same = sum(1 for t in values["ng"] if t in values["lg"] and values["ng"][t] == values["lg"][t])
            print(f"{algo}: same reported text on {same} common pages")


def wanted_titles(work):
    titles = set()
    for values in load(work, "compare.json").values():
        titles |= set(values["ng"]) | set(values["lg"] or {}) | set(values["lg0"] or {}) | set(values["cw"])
    return titles


def extract_file(arguments):
    source, target, wanted = arguments
    count = 0
    with bz2.open(source, "rt", encoding="utf-8") as stream, open(target, "w", encoding="utf-8") as output:
        keep, buffer, meta = False, None, {}
        for line in stream:
            if buffer is not None:
                end = line.find("</text>")
                if end < 0:
                    buffer.append(line)
                    continue
                buffer.append(line[:end])
                meta["text"] = html.unescape("".join(buffer))
                output.write(json.dumps(meta, ensure_ascii=False) + "\n")
                count += 1
                keep, buffer = False, None
                continue
            stripped = line.lstrip()
            if stripped.startswith("<title>"):
                title = html.unescape(TITLE.search(stripped).group(1))
                keep, meta = title in wanted, {"title": title}
            elif keep and stripped.startswith("<ns>"):
                meta["ns"] = int(stripped[4:stripped.index("</ns>")])
            elif keep and stripped.startswith("<timestamp>"):
                meta["ts"] = stripped[11:31]
            elif keep and stripped.startswith("<text"):
                if stripped.rstrip().endswith("/>"):
                    meta["text"] = ""
                    output.write(json.dumps(meta, ensure_ascii=False) + "\n")
                    count += 1
                    keep = False
                    continue
                rest = stripped[stripped.index(">") + 1:]
                end = rest.find("</text>")
                if end >= 0:
                    meta["text"] = html.unescape(rest[:end])
                    output.write(json.dumps(meta, ensure_ascii=False) + "\n")
                    count += 1
                    keep = False
                else:
                    buffer = [rest]
    return count


def extract_dump(work, content_dir):
    wanted = wanted_titles(work)
    folder = pathlib.Path(work) / "dump"
    shutil.rmtree(folder, ignore_errors=True)
    folder.mkdir()
    sources = sorted(glob.glob(f"{content_dir}/*.xml.bz2"))
    tasks = [(source, str(folder / f"part{index}.jsonl"), wanted) for index, source in enumerate(sources)]
    with concurrent.futures.ProcessPoolExecutor() as executor:
        total = sum(executor.map(extract_file, tasks))
    print(f"{total} pages extracted out of {len(wanted)} wanted, from {len(sources)} files")


def dump_pages(work):
    pages = {}
    for path in glob.glob(f"{work}/dump/part*.jsonl"):
        pages.update(jsonl(path))
    return pages


def fetch_current(work, sample=60):
    data = load(work, "compare.json")
    namespaces = load(work, "namespaces.json")
    random.seed(1)
    wanted = set()
    for values in data.values():
        ng, legacy, cw = values["ng"], values["lg"], set(values["cw"])
        if legacy is not None:
            wanted |= set(ng) ^ set(legacy)
            wanted |= {t for t in ng if t in legacy and ng[t] != legacy[t]}
            wanted |= set(values["lg0"]) - set(ng)
        else:
            unchecked = sorted(t for t in ng if t not in cw)
            for namespace in sorted({namespaces.get(t) for t in unchecked}, key=str):
                in_namespace = [t for t in unchecked if namespaces.get(t) == namespace]
                wanted |= set(random.sample(in_namespace, min(sample, len(in_namespace))))
        wanted |= cw - set(ng)
    wanted = sorted(wanted)
    print(len(wanted), "pages to fetch", flush=True)
    with open(f"{work}/current.jsonl", "w", encoding="utf-8") as output:
        for start in range(0, len(wanted), 40):
            parameters = {"action": "query", "prop": "revisions", "rvprop": "timestamp|ids|content",
                          "rvslots": "main", "titles": "|".join(wanted[start:start + 40])}
            records = {}
            while True:
                result = api(work, parameters)
                normalized = {n["to"]: n["from"] for n in result["query"].get("normalized", [])}
                for page in result["query"]["pages"]:
                    title = normalized.get(page["title"], page["title"])
                    record = records.setdefault(title, {"title": title, "missing": page.get("missing", False),
                                                        "ns": page.get("ns")})
                    for revision in page.get("revisions", []):
                        record["ts"] = revision["timestamp"]
                        if "content" in revision["slots"]["main"]:
                            record["text"] = revision["slots"]["main"]["content"]
                if "continue" not in result:
                    break
                parameters.update(result["continue"])
            for record in records.values():
                output.write(json.dumps(record, ensure_ascii=False) + "\n")
            time.sleep(0.5)
    print("done")


def harness(action, work):
    folder = pathlib.Path(work).resolve() / "harness"
    if action == "remove":
        HARNESS_JAVA.unlink(missing_ok=True)
        print("removed", HARNESS_JAVA)
        return
    if action == "install":
        config = load(work, "config.json")
        template = HARNESS_TEMPLATE.read_text(encoding="utf-8")
        HARNESS_JAVA.write_text(template.replace("@HARNESS_DIR@", str(folder))
                                .replace("@WIKI@", WIKIS[config["wiki"]][2]), encoding="utf-8")
        print("installed", HARNESS_JAVA)
        return
    inputs = folder / "in"
    tried = sorted((folder / "try").glob("*.txt")) if (folder / "try").exists() else []
    shutil.rmtree(inputs, ignore_errors=True)
    inputs.mkdir(parents=True)
    index = []
    for kind, pages in (("current", jsonl(f"{work}/current.jsonl")), ("dump", dump_pages(work))):
        for number, (title, page) in enumerate(sorted(pages.items())):
            if page.get("text") is not None:
                name = f"{kind[0]}{number:06d}.txt"
                (inputs / name).write_text(title + "\n" + page["text"], encoding="utf-8")
                index.append((name, kind, title))
    for path in tried:
        shutil.copy(path, inputs / path.name)
        index.append((path.name, "try", path.read_text(encoding="utf-8").split("\n", 1)[0]))
    (folder / "index.json").write_text(json.dumps(index, ensure_ascii=False), encoding="utf-8")
    print(len(index), "texts; running the harness", flush=True)
    command = ["./gradlew", ":wpcleaner-lib-check:apiTest", "--rerun", "-q",
               "--tests", "org.wpcleaner.lib.check.comparison.HarnessAnalysisTest"]
    completed = subprocess.run(command, cwd=REPO, capture_output=True, text=True)
    print(completed.stdout[-3000:], completed.stderr[-3000:])
    output = folder / "out.tsv"
    print(output, sum(1 for _ in open(output, encoding="utf-8")) if output.exists() else "missing")
    for name, kind, title in index:
        if kind == "try":
            print("try", title, [line.rstrip("\n").split("\t", 1)[1] for line in open(output, encoding="utf-8")
                                 if line.startswith(name + "\t") and "WHITELISTED" not in line])


def try_text(work, title, text):
    folder = pathlib.Path(work) / "harness" / "try"
    folder.mkdir(parents=True, exist_ok=True)
    number = len(list(folder.glob("*.txt")))
    (folder / f"t{number:04d}.txt").write_text(title + "\n" + text, encoding="utf-8")


def unescape(text):
    return html.unescape(text.replace("↵", "\n").replace("[NBSP]", " "))


def harness_results(work):
    folder = pathlib.Path(work) / "harness"
    index = {name: (kind, title) for name, kind, title in json.loads((folder / "index.json").read_text(encoding="utf-8"))}
    results = collections.defaultdict(lambda: collections.defaultdict(list))
    for line in open(folder / "out.tsv", encoding="utf-8"):
        parts = line.rstrip("\n").split("\t")
        if parts[1] == "EXCEPTION":
            results[index[parts[0]]]["EXCEPTION"].append(parts[2])
        elif parts[2] == "WHITELISTED":
            results[index[parts[0]]][parts[1].zfill(3)].append("<<WHITELISTED>>")
        else:
            results[index[parts[0]]][parts[1].zfill(3)].append(parts[4] if len(parts) > 4 else "")
    return results, {key for key in index.values()}


def classify(work, algos=None, examples=3, text_filter=None):
    config = load(work, "config.json")
    data = load(work, "compare.json")
    namespaces = load(work, "namespaces.json")
    current = jsonl(f"{work}/current.jsonl")
    results, analysed = harness_results(work)
    selected = set(config["algos"])
    classes = {}
    for algo in (algos or config["algos"]):
        ng, legacy, first, cw = data[algo]["ng"], data[algo]["lg"], data[algo]["lg0"], set(data[algo]["cw"])
        found = collections.defaultdict(list)

        def other_side(title, reported, label):
            page = current.get(title, {})
            if page.get("missing") or "text" not in page:
                return found[f"{label} only: page deleted or moved"].append((title, reported))
            detections = [t for t in results[("current", title)][algo] if t != "<<WHITELISTED>>"]
            whitelisted = "<<WHITELISTED>>" in results[("current", title)][algo]
            in_dump = ("dump", title) in analysed
            any_in_dump = any(a in selected and v and a != "EXCEPTION" for a, v in results[("dump", title)].items())
            if detections:
                if page.get("ts", "") > config["ng_run"]:
                    cause = "NG detects on current text, page edited after NG run"
                elif not in_dump:
                    cause = "NG detects on current text, page absent from content file"
                elif not any_in_dump:
                    cause = "NG detects on current text, no error at all in content file (never re-checked)"
                else:
                    cause = "NG detects on current text, page not edited: UNEXPLAINED"
                reported = detections
            elif whitelisted:
                cause = "page whitelisted for NG"
            elif reported and all(unescape(x) in page["text"] for x in reported):
                cause = "reported text still in current page => NG DOES NOT DETECT"
            elif reported:
                cause = "reported text no longer in current page => fixed since"
            else:
                cause = "NG does not detect on current text"
            found[f"{label} only: {cause}"].append((title, reported))

        if legacy is not None:
            for title in legacy:
                if title not in ng:
                    other_side(title, legacy[title], "Legacy")
            for title in ng:
                if title in legacy:
                    continue
                if ("dump", title) not in analysed:
                    cause = "no content file text"
                elif title in first:
                    cause = "legacy listed it first, removed at a later re-check"
                elif not results[("dump", title)][algo]:
                    cause = "NG finds nothing in content file => error introduced after the dump"
                else:
                    cause = f"NG detects in content file, legacy never listed => LEGACY DOES NOT DETECT [ns{namespaces.get(title)}]"
                found[f"NG only: {cause}"].append((title, ng[title]))
            for title in ng:
                if title in legacy and ng[title] != legacy[title]:
                    found["Both: different reported text"].append((title, ng[title] + ["  ≠ LEGACY:"] + legacy[title]))
        else:
            for title in ng:
                if title not in cw and ("current", title) in analysed:
                    found[f"NG only (no legacy, sample): [ns{namespaces.get(title)}]"].append((title, ng[title]))
        for title in cw:
            if title not in ng:
                other_side(title, None, "Check Wiki")
        print(f"===== {algo}")
        for cause in sorted(found):
            if text_filter and text_filter not in cause:
                continue
            print(f"  {len(found[cause]):5d} {cause}")
            for title, reported in found[cause][:examples]:
                print("         ", title, "::", " ¦ ".join(x[:110] for x in (reported or [])[:4]))
        classes[algo] = {cause: [title for title, _ in pages] for cause, pages in found.items()}
    previous = load(work, "classes.json") if (pathlib.Path(work) / "classes.json").exists() else {}
    save(work, "classes.json", {**previous, **classes})


def text_patterns(work, algo, cause_filter, length=25):
    data = load(work, "compare.json")[algo]
    classes = load(work, "classes.json")[algo]
    counter = collections.Counter()
    for cause, titles in classes.items():
        if cause_filter in cause:
            for title in titles:
                for text in (data["ng"].get(title) or (data["lg"] or {}).get(title) or []):
                    counter[text[:length]] += 1
    for text, count in counter.most_common(40):
        print(f"{count:5d}  {text}")


def context(work, title, needle, width=150, use_current=False):
    page = jsonl(f"{work}/current.jsonl").get(title) if use_current else dump_pages(work).get(title)
    if not page or page.get("text") is None:
        print("no text for", title)
        return
    text = page["text"]
    print("ts", page.get("ts"), "ns", page.get("ns"), "length", len(text))
    position, shown = text.find(needle), 0
    while position >= 0 and shown < 3:
        print("---", position)
        print(repr(text[max(0, position - width): position + len(needle) + width]))
        position, shown = text.find(needle, position + 1), shown + 1


def main(arguments):
    command, rest = (arguments[0], arguments[1:]) if arguments else ("", [])
    flags = [a for a in rest if a.startswith("--")]
    rest = [a for a in rest if not a.startswith("--")]
    if command == "init" and len(rest) == 4:
        init(*rest)
    elif command == "fetch" and len(rest) == 1:
        fetch(rest[0])
    elif command == "compare" and len(rest) == 1:
        compare(rest[0])
    elif command == "extract-dump" and len(rest) == 2:
        extract_dump(*rest)
    elif command == "fetch-current" and len(rest) in (1, 2):
        fetch_current(rest[0], *(int(v) for v in rest[1:]))
    elif command == "harness" and len(rest) == 2 and rest[0] in ("install", "run", "remove"):
        harness(*rest)
    elif command == "classify" and 1 <= len(rest) <= 4:
        algos = [a.zfill(3) for a in rest[1].split(",")] if len(rest) > 1 and rest[1] != "all" else None
        classify(rest[0], algos, int(rest[2]) if len(rest) > 2 else 3, rest[3] if len(rest) > 3 else None)
    elif command == "patterns" and len(rest) in (3, 4):
        text_patterns(rest[0], rest[1].zfill(3), rest[2], *(int(v) for v in rest[3:]))
    elif command == "context" and len(rest) in (3, 4):
        context(rest[0], rest[1], rest[2], int(rest[3]) if len(rest) > 3 else 150, "--current" in flags)
    elif command == "try" and len(rest) == 3:
        try_text(*rest)
    else:
        print(__doc__, file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
