---
name: full-review
description: "Run a comprehensive review by combining the workspace's existing helper skills (TypeScript/React reviewer, ESLint/Prettier configurator, Java coding standards, and Java architecture advisor). Use when you need an all‑in‑one code audit or checkpoint."
---

# Full Code Review

This umbrella skill orchestrates the other four review/configuration skills in the repository. It’s useful when you want a single prompt to check a file or project for style, architecture, and lint/formatting compliance whether the code is TypeScript, React, or Java.

## When to Use

- Performing a general-purpose ``code review`` on a pull request or file
- Running a `check` step for a mixed‑language commit (frontend + backend)
- Ensuring newly added code adheres to all standards without invoking each skill individually

## Core Workflow

1. **Examine the file type**:
   - `.java` → apply *java-coding-standards* and *java-architect* skills
   - `.ts`/`.tsx` → apply *typescript-react-reviewer* and *eslint-prettier-config*
   - other languages → fall back to standard advice or simply run lint/format checks if configured

2. **Invoke individual skill logic** in sequence and aggregate the findings.
   - For TypeScript/React the reviewer checks patterns and conventions while the ESLint/Prettier helper inspects linter configuration and formatting.
   - For Java the coding standards skill audits naming, immutability, Optional/stream usage, etc., while the architect skill provides higher‑level guidance on package layout and design.

3. **Report results** as a consolidated review comment: list issues, suggestions, and highlight missing configurations.

## Examples of Prompts

- "Please run a full review on `src/pages/Todos/index.tsx` and tell me if anything breaks the guidelines."
- "Audit `backend/src/main/java/com/ces/eos/controller/TodoController.java` with the full-review skill."
- "I want a project‑wide lint, style and architecture check—use the full-review workflow."

## Notes

- This skill doesn’t replace each individual skill; it just sequences them when it detects multiple applicable domains.
- You can still call the focused skills directly if you need a deep dive on one aspect.
- The skill is workspace‑scoped and relies on the other skills existing in `.agents/skills` (should be present by default).


