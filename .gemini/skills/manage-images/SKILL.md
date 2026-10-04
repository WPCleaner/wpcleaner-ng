---
name: manage-images
description: Manage project images in WPCleaner. Use when asked to download/import vector (SVG) images from Wikimedia Commons, convert originals to PNG according to ImageSize enum usages using Inkscape, update ImageCollection.java, or find and recover missing originals for existing project PNGs.
---

# Project Image Management

This skill handles image retrieval, format conversion, and asset auditing for `wpcleaner-lib-image`.

## Core Locations
- **Originals**: `wpcleaner-lib/wpcleaner-lib-image/src/main/resources/images/original/commons/`
- **Raster Output**: `wpcleaner-lib/wpcleaner-lib-image/src/main/resources/images/<size>/<mode>/commons/`
- **Enum Registry**: `wpcleaner-lib/wpcleaner-lib-image/src/main/java/org/wpcleaner/lib/image/ImageCollection.java`
- **Helper Script**: `.gemini/skills/manage-images/scripts/image_tool.py`

---

## Action 1: Fetch and Include Original Image

1. Run the helper to search Wikimedia Commons, download the SVG, normalize its name, and optionally register the enum:
   ```bash
   .gemini/skills/manage-images/scripts/image_tool.py fetch "<image-title-or-query>" [--enum <ENUM_NAME>]
   ```
2. If `--enum` was omitted, manually update `ImageCollection.java`:
   - Add `<ENUM_NAME>("commons/<normalized-name>.png"),` in alphabetical order.
   - Run `./gradlew :wpcleaner-lib:wpcleaner-lib-image:spotlessApply` to enforce formatting.

---

## Action 2: Convert Original to PNG by Target Usage

1. Convert an original SVG to the target PNG dimensions specified by `ImageSize`:
   ```bash
   .gemini/skills/manage-images/scripts/image_tool.py convert <image-name> --usage <BUTTON|LABEL|MENU|TOOLBAR|ICON>
   ```
2. The script:
   - Detects whether the original is square or non-square via SVG dimensions.
   - Targets the matching folder:
     - `BUTTON` / `LABEL`: `24px/square/` (24x24) or `24px/width/` (width 24)
     - `MENU` / `TOOLBAR`: `20px/square/` (20x20) or `20px/height/` (height 20)
     - `ICON`: `64px/square/` (64x64)
   - Executes Inkscape CLI to render the PNG.

---

## Action 3: Complete Missing Originals for Existing PNGs

1. Check which PNG files in `images/` lack a corresponding original in `original/commons/`:
   ```bash
   .gemini/skills/manage-images/scripts/image_tool.py audit
   ```
2. Automatically attempt to search Wikimedia Commons and download missing originals:
   ```bash
   .gemini/skills/manage-images/scripts/image_tool.py recover-missing [--dry-run]
   ```
