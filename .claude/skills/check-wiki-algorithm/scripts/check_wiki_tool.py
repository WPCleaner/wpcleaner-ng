#!/usr/bin/env python3
# SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
# SPDX-License-Identifier: Apache-2.0
"""Read-only helpers to implement a Check Wiki algorithm in WPCleanerNG.

  check_wiki_tool.py legacy <number> [<legacy_src>]
      path of the legacy algorithm, and the parameters it declares
      (<legacy_src> defaults to ../wpcleaner/WikipediaCleaner/src, relative to the repository)

  check_wiki_tool.py config <number> <wiki>...
      Check Wiki settings of the error on each wiki, the WPCleanerNG/Configuration/NNN page,
      and a YAML draft for that page. <wiki> is a Check Wiki code like enwiki or frwiki
      (host <language>.wikipedia.org), or <code>=<host> for other wikis.
"""

import json
import pathlib
import re
import sys
import urllib.parse
import urllib.request

USER_AGENT = "WPCleanerNG-dev/0.1 (https://github.com/WPCleaner/wpcleaner-ng)"
GENERAL_CONFIGURATION_PAGE = "User:NicoV/WikiCleanerConfiguration"
TRANSLATION_PAGE_PARAMETER = "check_wiki_translation_page"
PARAMETERS_PAGE = "Project:WPCleanerNG/Configuration/{number:03d}"
COMMON_SETTINGS = ("prio", "head", "desc", "link", "whitelistpage", "whitelist", "bot")
YAML_SPECIAL_START = set("-?:,[]{}#&*!|>'\"%@`")
YAML_RESERVED = {"true", "false", "null", "~", ""}
REPOSITORY_ROOT = pathlib.Path(__file__).resolve().parents[4]
DEFAULT_LEGACY_SRC = REPOSITORY_ROOT.parent / "wpcleaner" / "WikipediaCleaner" / "src"
LEGACY_PARAMETER = re.compile(r'String\s+(PARAMETER_\w+)\s*=\s*"([^"]+)"')
LEGACY_DESCRIPTION = re.compile(r'new AlgorithmParameter\(\s*(PARAMETER_\w+),\s*GT\._T\("([^"]*)"')


def legacy_path(legacy_src, number):
    return (
        pathlib.Path(legacy_src)
        / "org/wikipediacleaner/api/check/algorithm"
        / f"a{number // 100}xx/a{number // 10:02d}x/a{number:03d}"
        / f"CheckErrorAlgorithm{number:03d}.java"
    )


def legacy(number, legacy_src):
    path = legacy_path(legacy_src, number)
    if not path.exists():
        print(f"Legacy algorithm not found: {path}")
        return 1
    source = path.read_text(encoding="utf-8")
    descriptions = dict(LEGACY_DESCRIPTION.findall(source))
    print(f"Legacy algorithm: {path}")
    parameters = LEGACY_PARAMETER.findall(source)
    if not parameters:
        print("No parameters")
    for constant, name in parameters:
        print(f"Parameter {name}: {descriptions.get(constant, '(no description)')}")
    return 0


def parse_wiki(argument):
    if "=" in argument:
        code, host = argument.split("=", 1)
        return code, host
    if not argument.endswith("wiki"):
        raise ValueError(f"Unknown wiki {argument}: use <code>=<host>")
    return argument, f"{argument[: -len('wiki')].replace('_', '-')}.wikipedia.org"


def read_page(host, title):
    query = urllib.parse.urlencode(
        {
            "action": "query",
            "prop": "revisions",
            "rvprop": "content",
            "rvslots": "main",
            "titles": title,
            "format": "json",
            "formatversion": 2,
        }
    )
    request = urllib.request.Request(
        f"https://{host}/w/api.php?{query}", headers={"User-Agent": USER_AGENT}
    )
    with urllib.request.urlopen(request) as response:
        page = json.load(response)["query"]["pages"][0]
    if page.get("missing") or not page.get("revisions"):
        return page["title"], None
    return page["title"], page["revisions"][0]["slots"]["main"]["content"]


def parse_properties(text):
    properties = {}
    lines = iter(text.splitlines())
    for line in lines:
        name, separator, value = line.partition("=")
        name = name.strip()
        if not separator or not name or not all(c.isalnum() or c in "_ " for c in name):
            continue
        if value != "END":
            while " END" not in value:
                next_line = next(lines, None)
                if next_line is None:
                    break
                value += "\n" + next_line
            value = value.split(" END", 1)[0]
        else:
            value = ""
        properties[name] = value.strip()
    return properties


def yaml_scalar(value, flow=False):
    if (
        value.lower() in YAML_RESERVED
        or (flow and any(character in value for character in ",[]{}"))
        or value[0] in YAML_SPECIAL_START
        or ": " in value
        or " #" in value
        or value != value.strip()
    ):
        return json.dumps(value, ensure_ascii=False)
    return value


def yaml_draft(parameters):
    lines = []
    for name, value in sorted(parameters.items()):
        value_lines = [line.strip() for line in value.splitlines() if line.strip()]
        if value.strip().lower() in ("true", "false"):
            lines.append(f"{name}: {value.strip().lower()}")
        elif name.endswith("_groups") and value_lines and all("|" in line for line in value_lines):
            lines.append(f"{name}:")
            for line in value_lines:
                group, *names = [element.strip() for element in re.split(r"(?<!\\)\|", line)]
                lines.append(f"  {yaml_scalar(group, True)}: [{', '.join(yaml_scalar(element, True) for element in names)}]")
        else:
            names = [re.split(r"(?<!\\)\|", line)[0].strip() for line in value_lines]
            names = [element for line in names for element in line.split(",") if element.strip()]
            names = [element.strip() for element in names]
            templates = [element for element in names if not element.startswith("+")]
            groups = [element[1:].strip() for element in names if element.startswith("+")]
            if templates:
                lines.append(f"{name}:")
                lines += [f"  - {yaml_scalar(element)}" for element in templates]
            if groups:
                groups_name = name.replace("_templates", "_groups")
                if groups_name == name:
                    lines.append(f"# TODO: groups found in {name}, choose a dedicated parameter")
                    groups_name = f"{name}_groups"
                lines.append(f"{groups_name}:")
                lines += [f"  - {yaml_scalar(element)}" for element in groups]
    return lines


def config(number, wikis):
    result = 0
    for argument in wikis:
        code, host = parse_wiki(argument)
        print(f"== {code} ({host})")
        _, general = read_page(host, GENERAL_CONFIGURATION_PAGE)
        translation_page = parse_properties(general or "").get(TRANSLATION_PAGE_PARAMETER)
        if not translation_page:
            print(f"No {TRANSLATION_PAGE_PARAMETER} in {GENERAL_CONFIGURATION_PAGE}")
            result = 1
            continue
        _, translation = read_page(host, translation_page)
        pattern = re.compile(rf"error_{number:03d}_(.+)_{re.escape(code)}")
        settings = {
            match.group(1): value
            for name, value in parse_properties(translation or "").items()
            if (match := pattern.fullmatch(name))
        }
        print(f"Translation page: {translation_page}")
        for name in COMMON_SETTINGS:
            if name in settings:
                value = settings[name].replace("\n", " ")
                print(f"  {name}: {value[:120]}{'...' if len(value) > 120 else ''}")
        parameters = {name: value for name, value in settings.items() if name not in COMMON_SETTINGS}
        if not parameters:
            print("  No parameters")
        for name, value in sorted(parameters.items()):
            print(f"  parameter {name}: {value!r}")
        title, content = read_page(host, PARAMETERS_PAGE.format(number=number))
        print(f"Configuration page: {title} ({'missing' if content is None else 'exists'})")
        if parameters:
            print('<syntaxhighlight lang="yaml">')
            print("\n".join(yaml_draft(parameters)))
            print("</syntaxhighlight>")
    return result


def main(arguments):
    if len(arguments) in (2, 3) and arguments[0] == "legacy" and arguments[1].isdigit():
        legacy_src = arguments[2] if len(arguments) == 3 else DEFAULT_LEGACY_SRC
        return legacy(int(arguments[1]), legacy_src)
    if len(arguments) >= 3 and arguments[0] == "config" and arguments[1].isdigit():
        return config(int(arguments[1]), arguments[2:])
    print(__doc__, file=sys.stderr)
    return 2


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
