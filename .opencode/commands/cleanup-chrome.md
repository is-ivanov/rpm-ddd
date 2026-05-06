---
description: "Kill orphaned chromedriver and headless Chrome processes left over from failed Selenium test runs. Use when Selenium tests mass-fail with Connection reset or TimeoutException, when the system feels sluggish during test runs, or when the user mentions /cleanup-chrome. Also use proactively before running frontend acceptance tests if previous runs were interrupted or stopped."
subtask: true
---

Load the skill at .opencode/skills/cleanup-chrome/SKILL.md and execute it with the following arguments: $ARGUMENTS
