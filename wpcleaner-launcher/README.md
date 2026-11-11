# WPCleaner NG launcher

The launcher installs WPCleaner NG, keeps it up to date and starts it.
It embeds its own Java runtime, so nothing has to be installed beforehand.

## Installation

Download the archive for your system from the
[latest release](https://github.com/WPCleaner/wpcleaner-ng/releases/latest), extract it anywhere
in your user folders and start `WPCleanerNG`.

| System                | Archive                                      | Program to start              |
|-----------------------|----------------------------------------------|-------------------------------|
| Linux (x64)           | `wpcleaner-ng-launcher-linux-x64.tar.gz`     | `WPCleanerNG/bin/WPCleanerNG` |
| Linux (ARM 64)        | `wpcleaner-ng-launcher-linux-aarch64.tar.gz` | `WPCleanerNG/bin/WPCleanerNG` |
| Windows (x64)         | `wpcleaner-ng-launcher-windows-x64.zip`      | `WPCleanerNG\WPCleanerNG.exe` |
| macOS (Apple Silicon) | `wpcleaner-ng-launcher-macos-aarch64.zip`    | `WPCleanerNG.app`             |
| macOS (Intel)         | `wpcleaner-ng-launcher-macos-x64.zip`        | `WPCleanerNG.app`             |

On its first start, the launcher downloads WPCleaner NG and asks if you want a shortcut on your
desktop. Run it with `--create-shortcut` to create the shortcut later.

### Security warnings

The launcher is not signed with a paid developer certificate, so your system warns you the first
time you start it.

- **Windows**: in the *Windows protected your PC* window, click *More info*, then *Run anyway*.
- **macOS**: try to open `WPCleanerNG.app` once, then open *System Settings* > *Privacy & Security*
  and click *Open Anyway* next to the message about WPCleanerNG.
  You can also remove the quarantine flag from a terminal:
  `xattr -dr com.apple.quarantine WPCleanerNG.app`

## What the launcher does on each start

1. It reads `wpcleaner-ng.json` from the latest GitHub release.
2. If this release is newer than the installed one, it downloads the jar for your system,
   verifies its SHA-256 checksum and installs it. The previous version is kept.
3. It starts WPCleaner NG with Java, passing along the arguments it received.
   It uses its embedded Java runtime, or a Java installed on your system when a release requires
   a more recent Java.

Without network access, the launcher starts the installed version.
If a new version stops within a few seconds, the launcher offers to start the previous one and
will not install that version again.

## Files

Everything is stored in a folder of your user account:

| System  | Folder                                          |
|---------|-------------------------------------------------|
| Linux   | `~/.local/share/wpcleaner-ng`                   |
| Windows | `%LOCALAPPDATA%\WPCleanerNG`                    |
| macOS   | `~/Library/Application Support/WPCleanerNG`     |

- `versions/<version>/`: installed versions of WPCleaner NG
- `logs/launcher-0.log`: what the launcher did
- `logs/wpcleaner-ng.log`: output of WPCleaner NG
- `jvm-options.txt`: optional, one Java option per line (for example `-Xmx4g`), lines starting
  with `#` are ignored
- `launcher.json`: state of the launcher

To uninstall, delete this folder, the extracted launcher and the shortcut.

## Development

- Tests: `./gradlew :wpcleaner-launcher:test` (they run against a local HTTP server)
- Archive for the current system: `./gradlew :wpcleaner-launcher:packageLauncher`
  (result in `build/distributions`, application image in `build/jpackage/image`)
- System properties: `wpcleaner.launcher.home` replaces the folder above,
  `wpcleaner.launcher.releases` replaces `https://github.com/WPCleaner/wpcleaner-ng/releases`

The release workflow builds one jar and one launcher per system, then
`scripts/release_manifest.py` generates `wpcleaner-ng.json` from
`scripts/release_manifest_template.json` (required Java version, Java options, launcher
generation) and the checksums of the jars.
Increase `launcherGeneration` in the template and in `ReleaseUpdater` when a release needs a
newer launcher: older launchers then ask the user to download the new one.
