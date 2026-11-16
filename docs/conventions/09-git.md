# Git, commits and pull requests

## Rules
- `main` is always green. Nobody commits to it directly: branch, then open a PR.
- One branch per change: `<type>/<short-kebab-name>`, e.g. `feat/v5-entry-metadata`. Delete it after merging.
- One logical change per PR, reviewable in about 15 minutes. Run the tests before every commit.
- Stage files by name or with `git add -p`, not `git add .`, so local files (`.idea/`, notes) stay out.
- Never force-push `main`. On your own branch, only `--force-with-lease`.
- Anything already pushed is undone with `git revert`, never `reset`.

## Commit messages
Format: `<type>: <subject>`. The squash merge adds ` (#N)`.

| Type | For | Example |
|---|---|---|
| `feat` | New behaviour | `feat: add Money value type (#38)` |
| `fix` | A bug fix | `fix: use the danger token for destructive (#42)` |
| `refactor` | Same behaviour, better code | `refactor: use Money in LedgerLine (#46)` |
| `test` | Tests only | `test: guard entity classes with an ArchUnit rule (#28)` |
| `docs` | README, ADRs, these files | `docs: add ADR 2 on idempotent transfers (#56)` |
| `ci` | Workflows | `ci: check frontend formatting (#47)` |
| `chore` | Tooling, dependencies | `chore: add Prettier with Tailwind class sorting (#44)` |
| `style` | Formatting or visual polish, no logic | `style: match dashboard header to the design (#57)` |
| `perf` | Faster, same behaviour | `perf: index journal lines by account` |
| `build` | Maven, dependencies, Dockerfile | `build: add JaCoCo coverage report` |

- Lowercase, imperative ("add", not "added"), no full stop, about 70 characters at most.
- No scopes (`feat(ledger):`) until the codebase needs them.
- The squash commit's body lists the branch commits as bullets, in the same format. A one-commit PR has no body.
- A breaking change gets `!` after the type (`feat!: rename amount to value`) and a `BREAKING CHANGE:` footer.
- Checked automatically by a Git hook and in CI ([automation.md](15-automation.md)).

## Pull requests
- **Small:** under ~400 changed lines (generated files excluded). A big feature becomes several PRs, unfinished parts behind a flag.
- Open a **draft PR** early when you want feedback on the direction.
- **The title is the future commit message.**
- **The description follows the template:** *What* and *Testing* always; *Rules / decisions*, *Not covered yet* and *Notes* only when they apply. Delete empty sections.
- Link the work item with `Closes #N`. The description must make sense on its own.
- CI green before merging. Merge with **squash**.
- A decision with trade-offs gets an ADR in `docs/adr`, linked from the PR.
- Nobody merges their own PR without a review once the team has more than one person. New commits after approval need a new look.

## Reviews
- Answer a review request within one working day.
- Start every comment with a label: `[blocking]`, `[suggestion]`, `[question]`, `[nit]`.
- Reviewer checklist:
  - [ ] Does what the issue asks, and nothing else
  - [ ] Layer rules respected ([architecture.md](01-architecture.md)); each business rule has one home and a test
  - [ ] Tests: happy path, errors, edge cases; a bug fix starts with a failing test
  - [ ] No secrets, no personal data in logs
  - [ ] New migration (never an edited one), backward compatible
  - [ ] API change: OpenAPI diff reviewed, Postman updated, breaking change marked `!`
  - [ ] Docs and diagrams updated if the behaviour or flow changed

## Definition of Done
- [ ] Acceptance criteria of the issue met
- [ ] `./mvnw verify` and the frontend build green, locally and in CI
- [ ] Tests, docs, OpenAPI and Postman updated
- [ ] Unfinished work behind a flag that is off
- [ ] Reviewed and squash-merged; branch deleted

## Typical workflow
```bash
git switch main && git pull
git switch -c feat/my-change
# edit, run tests
git add -p                         # stage one logical change
git diff --staged                  # check what goes in
git commit -m "feat: ..."
git push -u origin feat/my-change
gh pr create --fill                # then edit title/description to the template
gh pr checks                       # CI status
gh pr merge --squash --delete-branch
git switch main && git pull && git branch -d feat/my-change
```

## Commands to know
| Need | Command |
|---|---|
| What changed | `git status -s`, `git diff`, `git diff --staged` |
| Recent history | `git log --oneline --graph -15` |
| Split one file into two commits | `git add -p file` (`y` stage, `n` skip, `s` split, `e` edit hunk) |
| Change the last commit (not pushed) | `git commit --amend` (`--no-edit` keeps the message) |
| Unstage a file | `git restore --staged file` |
| Discard edits in a file | `git restore file` (can't be undone) |
| Undo the last local commit, keep changes | `git reset --soft HEAD~1` |
| Undo a pushed commit | `git revert <commit>` |
| Park changes | `git stash push -m "wip"`, later `git stash pop` |
| Update branch with main | `git pull --rebase origin main` |
| Branch changes since it left main | `git diff main...my-branch` |
| Who changed a line | `git blame file`, `git log -p file` |
| Find a "lost" commit | `git reflog`, then `git switch -c rescue <hash>` |
