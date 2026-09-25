---
description: Review all uncommitted code changes
---

# Uncommitted Code Review

Perform a thorough code review of the current uncommitted changes.

## 1. Inspect the changes

First inspect:

* `git status`
* `git diff`
* `git diff HEAD`


Review the actual changed code, but also read the surrounding classes and relevant dependencies when necessary to understand the change correctly.

Do not modify any files.

## 2. Explain the change

Start by explaining:

* What changed
* What behavior the change appears to introduce or modify
* Why the change likely exists
* How it fits into the existing code/architecture
* Which files/components are affected

If the intent cannot be determined confidently, state the uncertainty rather than inventing an explanation.

## 3. Review for problems

Look for:

* Bugs and incorrect behavior
* Edge cases
* Nullability problems
* Incorrect state handling
* Concurrency/thread-safety issues
* Resource leaks
* Breaking changes
* Backward compatibility problems
* Unnecessary complexity
* Duplicated or inconsistent logic
* Dead or unreachable code

Prioritize real problems over stylistic preferences.

## 4. Java and design

Check:

* SOLID principles
* Encapsulation
* Separation of responsibilities
* Appropriate abstractions
* Dependency injection
* API/design consistency
* Naming
* Immutability where appropriate
* Existing project conventions

Do not recommend refactoring merely for the sake of applying a pattern.

Prefer the simplest design that fits the existing architecture.

## 5. Exception handling

Check for:

* Swallowed exceptions
* Overly broad `catch` blocks
* Lost exception causes
* Incorrect exception types
* Incorrect error propagation
* Missing handling of expected failures
* Incorrect retry behavior
* Exceptions being logged and rethrown unnecessarily
* User-facing errors that are not handled appropriately

## 6. Logging

Check whether logging:

* Captures important failures 
* Provides useful diagnostic context
* Avoids duplicate or excessive messages
* Avoids sensitive information
* Preserves exception causes and stack traces when appropriate

Do not recommend adding logs everywhere.

## 7. Tests

Inspect existing tests related to the changed code.

Check whether the change is adequately tested, including:

* Normal behavior
* Edge cases
* Error handling
* Business rules
* Regression scenarios

Recommend tests only where they provide meaningful protection against realistic failures.

Do not invent coverage percentages.

## 8. Performance and security

Check only for issues relevant to the actual change, including::

* Performance regressions
* Unnecessary allocations or expensive operations
* Database/query problems
* Blocking operations
* Security vulnerabilities
* Unsafe input handling
* Sensitive data exposure
* Authorization/authentication problems

Only report issues that are relevant to the actual change.

## Output

Use this structure:

### Summary

Briefly summarize what the uncommitted changes do and the overall review result.

### Purpose of the Changes

Explain the apparent purpose and architectural context.

### Findings

For each finding:

* **Severity:** Critical / High / Medium / Low
* **Location:** file and relevant code
* **Problem:** what is wrong or risky
* **Impact:** why it matters
* **Recommendation:** how to improve it

Focus on actionable findings.

Order findings by severity.

If no meaningful issues are found, say so explicitly.

### Best Practices

Discuss relevant SOLID, Java, architecture, and maintainability concerns.

### Exception Handling

Summarize exception-related issues.

### Logging

Summarize logging-related issues.

### Tests

Summarize existing test coverage relevant to the changes and missing test cases.

### Improvements

Separate into:

* **Must fix**
* **Should improve**
* **Optional**

### Final Assessment

Briefly state whether the changes appear ready for commit, require fixes, or need further investigation.

Do not give a numeric score.

Do not modify files unless explicitly requested.
