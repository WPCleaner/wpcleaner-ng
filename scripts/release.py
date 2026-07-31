import sys
import os
import subprocess
import re
import json
import urllib.request

def parse_dry_run_parameter():
    dry_run = True
    for argument in sys.argv[1:]:
        if argument.startswith("--dry-run="):
            boolean_string = argument.split("=", 1)[1].lower()
            if boolean_string in ("true", "1", "yes", "y"):
                dry_run = True
            elif boolean_string in ("false", "0", "no", "n"):
                dry_run = False
            else:
                print(f"Error: Invalid boolean value for --dry-run: {boolean_string}", file=sys.stderr)
                sys.exit(1)
        elif argument == "--dry-run":
            dry_run = True
    return dry_run

def update_local_repository():
    try:
        fetch_result = subprocess.run(
            ["git", "fetch", "--all", "--tags"],
            capture_output=True,
            text=True
        )
        if fetch_result.returncode != 0:
            print(f"Error: Failed to fetch from git.\n{fetch_result.stderr}", file=sys.stderr)
            sys.exit(1)
    except Exception as execution_error:
        print(f"Error: Failed to execute git fetch.\n{execution_error}", file=sys.stderr)
        sys.exit(1)

def verify_no_local_modifications(dry_run):
    try:
        status_result = subprocess.run(
            ["git", "status", "--porcelain"],
            capture_output=True,
            text=True
        )
        if status_result.returncode != 0:
            print(f"Error: Failed to run git status.\n{status_result.stderr}", file=sys.stderr)
            sys.exit(1)
        
        status_lines = status_result.stdout.strip().split('\n')
        actual_modifications = []
        for line in status_lines:
            if not line or line.startswith("AD "):
                continue
            stripped_line = line.strip()
            parts = stripped_line.split(None, 1)
            if len(parts) == 2:
                file_path = parts[1].lstrip('"')
                if file_path.startswith("releases/") or file_path.startswith("scripts/"):
                    continue
            actual_modifications.append(line)
        
        if actual_modifications:
            status_output = '\n'.join(actual_modifications)
            message = f"Local modifications detected in the repository:\n{status_output}"
            if dry_run:
                print(f"WARNING: {message}")
            else:
                print(f"Error: {message}", file=sys.stderr)
                sys.exit(1)
        else:
            print("No local modifications detected.")
    except Exception as execution_error:
        print(f"Error: Failed to check git status.\n{execution_error}", file=sys.stderr)
        sys.exit(1)

def extract_version_prefix():
    script_directory = os.path.dirname(os.path.abspath(__file__))
    gradle_properties_path = os.path.join(script_directory, "..", "gradle.properties")
    
    if not os.path.isfile(gradle_properties_path):
        print(f"Error: gradle.properties file not found at {gradle_properties_path}", file=sys.stderr)
        sys.exit(1)
        
    version_prefix = None
    try:
        with open(gradle_properties_path, "r", encoding="utf-8") as properties_file:
            for line in properties_file:
                stripped_line = line.strip()
                if stripped_line.startswith("version.prefix="):
                    version_prefix = stripped_line.split("=", 1)[1].strip()
                    break
    except Exception as read_error:
        print(f"Error: Failed to read gradle.properties.\n{read_error}", file=sys.stderr)
        sys.exit(1)
        
    if not version_prefix:
        print("Error: version.prefix not found in gradle.properties", file=sys.stderr)
        sys.exit(1)
        
    return version_prefix

def check_release_notes_file(version):
    script_directory = os.path.dirname(os.path.abspath(__file__))
    releases_directory = os.path.join(script_directory, "..", "releases")
    release_notes_path = os.path.join(releases_directory, f"v{version}.md")
    
    if not os.path.isfile(release_notes_path):
        template_path = os.path.join(releases_directory, "_template.md")
        if not os.path.isfile(template_path):
            print(f"Error: Template file not found at {template_path}", file=sys.stderr)
            sys.exit(1)
            
        try:
            with open(template_path, "r", encoding="utf-8") as template_file:
                template_content = template_file.read()
                
            new_content = template_content.replace("$version", version)
            
            with open(release_notes_path, "w", encoding="utf-8") as new_file:
                new_file.write(new_content)
                
            print(f"Created release notes file at {release_notes_path} from template.")
        except Exception as file_error:
            print(f"Error: Failed to create release notes file.\n{file_error}", file=sys.stderr)
            sys.exit(1)
        
    return release_notes_path

def get_dependabot_commits():
    try:
        tag_result = subprocess.run(
            ["git", "describe", "--tags", "--abbrev=0"],
            capture_output=True,
            text=True
        )
        if tag_result.returncode == 0:
            last_tag = tag_result.stdout.strip()
            log_range = f"{last_tag}..HEAD"
        else:
            log_range = "HEAD"
            
        log_result = subprocess.run(
            ["git", "log", log_range, "--format=%s|%an"],
            capture_output=True,
            text=True
        )
        if log_result.returncode != 0:
            print(f"Error: Failed to retrieve commits.\n{log_result.stderr}", file=sys.stderr)
            sys.exit(1)
            
        commits = []
        for line in log_result.stdout.strip().split('\n'):
            if not line:
                continue
            parts = line.split('|', 1)
            if len(parts) == 2:
                title, author = parts
                if "dependabot" in author.lower():
                    if ":" in title:
                        title_parts = title.split(":", 1)
                        if title_parts[0].lower().strip() in ["dependabot(deps)", "dependabot", "build(deps)", "chore(deps)"]:
                            title = title_parts[1].strip()
                    author_clean = author.replace("[bot]", "")
                    title = re.sub(r'\(#(\d+)\)$', r'([#\1](https://github.com/WPCleaner/wpcleaner-ng/pull/\1))', title.strip())
                    commits.append(f"* {title} (by {author_clean})")
        commits.sort()
        return commits
    except Exception as e:
        print(f"Error: Failed to get commits.\n{e}", file=sys.stderr)
        sys.exit(1)

def fill_dependency_upgrades(release_notes_path):
    dependabot_commits = get_dependabot_commits()
    
    try:
        with open(release_notes_path, "r", encoding="utf-8") as f:
            lines = f.read().split('\n')
            
        new_lines = []
        in_dependency_section = False
        
        for line in lines:
            if line.startswith("## :wrench: Dependency upgrades"):
                new_lines.append(line)
                in_dependency_section = True
                if dependabot_commits:
                    new_lines.extend(dependabot_commits)
                continue
                
            if in_dependency_section:
                if line.startswith("## "):
                    in_dependency_section = False
                    if dependabot_commits and new_lines and new_lines[-1] != "":
                        new_lines.append("")
                    new_lines.append(line)
            else:
                new_lines.append(line)
                
        with open(release_notes_path, "w", encoding="utf-8") as f:
            f.write('\n'.join(new_lines))
            
    except Exception as e:
        print(f"Error: Failed to update release notes.\n{e}", file=sys.stderr)
        sys.exit(1)

def get_contributors(release_notes_path=None):
    try:
        remote_result = subprocess.run(["git", "remote", "get-url", "origin"], capture_output=True, text=True)
        if remote_result.returncode != 0:
            print(f"Error: Failed to get remote url.\n{remote_result.stderr}", file=sys.stderr)
            sys.exit(1)
            
        remote_url = remote_result.stdout.strip()
        repo_path = re.sub(r'^.*github\.com[:/]', '', remote_url)
        repo_path = re.sub(r'\.git$', '', repo_path)
        
        tag_result = subprocess.run(
            ["git", "describe", "--tags", "--abbrev=0"],
            capture_output=True,
            text=True
        )
        if tag_result.returncode == 0:
            last_tag = tag_result.stdout.strip()
            log_range = f"{last_tag}..HEAD"
        else:
            log_range = "HEAD"
            
        log_result = subprocess.run(
            ["git", "log", log_range, "--format=%H|%an|%ae"],
            capture_output=True,
            text=True
        )
        if log_result.returncode != 0:
            print(f"Error: Failed to retrieve contributors.\n{log_result.stderr}", file=sys.stderr)
            sys.exit(1)
            
        email_to_commit = {}
        for line in log_result.stdout.strip().split('\n'):
            if not line:
                continue
            parts = line.split('|', 2)
            if len(parts) == 3:
                commit_hash, author_name, author_email = parts
                if "dependabot" in author_name.lower() or "dependabot" in author_email.lower():
                    continue
                if "github-actions" in author_name.lower() or "github-actions" in author_email.lower():
                    continue
                if author_email not in email_to_commit:
                    email_to_commit[author_email] = (commit_hash, author_name)
                    
        import urllib.parse
        existing_logins = set()
        if release_notes_path and os.path.isfile(release_notes_path):
            try:
                with open(release_notes_path, "r", encoding="utf-8") as f:
                    content = f.read()
                parts = content.split("## :smiling_imp: Contributors")
                if len(parts) > 1:
                    contributors_part = parts[1].split("## ")[0]
                    for line in contributors_part.split("\n"):
                        match = re.search(r'\*\s+\[([^\]]+)\]', line)
                        if match:
                            existing_logins.add(match.group(1).strip())
            except Exception:
                pass

        github_logins = set()
        headers = {'User-Agent': 'release-script'}
        github_token = os.environ.get("GITHUB_TOKEN") or os.environ.get("GITHUB_ACCESS_TOKEN")
        if github_token:
            headers['Authorization'] = f"token {github_token}"

        for author_email, (commit_hash, fallback_name) in email_to_commit.items():
            login = None

            api_url = f"https://api.github.com/repos/{repo_path}/commits/{commit_hash}"
            try:
                req = urllib.request.Request(api_url, headers=headers)
                with urllib.request.urlopen(req) as response:
                    data = json.loads(response.read().decode('utf-8'))
                    author_info = data.get("author")
                    if author_info and "login" in author_info:
                        login = author_info["login"]
            except Exception as e:
                print(f"Error: Failed to retrieve commits.\n{e}", file=sys.stderr)
                pass

            if not login:
                try:
                    email_url = f"https://api.github.com/repos/{repo_path}/commits?author={urllib.parse.quote(author_email)}"
                    req = urllib.request.Request(email_url, headers=headers)
                    with urllib.request.urlopen(req) as response:
                        commits_data = json.loads(response.read().decode('utf-8'))
                        if isinstance(commits_data, list) and len(commits_data) > 0:
                            first_commit = commits_data[0]
                            author_info = first_commit.get("author")
                            if author_info and "login" in author_info:
                                login = author_info["login"]
                except Exception as e:
                    print(f"Error: Failed to retrieve commits for author {author_email}.\n{e}", file=sys.stderr)
                    pass

            if not login:
                try:
                    search_url = f"https://api.github.com/search/users?q={urllib.parse.quote(author_email)}+in:email"
                    search_headers = headers.copy()
                    search_headers['Accept'] = 'application/vnd.github+json'
                    req = urllib.request.Request(search_url, headers=search_headers)
                    with urllib.request.urlopen(req) as response:
                        search_data = json.loads(response.read().decode('utf-8'))
                        items = search_data.get("items")
                        if items and len(items) > 0:
                            login = items[0].get("login")
                except Exception as e:
                    print(f"Error: Failed to retrieve information about user {author_email}.\n{e}", file=sys.stderr)
                    pass

            if not login and existing_logins:
                clean_name = fallback_name.lower().replace(" ", "")
                clean_email = author_email.lower()
                for existing in existing_logins:
                    clean_existing = existing.lower()
                    if clean_existing in clean_name or clean_existing in clean_email or clean_name in clean_existing:
                        login = existing
                        break
                    
                    name_parts = fallback_name.lower().split()
                    if len(name_parts) >= 2:
                        if clean_existing == name_parts[0][0] + name_parts[-1]:
                            login = existing
                            break

            if login:
                github_logins.add(login)
            else:
                github_logins.add(fallback_name)
                
        sorted_logins = sorted(list(github_logins), key=lambda s: s.lower())
        return [f"* [{login}](https://github.com/{login})" for login in sorted_logins]
    except Exception as e:
        print(f"Error: Failed to get contributors.\n{e}", file=sys.stderr)
        sys.exit(1)

def fill_contributors(release_notes_path):
    contributors = get_contributors(release_notes_path)
    
    try:
        with open(release_notes_path, "r", encoding="utf-8") as f:
            lines = f.read().split('\n')
            
        new_lines = []
        in_contributors_section = False
        
        for line in lines:
            if line.startswith("## :smiling_imp: Contributors"):
                new_lines.append(line)
                in_contributors_section = True
                if contributors:
                    new_lines.extend(contributors)
                continue
                
            if in_contributors_section:
                if line.startswith("## "):
                    in_contributors_section = False
                    if contributors and new_lines and new_lines[-1] != "":
                        new_lines.append("")
                    new_lines.append(line)
            else:
                new_lines.append(line)
                
        with open(release_notes_path, "w", encoding="utf-8") as f:
            f.write('\n'.join(new_lines))
            
    except Exception as e:
        print(f"Error: Failed to update release notes.\n{e}", file=sys.stderr)
        sys.exit(1)

def display_and_confirm_release_notes(release_notes_path):
    print(f"Release notes path: {release_notes_path}")
    print("\n" + "="*40)
    user_response = "n"
    try:
        user_response = input("Is the content correct? (y/n): ").strip().lower()
    except (KeyboardInterrupt, EOFError):
        print("\nProcess interrupted by user.")
        sys.exit(0)
        
    if user_response != "y":
        print("Release notes not confirmed. Ending script.")
        sys.exit(0)
        
    print("Release notes confirmed.")

def confirm_proceed_with_release(dry_run):
    print("\n" + "="*40)
    mode_text = "DRY RUN" if dry_run else "REAL RUN"
    print(f"You are running in {mode_text} mode.")
    user_response = "n"
    try:
        user_response = input("Do you really want to proceed with the release? (y/n): ").strip().lower()
    except (KeyboardInterrupt, EOFError):
        print("\nProcess interrupted by user.")
        sys.exit(0)
        
    if user_response != "y":
        print("Release aborted. Ending script.")
        sys.exit(0)

def replace_date_in_release_notes(release_notes_path, dry_run):
    from datetime import date
    current_date = date.today().strftime("%Y-%m-%d")
    try:
        if dry_run:
            print(f"Replacing $date with {current_date} in {release_notes_path} (dry-run)")
        else:
            with open(release_notes_path, "r", encoding="utf-8") as f:
                content = f.read()
            new_content = content.replace("$date", current_date)
            with open(release_notes_path, "w", encoding="utf-8") as f:
                f.write(new_content)
            print(f"Replaced $date with {current_date} in {release_notes_path}")
    except Exception as e:
        print(f"Error: Failed to replace $date in release notes.\n{e}", file=sys.stderr)
        sys.exit(1)

def create_release_branch(version, dry_run):
    branch_name = f"release/v{version}"
    try:
        if dry_run:
            print(f"Branch to be created: {branch_name}")
        else:
            subprocess.run(["git", "checkout", "-b", branch_name], check=True)
            print(f"Created branch dedicated to the release: {branch_name}")
    except Exception as e:
        print(f"Error: Failed to create release branch {branch_name}.\n{e}", file=sys.stderr)
        sys.exit(1)

def commit_release_modifications(version, dry_run):
    try:
        status_result = subprocess.run(
            ["git", "status", "--porcelain"],
            capture_output=True,
            text=True
        )
        if status_result.returncode != 0:
            print(f"Error: Failed to run git status.\n{status_result.stderr}", file=sys.stderr)
            sys.exit(1)
        
        status_output = status_result.stdout.strip()
        if not status_output:
            print("No modifications to commit.")
            return
        
        if dry_run:
            print(f"Modifications to be committed:\n{status_output}")
        else:
            subprocess.run(["git", "add", "-A"], check=True)
            commit_result = subprocess.run(
                ["git", "commit", "-m", f"Preparing release v{version}"],
                capture_output=True,
                text=True
            )
            if commit_result.returncode != 0:
                print(f"Error: Failed to commit modifications.\n{commit_result.stderr}", file=sys.stderr)
                sys.exit(1)
            print(f"Committed modifications for release v{version}.")
    except Exception as e:
        print(f"Error: Failed to commit modifications.\n{e}", file=sys.stderr)
        sys.exit(1)

def push_release_modifications(version, dry_run):
    branch_name = f"release/v{version}"
    try:
        if dry_run:
            print(f"Pushing branch {branch_name} to remote repository (dry-run)")
        else:
            subprocess.run(["git", "push", "-u", "origin", branch_name], check=True)
            print(f"Pushed branch {branch_name} to remote repository.")
    except Exception as e:
        print(f"Error: Failed to push release branch {branch_name}.\n{e}", file=sys.stderr)
        sys.exit(1)

def main():
    script_directory = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.abspath(os.path.join(script_directory, ".."))
    os.chdir(repo_root)

    dry_run = parse_dry_run_parameter()
    print(f"Running release script with dry-run={dry_run}")
    
    update_local_repository()
    verify_no_local_modifications(dry_run)

    version = extract_version_prefix()
    print(f"Extracted version prefix: {version}")

    release_notes_path = check_release_notes_file(version)
    fill_dependency_upgrades(release_notes_path)
    fill_contributors(release_notes_path)
    display_and_confirm_release_notes(release_notes_path)
    
    confirm_proceed_with_release(dry_run)
    replace_date_in_release_notes(release_notes_path, dry_run)
    create_release_branch(version, dry_run)
    commit_release_modifications(version, dry_run)
    push_release_modifications(version, dry_run)
    
    print("Release preparation complete.")

if __name__ == "__main__":
    main()
