# WPCleaner NG bot

The bot runs maintenance tasks on a wiki without any graphical interface.
It doesn't need JavaFX and the same jar runs on every system.

## Installation

Run the [launcher](../../wpcleaner-launcher/README.md) with `--install-bot`: it installs or
updates the bot, writes a start script and stops without starting anything.
Run it again to update the bot, or after moving the launcher.

| System  | Start script                                                        |
|---------|---------------------------------------------------------------------|
| Linux   | `~/.local/share/wpcleaner-ng/bot/wpcleaner-ng-bot.sh`               |
| Windows | `%LOCALAPPDATA%\WPCleanerNG\bot\wpcleaner-ng-bot.cmd`               |
| macOS   | `~/Library/Application Support/WPCleanerNG/bot/wpcleaner-ng-bot.sh` |

Java options can be given with the `JAVA_TOOL_OPTIONS` environment variable
(for example `-Xmx4g`).

## Usage

```
wpcleaner-ng-bot.sh --wiki=fr --script=check.yaml [--script=other.yaml...]
                    [--content-folder=<folder>] [--output-folder=<folder>]
```

- `--wiki` (required): code of the wiki, as in the list of wikis of WPCleaner NG (`fr` for the
  French Wikipedia). The bot logs in with the credentials configured for this wiki in
  `credentials.yaml` or `credentials.json` of the `.wpcleaner` folder of your home folder.
- `--script` (required, repeatable): YAML file describing what the bot does.
  Several scripts are run one after another.
- `--content-folder`: folder holding the
  [MediaWiki Content File Exports](https://wikitech.wikimedia.org/wiki/MediaWiki_Content_File_Exports)
  of `mediawiki_content_current`, organized by wiki and then by date
  (default `mediawiki_content_current` in the current folder).
- `--output-folder`: folder where the results are written (default `wpcleaner-bot-output` in
  the current folder).

The exit code is `0` on success, `1` on failure and `2` for invalid arguments or scripts.

API requests are limited to 3 per minute, or 10 per minute when the account is in the `bot` group.
Accounts with the `apihighlimits` right retrieve 500 pages per request instead of 50.

## Scripts

A script holds a list of `tasks`, each with a `type`:

```yaml
# Comments are allowed
tasks:
  - type: processContentCurrent
    namespaces: [0]          # optional, main namespace by default
    report:
      - 17                   # Check Wiki algorithm, in the namespaces of the task
      - algorithm: 48        # Check Wiki algorithm with its own namespaces
        namespaces: [0, 104]
```

### processContentCurrent

The bot streams the most recent `mediawiki_content_current` files of the wiki: in
`<content folder>/<wiki>/<date>`, the most recent date with a `SHA256SUMS` file, which lists the
files to read.

For each page, the bot runs the Check Wiki algorithms listed in `report`, even if they are
inactive on the wiki, but skips pages in their whitelists. When errors are detected, it retrieves
the current revision of the page and analyzes it again.

Results are written in the output folder, `frwiki-2026-11-01` being the wiki and date of the files:

- `frwiki-2026-11-01-errors.jsonl`: one line per page with errors still present, with the page
  id, its current title and the errors.
- `frwiki-2026-11-01-error-017.txt`: for each algorithm, the list of pages in wikitext, sorted by
  namespace and title, ready to be copied between the `BOT` comments of the Check Wiki pages.

## Development

- Tests: `./gradlew :wpcleaner-application-bot:test`
- Jar: `./gradlew :wpcleaner-application-bot:bootJar` (result in `build/libs`)
