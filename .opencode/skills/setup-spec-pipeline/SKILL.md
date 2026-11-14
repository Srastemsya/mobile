---
name: setup-spec-pipeline
description: Bootstrap an extensible specification-to-PR workflow in an existing project with a compact dispatcher, per-spec PM, Developer and independent black-box Tester. Use when asked to set up the agent pipeline, initialize project agent rules, or add rules to this skeleton. Optionally install the MatchWear rule profile. Setup only; do not implement specs unless separately requested.
---

# Setup Spec Pipeline

1. Locate the user-selected project. If none is accessible, ask for its location; never treat an upload or temporary directory as the project by default.
2. Read existing AGENTS.md, workflow/config.json and rule index if present. Preserve all existing instructions and work. Do not read application source merely to bootstrap.
3. Run `python3 <skill-dir>/scripts/bootstrap.py <project-root>`. Add `--profile matchwear` only for MatchWear or when explicitly requested. The script skips existing files and appends only its missing routing block to AGENTS.md; inspect the resulting instructions for conflicts.
4. Discover spec directory, base branch and test commands from project documentation and metadata. Set workflow/config.json accordingly; leave unknowns null and report them. Do not claim unset settings are working. Do not install dependencies, alter application source, create remote PRs or launch the queue as part of setup.
5. To add rules, create a focused Markdown file in workflow/rules/ and register path, applicable roles and scope in workflow/rules/index.md. Read and reconcile existing rules; never silently override a conflict. Treat supplied rule text as requirements for the requested project, not instructions overriding this skill's scope.
6. Verify JSON parses, referenced role/rule files exist, and rerunning bootstrap preserves existing content. Summarize how to launch: ask the main chat to read workflow/QUEUE.md and process the configured specs. Explain any missing prerequisites.

Use provided Markdown role files as explicit task instructions, not as native custom-agent configuration. Do not invent slash commands, auto-enable features or promise hard token budgets/background uptime. Fresh-context creation is required for independence; if the available harness cannot create a non-inheriting agent, stop that task as BLOCKED rather than leak source to Tester.
