# Why these rules, and where they come from

None of these rules are invented here. Each one comes from a widely used book, specification or company guide.

## Git and pull requests
| Rule | Why | Source |
|---|---|---|
| `type: subject` commit messages | Readable history, automatic changelogs and versioning | [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) |
| Imperative, short subject | Reads like "this commit will…", fits every tool's one-line view | Chris Beams, [How to Write a Git Commit Message](https://cbea.ms/git-commit/) |
| Small pull requests | Reviewed faster and more thoroughly, easier to roll back | Google, [Small CLs](https://google.github.io/eng-practices/review/developer/small-cls.html) |
| Descriptions that stand alone | A PR description is the permanent record of why the code changed | Google, [Writing good CL descriptions](https://google.github.io/eng-practices/review/developer/cl-descriptions.html) |
| ADRs for decisions with trade-offs | Future readers learn *why*, not just *what* | Michael Nygard, [Documenting Architecture Decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) (2011) |

## Architecture
| Rule | Why | Source |
|---|---|---|
| Service layer owns transactions, controllers stay thin | One place defines a use case and its transaction boundary | Martin Fowler, *Patterns of Enterprise Application Architecture* (2002): Service Layer |
| Business rules only in the domain | Rules are tested without a framework and can't drift apart | Eric Evans, *Domain-Driven Design* (2003): layered architecture |
| Dependencies point inwards | Domain code doesn't change when the database or web framework does | Tom Hombergs, *Get Your Hands Dirty on Clean Architecture* (2nd ed., 2023) |
| Package by feature, then by layer | Related code sits together; a feature can be understood in one place | Hombergs (above); Spring Modulith documentation |
| Domain exceptions, not catch-and-translate | A bug isn't reported to the client as "invalid request" | Joshua Bloch, *Effective Java* (3rd ed.): Items 70, 72, 73 |

## Domain and persistence
| Rule | Why | Source |
|---|---|---|
| Immutable records validated in the constructor | An invalid object can't exist, so no code has to check again | *Effective Java*: Item 17 (minimize mutability), Item 49 (check parameters) |
| Copy incoming collections | The caller can't change the object after validation | *Effective Java*: Item 50 (defensive copies) |
| Value objects for money and ids | Prevents mixing currencies and swapping ids; no silent rounding | Fowler, *PoEAA*: Money, Value Object; Evans, *DDD*: Value Objects |
| Static factory methods with meaningful names | Names say what is built; one constructor keeps the rules | *Effective Java*: Item 1 |
| Entities separate from the domain | Records can't be JPA entities; JPA details stay out of the rules | Hombergs, mapping strategies; Fowler, *PoEAA*: Data Mapper |
| Database constraints as a second copy | Data stays correct even when application code has a bug | Martin Kleppmann, *Designing Data-Intensive Applications*: consistency and transactions |
| Never edit an applied migration | Every database must end up with the same schema; Flyway checks checksums | Flyway documentation: versioned migrations |

## Naming
| Rule | Why | Source |
|---|---|---|
| Intention-revealing names, no abbreviations | Code is read far more often than written | Robert C. Martin, *Clean Code* (2008): ch. 2 Meaningful Names |
| `find` may return nothing, `get` fails | The caller knows from the name whether to handle "missing" | Spring Data repository naming conventions |

## Testing
| Rule | Why | Source |
|---|---|---|
| Many fast unit tests, few integration tests | Fast feedback; slow tests only where they prove something unit tests can't | Ham Vocke, [The Practical Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html) |
| Test behaviour, not implementation; one behaviour per test | Tests survive refactoring and fail for one clear reason | Vladimir Khorikov, *Unit Testing Principles, Practices, and Patterns* (2020) |
| Don't mock domain classes | Mocks of values hide real bugs; build the real object | Khorikov (above) |
| Arrange, act, assert | Every test reads the same way | Khorikov (above) |
| Real database in a container | An in-memory database behaves differently from production | Testcontainers documentation |
| Sabotage check | Proves the test can fail; the manual version of mutation testing | PIT mutation testing (pitest.org) |
| `@Nested` + `@DisplayName` | Test reports read like a specification | JUnit 5 User Guide |

## Docker
| Rule | Why | Source |
|---|---|---|
| Config from the environment, same services everywhere | Dev matches production; no secrets in code | [The Twelve-Factor App](https://12factor.net/): Config, Dev/prod parity |
| Multi-stage build, non-root user, pinned image versions | Smaller images, smaller attack surface, repeatable builds | [Docker build best practices](https://docs.docker.com/build/building/best-practices/) |
