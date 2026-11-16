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

## Domain rules
| Rule | Why | Source |
|---|---|---|
| Rule on the smallest owner; one rule, one home | A copied condition drifts and becomes a bug | Evans, *DDD*: aggregates, value objects, domain services |
| Transitions in the enum, `switch` without `default` | A new status breaks the build instead of production | *Effective Java*: Item 34 (use enums instead of int constants) |
| Strategies instead of long `switch` branches | A new variant is a new class, not an edit to old code | Gamma et al., *Design Patterns* (1994): Strategy |

## API
| Rule | Why | Source |
|---|---|---|
| One error shape with a stable code | Clients and support handle every error the same way | [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457) |
| `Deprecation` and `Sunset` headers before removing `v1` | Clients learn about retirement from the response itself | [RFC 9745](https://www.rfc-editor.org/rfc/rfc9745), [RFC 8594](https://www.rfc-editor.org/rfc/rfc8594) |
| `Idempotency-Key` on money-moving requests | A retried request never moves money twice | IETF draft *The Idempotency-Key HTTP Header Field*; Stripe API docs |
| Committed OpenAPI file | Contract changes show up in review | [OpenAPI Specification](https://spec.openapis.org/oas/latest.html) |
| Money as strings in JSON | JavaScript numbers are binary doubles | ADR 4; [MDN: Number](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number) |

## Transactions and data
| Rule | Why | Source |
|---|---|---|
| No remote calls inside a transaction; outbox for reliable side effects | Commit and message can't diverge | Chris Richardson, [Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html) |
| Lock in a fixed order, re-read after locking | No deadlocks, no write skew | Kleppmann, *DDIA*: ch. 7 Transactions |
| Paged lists, no N+1, projections for reads | Queries stay fast as tables grow | Vlad Mihalcea, *High-Performance Java Persistence* |
| Expand → migrate → contract migrations | The old app version still runs, so rollback is a redeploy | Martin Fowler, [Parallel Change](https://martinfowler.com/bliki/ParallelChange.html) |

## Configuration, logging and observability
| Rule | Why | Source |
|---|---|---|
| Typed, validated configuration | A bad value fails at startup, not at 2 a.m. | Spring Boot reference: `@ConfigurationProperties` |
| No personal data in logs, log once | Logs leak and get copied; duplicates hide the cause | [OWASP Logging Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html) |
| Latency, traffic, errors, saturation on every dashboard | The four signals that show what users feel | Google, *Site Reliability Engineering* (2016): ch. 6 Monitoring Distributed Systems |
| Low-cardinality metric labels | Every distinct label value is a new time series | [Prometheus: metric and label naming](https://prometheus.io/docs/practices/naming/) |
| Percentiles, not averages | An average describes nobody | Gil Tene, *How NOT to Measure Latency* (talk) |

## Release and documentation
| Rule | Why | Source |
|---|---|---|
| SemVer derived from commits | The version number says what kind of change it is | [Semantic Versioning](https://semver.org/) |
| Human-readable changelog | Users see what changed without reading commits | [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) |
| Build once, deploy the same artifact everywhere | Production runs exactly what was tested | Humble & Farley, *Continuous Delivery* (2010) |
| Diagrams at the level that answers the question | One diagram per question, not one diagram for everything | Simon Brown, [The C4 model](https://c4model.com/) |
| Executable API collection in Git | Docs that run can't go stale silently | [Newman](https://github.com/postmanlabs/newman) |
| Secret scanning in CI | A leaked key is found before it is exploited | [Gitleaks](https://github.com/gitleaks/gitleaks) |

## Docker
| Rule | Why | Source |
|---|---|---|
| Config from the environment, same services everywhere | Dev matches production; no secrets in code | [The Twelve-Factor App](https://12factor.net/): Config, Dev/prod parity |
| Multi-stage build, non-root user, pinned image versions | Smaller images, smaller attack surface, repeatable builds | [Docker build best practices](https://docs.docker.com/build/building/best-practices/) |
