# Automated checks

A rule that only lives in a document gets broken. Every rule that a machine can check, a machine checks.

## What runs where
| Check | Catches | Where |
|---|---|---|
| Unit, slice and integration tests | Broken behaviour | CI, every PR (`./mvnw verify`) |
| ArchUnit | Layer and package rules ([architecture.md](01-architecture.md#enforced-by-archunit)) | CI, as tests |
| Schema tests + `ddl-auto: validate` | Entity and schema out of sync, missing constraints | CI, as tests |
| Config test | Typos in profile files, literal secrets ([configuration.md](11-configuration.md)) | CI, as tests |
| OpenAPI contract test | Endpoint without a summary; unreviewed contract change | CI |
| Formatting | Unformatted code: Spotless (backend), Prettier (frontend) | CI |
| Lint and build | Frontend errors | CI |
| Coverage (JaCoCo) | Coverage dropping on domain and service packages; a floor, not a goal | CI |
| Dependency review / Dependabot | Vulnerable or outdated dependencies | CI + weekly |
| Secret scan (Gitleaks) | Passwords and keys in commits | CI |
| Migration check | An applied migration was edited, deleted or renamed | CI |
| Commit message check | Messages that break [git.md](09-git.md) | Git hook + CI |
| Newman | The deployed app doesn't work | After each deploy |
| Mutation testing (PIT, optional) | Tests that can't fail | Nightly, domain package |

## Git hooks (on your machine)
Install once per clone: `git config core.hooksPath .githooks`. Hooks can be skipped with `--no-verify`, so CI repeats every check.

`.githooks/commit-msg` rejects a subject that doesn't match:
```
^(feat|fix|perf|refactor|test|docs|build|ci|chore|style)!?: .{1,70}$
```
`.githooks/pre-push` blocks direct pushes to `main` and branch names that don't match `^(feat|fix|refactor|test|docs|ci|chore|style)/[a-z0-9-]+$`.

## Migration check (CI)
Fails the PR when an existing migration changed:
```bash
git diff --name-only --diff-filter=MDR origin/main...HEAD -- '**/db/migration/*'
```
Any output means a migration that may already have run was touched. Add a new one instead.

## Rules for the checks themselves
- A check that fails must say what to do next, in one line.
- No `@Disabled` without an issue number in the reason. A flaky test is a bug: fix it or delete it within a week.
- Never lower a threshold to make a PR pass. Change the threshold in its own PR, with the reason.
