import argparse
import hashlib
import json
import os
import re
import sys

ASSET_PATTERN = re.compile(r"^wpcleaner-ng-(?P<platform>[a-z]+-[a-z0-9]+)\.jar$")

def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Generates the manifest read by the launcher to install a release"
    )
    parser.add_argument("--version", required=True, help="Version of the release, like 0.0.5")
    parser.add_argument("--assets", required=True, help="Folder holding the jars of the release")
    parser.add_argument("--output", required=True, help="Manifest file to generate")
    return parser.parse_args()

def compute_sha256(file_path):
    digest = hashlib.sha256()
    with open(file_path, "rb") as asset_file:
        for chunk in iter(lambda: asset_file.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()

def list_assets(assets_directory):
    assets = {}
    for file_name in sorted(os.listdir(assets_directory)):
        match = ASSET_PATTERN.match(file_name)
        if match:
            assets[match.group("platform")] = {
                "name": file_name,
                "sha256": compute_sha256(os.path.join(assets_directory, file_name)),
            }
    return assets

def main():
    arguments = parse_arguments()
    if not re.fullmatch(r"\d+(\.\d+)*", arguments.version):
        print(f"Error: Invalid version: {arguments.version}", file=sys.stderr)
        sys.exit(1)
    assets = list_assets(arguments.assets)
    if not assets:
        print(f"Error: No release jar found in {arguments.assets}", file=sys.stderr)
        sys.exit(1)
    script_directory = os.path.dirname(os.path.abspath(__file__))
    with open(os.path.join(script_directory, "release_manifest_template.json")) as template_file:
        manifest = json.load(template_file)
    manifest["version"] = arguments.version
    manifest["assets"] = assets
    with open(arguments.output, "w") as manifest_file:
        json.dump(manifest, manifest_file, indent=2)
        manifest_file.write("\n")
    print(f"Generated {arguments.output} for {', '.join(assets)}")

if __name__ == "__main__":
    main()
