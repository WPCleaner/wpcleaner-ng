#!/usr/bin/env python3
# SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
# SPDX-License-Identifier: Apache-2.0
"""Read-only helpers for the WPCleanerNG documentation hosted on English Wikipedia.

  wiki_tool.py fetch <output_dir>       dump every Wikipedia:WPCleanerNG page as <output_dir>/<title>.wiki
  wiki_tool.py check-files <wiki_file>  report [[File:...]] links missing on Commons
"""

import json
import pathlib
import re
import sys
import urllib.parse
import urllib.request

USER_AGENT = "WPCleanerNG-docs/1.0 (https://github.com/WPCleaner/wpcleaner-ng)"
ENWIKI_API = "https://en.wikipedia.org/w/api.php"
COMMONS_API = "https://commons.wikimedia.org/w/api.php"
ROOT_PAGE = "WPCleanerNG"
PROJECT_NAMESPACE = 4
BATCH_SIZE = 50


def call_api(api_url, parameters):
    query = urllib.parse.urlencode({**parameters, "format": "json", "formatversion": 2})
    request = urllib.request.Request(f"{api_url}?{query}", headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request) as response:
        return json.load(response)


def fetch(output_dir):
    folder = pathlib.Path(output_dir)
    folder.mkdir(parents=True, exist_ok=True)
    result = call_api(
        ENWIKI_API,
        {
            "action": "query",
            "generator": "allpages",
            "gapnamespace": PROJECT_NAMESPACE,
            "gapprefix": ROOT_PAGE,
            "gaplimit": "max",
            "prop": "revisions",
            "rvprop": "content|timestamp",
            "rvslots": "main",
        },
    )
    for page in sorted(result["query"]["pages"], key=lambda p: p["title"]):
        revision = page["revisions"][0]
        file_name = page["title"].replace("/", "__").replace(":", "_") + ".wiki"
        (folder / file_name).write_text(revision["slots"]["main"]["content"], encoding="utf-8")
        print(f"{page['title']}\t{revision['timestamp']}\t{folder / file_name}")


def check_files(wiki_file):
    text = pathlib.Path(wiki_file).read_text(encoding="utf-8")
    titles = sorted({"File:" + name.strip() for name in re.findall(r"\[\[File:([^|\]]+)", text)})
    missing = []
    for start in range(0, len(titles), BATCH_SIZE):
        batch = titles[start : start + BATCH_SIZE]
        result = call_api(COMMONS_API, {"action": "query", "titles": "|".join(batch)})
        missing += [page["title"] for page in result["query"]["pages"] if page.get("missing")]
    print(f"{len(titles)} files checked, {len(missing)} missing")
    for title in missing:
        print(f"MISSING\t{title}")
    return 1 if missing else 0


def main(arguments):
    if len(arguments) == 2 and arguments[0] == "fetch":
        fetch(arguments[1])
        return 0
    if len(arguments) == 2 and arguments[0] == "check-files":
        return check_files(arguments[1])
    print(__doc__, file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
