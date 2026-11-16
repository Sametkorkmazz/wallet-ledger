# Conventions

Short rules for how this project is built. Each rule is written once, in one of these files. When a new rule comes up, change the file in the same pull request. Where the code doesn't follow a rule yet, the rule wins: fix the code.

## The five rules to remember

1. **Business rules live only in the domain.** Other layers never re-check them. Database constraints are the one deliberate second copy.
2. **An invalid domain object can't exist.** Records validate in their constructor.
3. **Entities only map.** They are built from a domain object and hold no rules.
4. **A test describes one behaviour**, and its name says which.
5. **One logical change per pull request**, squash-merged with a `type: subject` title.

## Files

| File | Covers |
|---|---|
| [git.md](git.md) | Branches, commit messages, pull requests, everyday commands |
| [architecture.md](architecture.md) | Packages, layers, where each check lives, exceptions |
| [domain-and-persistence.md](domain-and-persistence.md) | Domain records, value objects, entities, mapping, migrations |
| [naming.md](naming.md) | Classes, methods, variables, database objects |
| [testing.md](testing.md) | What to test, names, helpers, order, checklist |
| [testing-by-type.md](testing-by-type.md) | Rules and templates per kind of test |
| [docker.md](docker.md) | Local services, everyday commands, troubleshooting |
