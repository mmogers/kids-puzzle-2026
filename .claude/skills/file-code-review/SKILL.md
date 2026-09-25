---
name: file-code-review
description: Review Java code, classes, or files for bugs, design issues, SOLID, best practices, exception handling, logging, tests, performance, and security. Use when the user asks to check, review, inspect, analyze, or improve Java code, including requests like "check the code in GridViewActivity".
context: fork
---

# Java Code Review

Review Java code deeply but pragmatically.

## Workflow

1. Identify the target file/class.
2. Read the entire target file.
3. Inspect relevant dependencies, callers, interfaces, DTOs, repositories,
   configuration, and tests when needed.
4. Check git status/diff if useful, but do not require uncommitted changes.
5. Review the code in its project context.

## Review

Check:

- Correctness and bugs
- SOLID and object-oriented design
- Java best practices
- Exception handling
- Logging
- Unit/integration tests
- Architecture and maintainability
- Performance
- Security

Pay particular attention to:
- swallowed exceptions
- overly broad catches
- lost exception causes
- incorrect retry/error handling
- inappropriate log levels
- sensitive data in logs
- duplicate or excessive logging
- missing edge/error-case tests
- unnecessary complexity

Do not invent test coverage percentages.

Avoid false positives and over-engineering. Follow existing project conventions
unless there is a concrete reason to recommend changing them.

## Findings

For each significant issue, provide:

- Severity: Critical / High / Medium / Low
- Location
- Problem
- Impact
- Recommendation

Distinguish actual bugs from design concerns, maintainability issues,
and optional preferences.

## Output

1. Summary
2. What the code does
3. Findings
4. SOLID
5. Exception handling
6. Logging
7. Tests
8. Improvements
    - Must fix
    - Should improve
    - Optional
9. Git changes, if relevant
10. Recommended next steps

Do not modify files unless explicitly asked.