#!/usr/bin/env python3
import os
import sys
import re
import json
import time
import urllib.request
import urllib.parse
import urllib.error
import xml.etree.ElementTree as ET
import subprocess
from pathlib import Path

USER_AGENT = "WPCleanerImageTool/1.0 (https://github.com/WPCleaner/wpcleaner-ng)"
API_URL = "https://commons.wikimedia.org/w/api.php"

def get_workspace_root():
    current = Path.cwd()
    # Search upwards for settings.gradle.kts or GEMINI.md
    for p in [current, *current.parents]:
        if (p / "settings.gradle.kts").exists() or (p / "wpcleaner-lib").exists():
            return p
    print("Error: Could not find workspace root (looking for settings.gradle.kts)", file=sys.stderr)
    sys.exit(1)

WORKSPACE_ROOT = get_workspace_root()
LIB_IMAGE_DIR = WORKSPACE_ROOT / "wpcleaner-lib/wpcleaner-lib-image/src/main/resources/images"
ORIGINAL_COMMONS_DIR = LIB_IMAGE_DIR / "original/commons"
IMAGE_COLLECTION_FILE = WORKSPACE_ROOT / "wpcleaner-lib/wpcleaner-lib-image/src/main/java/org/wpcleaner/lib/image/ImageCollection.java"

USAGE_FOLDERS = {
    "BUTTON": ("24px/square/commons", "24px/width/commons"),
    "LABEL": ("24px/square/commons", "24px/width/commons"),
    "MENU": ("20px/square/commons", "20px/height/commons"),
    "TOOLBAR": ("20px/square/commons", "20px/height/commons"),
    "ICON": ("64px/square/commons", "64px/square/commons"),
}

def normalize_name(text):
    text = text.lower()
    text = re.sub(r'[^a-z0-9.-]', '-', text)
    text = re.sub(r'-+', '-', text)
    return text.strip('-')

def fetch_commons_api(url, params):
    query_str = urllib.parse.urlencode(params)
    full_url = f"{url}?{query_str}"
    req = urllib.request.Request(full_url, headers={"User-Agent": USER_AGENT})
    retries = 5
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(req) as resp:
                return json.loads(resp.read().decode('utf-8'))
        except urllib.error.HTTPError as e:
            if e.code == 429:
                delay = 2 ** attempt
                print(f"Rate limited. Retrying in {delay} seconds...", file=sys.stderr)
                time.sleep(delay)
            else:
                raise e
    raise Exception("Max retries exceeded for Wikimedia API")

def search_wikimedia_commons(query):
    # If the user passed a direct URL
    if query.startswith("http"):
        if "upload.wikimedia.org" in query:
            return None, query, None
        if "wiki/File:" in query:
            query = urllib.parse.unquote(query.split("wiki/")[-1])
            
    # Normalize title candidate
    title = query.strip()
    if not title.lower().startswith("file:"):
        title = "File:" + title
    if not any(title.lower().endswith(ext) for ext in [".svg", ".png", ".jpg", ".jpeg", ".gif"]):
        title += ".svg"

    # Try exact title
    data = fetch_commons_api(API_URL, {
        "action": "query",
        "titles": title,
        "prop": "imageinfo",
        "iiprop": "url|mime",
        "format": "json"
    })
    pages = data.get("query", {}).get("pages", {})
    for pid, pdata in pages.items():
        if pid != "-1" and "imageinfo" in pdata:
            return pdata["title"], pdata["imageinfo"][0]["url"], pdata["imageinfo"][0]["mime"]
            
    # Try searching
    data = fetch_commons_api(API_URL, {
        "action": "query",
        "list": "search",
        "srsearch": query,
        "srnamespace": "6",
        "format": "json"
    })
    results = data.get("query", {}).get("search", [])
    if results:
        best = next((r["title"] for r in results if r["title"].lower().endswith(".svg")), results[0]["title"])
        data2 = fetch_commons_api(API_URL, {
            "action": "query",
            "titles": best,
            "prop": "imageinfo",
            "iiprop": "url|mime",
            "format": "json"
        })
        pages2 = data2.get("query", {}).get("pages", {})
        for pid, pdata in pages2.items():
            if pid != "-1" and "imageinfo" in pdata:
                return pdata["title"], pdata["imageinfo"][0]["url"], pdata["imageinfo"][0]["mime"]
    return None, None, None

def download_image(url, dest_path):
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req) as resp, open(dest_path, 'wb') as f:
        f.write(resp.read())
    print(f"Downloaded: {dest_path}")

def update_image_collection(enum_name, png_rel_path):
    if not IMAGE_COLLECTION_FILE.exists():
        print(f"Error: {IMAGE_COLLECTION_FILE} not found", file=sys.stderr)
        return
        
    content = IMAGE_COLLECTION_FILE.read_text()
    pattern = r"(public enum ImageCollection \{\n)(.*?)(\n  private final String filename;)"
    match = re.search(pattern, content, re.DOTALL)
    if not match:
        print("Error: Could not parse ImageCollection.java", file=sys.stderr)
        return
        
    body = match.group(2)
    items = {}
    for line in body.split("\n"):
        line = line.strip()
        if not line:
            continue
        m = re.match(r"^([A-Z0-9_]+)\(\"([^\"]+)\"[\),;]+", line)
        if m:
            items[m.group(1)] = m.group(2)
            
    items[enum_name] = png_rel_path
    
    sorted_keys = sorted(items.keys())
    new_body_lines = []
    for i, k in enumerate(sorted_keys):
        delim = ";" if i == len(sorted_keys) - 1 else ","
        new_body_lines.append(f"  {k}(\"{items[k]}\"){delim}")
        
    new_body = "\n".join(new_body_lines)
    new_content = content[:match.start(2)] + new_body + content[match.end(2):]
    IMAGE_COLLECTION_FILE.write_text(new_content)
    print(f"Updated ImageCollection.java with {enum_name}(\"{png_rel_path}\")")

def get_svg_dimensions(filepath):
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()
        vb = root.attrib.get("viewBox")
        if vb:
            parts = vb.strip().replace(",", " ").split()
            if len(parts) == 4:
                return float(parts[2]), float(parts[3])
        w = root.attrib.get("width")
        h = root.attrib.get("height")
        if w and h:
            w_val = float(re.findall(r"[\d.]+", w)[0])
            h_val = float(re.findall(r"[\d.]+", h)[0])
            return w_val, h_val
    except Exception as e:
        print(f"Error parsing SVG {filepath}: {e}", file=sys.stderr)
    return None, None

def is_square(filepath):
    if str(filepath).endswith(".svg"):
        w, h = get_svg_dimensions(filepath)
        if w is not None and h is not None:
            return abs(w - h) < 0.1
    # Fallback for non-SVG or unparseable SVG
    try:
        out = subprocess.check_output(["identify", "-format", "%w %h", str(filepath)])
        w, h = map(float, out.decode().split())
        return abs(w - h) < 0.1
    except Exception as e:
        print(f"Error identifying {filepath}: {e}", file=sys.stderr)
        return True # Default to square if unknown

def cmd_fetch(query, enum_name=None):
    title, url, mime = search_wikimedia_commons(query)
    if not url:
        print("Error: Could not find image on Wikimedia Commons", file=sys.stderr)
        sys.exit(1)
        
    # Get original filename stem from url or title
    if title:
        original_name = title[5:] if title.startswith("File:") else title
    else:
        original_name = url.split("/")[-1]
        
    norm_name = normalize_name(original_name)
    if not norm_name.endswith(".svg") and url.split("?")[0].endswith(".svg"):
        norm_name += ".svg"
        
    ORIGINAL_COMMONS_DIR.mkdir(parents=True, exist_ok=True)
    dest_path = ORIGINAL_COMMONS_DIR / norm_name
    download_image(url, dest_path)
    
    if enum_name:
        stem = dest_path.stem
        png_rel_path = f"commons/{stem}.png"
        update_image_collection(enum_name, png_rel_path)

def cmd_convert(name_or_path, usage):
    if usage not in USAGE_FOLDERS:
        print(f"Error: Unknown usage '{usage}'. Valid: {list(USAGE_FOLDERS.keys())}", file=sys.stderr)
        sys.exit(1)
        
    p = Path(name_or_path)
    if not p.exists():
        # Try finding in original/commons
        p = ORIGINAL_COMMONS_DIR / name_or_path
        if not p.exists() and not name_or_path.endswith(".svg"):
            p = ORIGINAL_COMMONS_DIR / (name_or_path + ".svg")
            
    if not p.exists():
        print(f"Error: Could not find original image {name_or_path}", file=sys.stderr)
        sys.exit(1)
        
    square = is_square(p)
    target_folder_rel = USAGE_FOLDERS[usage][0] if square else USAGE_FOLDERS[usage][1]
    target_dir = LIB_IMAGE_DIR / target_folder_rel
    target_dir.mkdir(parents=True, exist_ok=True)
    
    target_png = target_dir / (p.stem + ".png")
    
    inkscape_cmd = ["inkscape"]
    if square:
        size = 24 if "24px" in target_folder_rel else 20 if "20px" in target_folder_rel else 64
        inkscape_cmd.extend(["-w", str(size), "-h", str(size)])
    else:
        if "width" in target_folder_rel:
            inkscape_cmd.extend(["-w", "24"])
        elif "height" in target_folder_rel:
            inkscape_cmd.extend(["-h", "20"])
        else:
            # ICON fallback
            inkscape_cmd.extend(["-w", "64", "-h", "64"])
            
    inkscape_cmd.extend(["-o", str(target_png), str(p)])
    
    print(f"Running: {' '.join(inkscape_cmd)}")
    subprocess.run(inkscape_cmd, check=True)
    print(f"Converted {p.name} -> {target_folder_rel}/{target_png.name}")

def cmd_audit():
    png_files = set()
    for root, _, files in os.walk(LIB_IMAGE_DIR):
        if "original" in root: continue
        for f in files:
            if f.endswith(".png"):
                png_files.add(f)
                
    original_stems = set()
    if ORIGINAL_COMMONS_DIR.exists():
        for f in ORIGINAL_COMMONS_DIR.iterdir():
            original_stems.add(f.stem)
            
    missing = []
    for p in sorted(png_files):
        stem = Path(p).stem
        if stem not in original_stems:
            missing.append(p)
            
    print(f"Total distinct PNG files: {len(png_files)}")
    print(f"Total originals: {len(original_stems)}")
    print(f"Missing originals ({len(missing)}):")
    for m in missing:
        print(f" - {m}")
        
    return missing

def cmd_recover(dry_run=False):
    missing = cmd_audit()
    if not missing:
        print("No missing originals found.")
        return
        
    print("\nAttempting recovery...")
    ORIGINAL_COMMONS_DIR.mkdir(parents=True, exist_ok=True)
    
    for m in missing:
        stem = Path(m).stem
        # Try exact first
        candidates = [f"File:{stem}.svg", f"File:{stem.capitalize()}.svg", f"File:{stem.title()}.svg"]
        if stem.startswith("gnome-"):
            candidates.append(f"File:Gnome-{stem[6:]}.svg")
            
        found_url = None
        for cand in candidates:
            title, url, mime = search_wikimedia_commons(cand)
            if url and url.split("?")[0].endswith(".svg"):
                found_url = url
                break
                
        if not found_url:
            query = stem.replace("-", " ")
            title, url, mime = search_wikimedia_commons(query)
            if url and url.split("?")[0].endswith(".svg"):
                found_url = url
                
        if found_url:
            dest = ORIGINAL_COMMONS_DIR / f"{stem}.svg"
            if dry_run:
                print(f"[DRY RUN] Would download {found_url} -> {dest.name}")
            else:
                download_image(found_url, dest)
                time.sleep(1.0) # respect rate limit
        else:
            print(f"Warning: Could not automatically find SVG for {stem}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <fetch|convert|audit|recover-missing> ...", file=sys.stderr)
        sys.exit(1)
        
    cmd = sys.argv[1]
    if cmd == "fetch":
        if len(sys.argv) < 3:
            print("Usage: fetch <query> [--enum <NAME>]", file=sys.stderr)
            sys.exit(1)
        query = sys.argv[2]
        enum_name = sys.argv[4] if len(sys.argv) >= 5 and sys.argv[3] == "--enum" else None
        cmd_fetch(query, enum_name)
    elif cmd == "convert":
        if len(sys.argv) < 5 or sys.argv[3] != "--usage":
            print("Usage: convert <name_or_path> --usage <BUTTON|LABEL|MENU|TOOLBAR|ICON>", file=sys.stderr)
            sys.exit(1)
        cmd_convert(sys.argv[2], sys.argv[4])
    elif cmd == "audit":
        cmd_audit()
    elif cmd == "recover-missing":
        dry_run = "--dry-run" in sys.argv
        cmd_recover(dry_run)
    else:
        print(f"Unknown command: {cmd}", file=sys.stderr)
        sys.exit(1)
