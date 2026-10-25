---
name: manage-images
description: Manage project images in WPCleaner. Use when asked to download/import vector (SVG) images from Wikimedia Commons, convert originals to PNG according to ImageSize enum usages using Inkscape, update ImageCollection.java, or find and recover missing originals for existing project PNGs.
---

# Project image management

Image retrieval, format conversion and asset auditing for the `:wpcleaner-lib-image` module.

## Locations

- **Originals**: `wpcleaner-lib/wpcleaner-lib-image/src/main/resources/images/original/commons/`
- **Raster output**: `wpcleaner-lib/wpcleaner-lib-image/src/main/resources/images/<size>/<mode>/commons/`
- **Enum registry**: `wpcleaner-lib/wpcleaner-lib-image/src/main/java/org/wpcleaner/lib/image/ImageCollection.java`
- **Helper script**: `${CLAUDE_SKILL_DIR}/scripts/image_tool.py` (run from the repository root; requires `inkscape`)

## Fetch an original image

1. Search Wikimedia Commons, download the SVG with a normalized name, and optionally register the enum:
   ```bash
   python3 ${CLAUDE_SKILL_DIR}/scripts/image_tool.py fetch "<image-title-or-query>" [--enum <ENUM_NAME>]
   ```
2. If `--enum` was omitted, add `<ENUM_NAME>("commons/<normalized-name>.png"),` to `ImageCollection.java`
   in alphabetical order.
3. Run `./gradlew :wpcleaner-lib-image:spotlessApply`.
4. Tell the user which Commons file was downloaded (title and URL) so they can check the license/attribution.

## Convert an original to PNG for a usage

```bash
python3 ${CLAUDE_SKILL_DIR}/scripts/image_tool.py convert <image-name> --usage <BUTTON|LABEL|MENU|TOOLBAR|ICON>
```

The script detects whether the SVG is square and renders with Inkscape into:

| Usage              | Square                  | Non-square                      |
|--------------------|-------------------------|---------------------------------|
| `BUTTON` / `LABEL` | `24px/square/` (24x24)  | `24px/width/` (width 24)        |
| `MENU` / `TOOLBAR` | `20px/square/` (20x20)  | `20px/height/` (height 20)      |
| `ICON`             | `64px/square/` (64x64)  | `64px/square/` (64x64)          |

Check `ImageSize` usages in the code to pick the right usage when the user doesn't specify it.

## Recover missing originals

1. List PNGs without a matching original:
   ```bash
   python3 ${CLAUDE_SKILL_DIR}/scripts/image_tool.py audit
   ```
2. Try to find and download them from Commons — always do a dry run first and show the result to the user:
   ```bash
   python3 ${CLAUDE_SKILL_DIR}/scripts/image_tool.py recover-missing --dry-run
   python3 ${CLAUDE_SKILL_DIR}/scripts/image_tool.py recover-missing
   ```
   Matches come from a fuzzy search: verify that each recovered SVG really corresponds to the existing PNG.
