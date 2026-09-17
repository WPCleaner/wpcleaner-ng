---
name: phabricator
description: Interact with Phabricator, the tool used to manage bug reports, tasks, and feature requests for this project. Use when the user asks to retrieve, analyze, summarize, or work on a Phabricator task (identified by T followed by a number, e.g., T392773) or references the Phabricator board/issues.
---

# Phabricator Skill

This skill provides precise guidance and procedures for interacting with Phabricator to retrieve, analyze, and process task information.

## Core Concepts

- **Phabricator Base URL:** https://phabricator.wikimedia.org/
- **Project Board:** https://phabricator.wikimedia.org/project/board/4842/
- **Task Identifiers:** Phabricator tasks are identified by the letter T followed by a unique numeric identifier (e.g., T392773).

---

## Workflow: Retrieving and Parsing a Phabricator Task

When instructed to retrieve, analyze, or summarize a Phabricator task, you MUST follow this workflow:

### 1. Extract the Task ID
Identify the task ID from the user request. It should match the pattern T\d+ (e.g., T392773). If only a URL is provided, extract the task ID from the URL path.

### 2. Construct the Task URL
Build the task URL by appending the task ID to the base URL:
https://phabricator.wikimedia.org/<task_id>
For example, for task T392773, the URL is https://phabricator.wikimedia.org/T392773.

### 3. Fetch Task Contents using web_fetch
Invoke the web_fetch tool on the constructed URL. Provide a clear, structured extraction prompt to guide the underlying parser. 
The prompt MUST instruct web_fetch to retrieve:
- **Title:** The main heading or title of the task.
- **Status:** The current workflow status (e.g., Open, Closed, Resolved, Stalled).
- **Metadata:** Author, assignee, creation/modification dates, and associated projects or tags.
- **Description:** The full detailed description of the issue or feature request.
- **Activity & Comments:** The list of relevant history comments, including decisions, sub-tasks, and code links.

Example web_fetch prompt:
> "Fetch task <task_id> from <url> and extract the task title, current status, metadata (author, assignee, projects), full description, and chronologically ordered comments or recent activity."

### 4. Present or Process the Task Content
Depending on the user intent:
- **For Inquiries (summarize, explain, retrieve):**
  Synthesize the retrieved content into a clean Markdown summary. Use headers, bullet points, and blockquotes for comments to make the report highly readable.
- **For Directives (implement, fix, build):**
  Use the extracted details to form your Research and Strategy phases. Do not write any code until you have successfully retrieved and understood the task requirements.

---

## Output Presentation Standard

When presenting a retrieved task to the user, format your output following this structure:

### [Task ID] Title
- **URL:** [Task URL]
- **Status:** [Status]
- **Assignee:** [Assignee] | **Author:** [Author]
- **Projects/Tags:** [Project Tags]

#### Description
[Full Description]

#### Comments & Activity
- **[Date] [User]:**
  > [Comment Text]
