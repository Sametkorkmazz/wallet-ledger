# Conventions

Short rules for how this project is built. Each rule is written once, in one of these files. When a new rule comes up, change the file in the same pull request. Where the code doesn't follow a rule yet, the rule wins: fix the code.

## The seven rules to remember

1. **Business rules live only in the domain.** Other layers never re-check them. Database constraints are the one deliberate second copy.
2. **An invalid domain object can't exist.** Records validate in their constructor; values with rules are value objects.
3. **Entities only map.** They are built from a domain object and hold no rules.
4. **Money is exact.** `Money` over `BigDecimal`, strings in JSON, never `double`, never silent rounding.
5. **A test describes one behaviour**, and its name says which. A bug fix starts with a failing test.
6. **No secrets in Git, no personal data in logs.**
7. **One logical change per pull request**, squash-merged with a `type: subject` title.

## Files, in reading order

Read them in number order: the big picture first, then the code layers from the inside out, then tests, workflow and operations.

| # | File | Covers | Read when |
|---|---|---|---|
| 01 | [architecture.md](01-architecture.md) | Packages, layers, use-case shape, where each check lives, exceptions | Day 1 |
| 02 | [naming.md](02-naming.md) | Classes, methods, variables, REST paths, metrics, database objects | Day 1 |
| 03 | [domain.md](03-domain.md) | Where a rule goes, records, value objects, statuses, policies | Before your first business rule |
| 04 | [persistence.md](04-persistence.md) | Stores, repositories, entities, mapping, migrations | Before touching the database |
| 05 | [transactions.md](05-transactions.md) | Transaction rules, locking, after-commit work, bulk updates | Before any write |
| 06 | [api.md](06-api.md) | Endpoints, status codes, DTOs, errors, paging, OpenAPI, versioning | Before your first endpoint |
| 07 | [testing.md](07-testing.md) | What to test, names, helpers, order, checklist | Day 1 |
| 08 | [testing-by-type.md](08-testing-by-type.md) | Rules and templates per kind of test | When you write that kind of test |
| 09 | [git.md](09-git.md) | Branches, commits, pull requests, reviews, everyday commands | Before your first commit |
| 10 | [docker.md](10-docker.md) | Local services, everyday commands, troubleshooting | Day 1 |
| 11 | [configuration.md](11-configuration.md) | Profiles, typed config, secrets, feature flags | Before changing any YAML |
| 12 | [logging.md](12-logging.md) | Levels, correlation id, what never to log | Before your first endpoint |
| 13 | [observability.md](13-observability.md) | Metrics, Grafana dashboard, alerts | Before your first release |
| 14 | [postman.md](14-postman.md) | Collection structure, scripts, Newman in CI | Before your first endpoint |
| 15 | [automation.md](15-automation.md) | Which checks run where: CI, hooks, ArchUnit | When a check fails |
| 16 | [release.md](16-release.md) | Versions, build once, release checklist, rollback, changelog | Before your first release |
| 17 | [documentation.md](17-documentation.md) | Docs as code, diagrams, ADRs | When you add a feature |
| 18 | [sources.md](18-sources.md) | Why each rule exists, and the book or guide it comes from | When someone asks "says who?" |
