# PreToolUse hook: blocks `git commit` when the working tree has changes that are not staged,
# so a commit can't silently leave out edits made after the last `git add` (files showing as
# MM / AM / ?? in `git status --short`).
#
# Input: the hook payload JSON on stdin (tool_input.command, cwd).
# Exit 0 = allow, exit 2 = block (stderr is shown to Claude as the reason).

$ErrorActionPreference = 'Stop'

try {
    $payload = [Console]::In.ReadToEnd() | ConvertFrom-Json
} catch {
    exit 0  # not our payload shape; never block on a parsing problem
}

$command =[string]$payload.tool_input.command
if (-not $command) { exit 0 }

# only `git ... commit` invocations
if ($command -notmatch '\bgit\b[^;&|]*\bcommit\b') { exit 0 }

# staging in the same command line (`git add -A && git commit ...`) can't be judged beforehand.
# `add` must be the git subcommand at the start of a command segment (optionally after -C <path> /
# -c <key=value>), matched case-sensitively, so a message like `git commit -m "Add ..."` still gets checked.
if ($command -cmatch '(^|[;&|])\s*git(\s+-[Cc]\s+("[^"]*"|''[^'']*''|\S+))*\s+add\b') { exit 0 }

# `git commit -a` / `--all` stages modified tracked files itself; untracked files are still left out
$stagesTracked = $command -match '(^|\s)(--all|-[A-Za-z]*a[A-Za-z]*)(\s|$)'

$repoDir = if ($payload.cwd) { [string]$payload.cwd } else { (Get-Location).Path }

$status = & git -C $repoDir status --porcelain=v1 --untracked-files=all 2>$null
if ($LASTEXITCODE -ne 0) { exit 0 }  # not a git repo / git unavailable: nothing to guard

$problems = @()
foreach ($line in @($status)) {
    if (-not $line) { continue }
    $x = $line[0]
    $y = $line[1]
    $path = $line.Substring(3)
    if ($x -eq '?' -and $y -eq '?') {
        $problems += "  ?? $path  (untracked, not in the commit)"
    } elseif ($y -ne ' ' -and -not $stagesTracked) {
        $problems += "  $x$y $path  (has unstaged changes)"
    }
}

if ($problems.Count -eq 0) { exit 0 }

[Console]::Error.WriteLine(
    "Commit blocked by .claude/hooks/commit-guard.ps1: the index does not match the working tree.`n" +
    ($problems -join "`n") + "`n" +
    "Stage the files that belong in this commit (e.g. git add -A), or stash/restore the rest, then commit again.")
exit 2
