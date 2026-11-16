# Architecture and packaging

## Packages: by feature, then by layer
```
com.example.app
└── ledger              ← feature
    ├── api             ← controllers, request/response DTOs
    ├── service         ← use cases
    ├── domain          ← records with the business rules
    └── persistence     ← entities, repositories, stores
```
A new feature (e.g. `users`) gets the same four sub-packages.

## Layers
| Layer | Does | Doesn't |
|---|---|---|
| `api` | Parses requests, checks their shape (`@Valid`), calls one service method, maps the result to a response | Hold business rules, call repositories |
| `service` | Loads data, builds domain objects, saves them; owns `@Transactional` | Hold business rules, catch and translate domain exceptions |
| `domain` | All business rules; plain Java, no Spring, no JPA | Know about HTTP or the database |
| `persistence` | **Store** (public): the entry point other layers use; it speaks domain objects. **Repository** (package-private): Spring Data interface. **Entity** (package-private): table mapping | Hold business rules |

Dependencies point inwards: `api → service → domain`, and `service → persistence → domain`. ArchUnit tests enforce the boundaries.

## Where each check lives
| Check | Where | On failure |
|---|---|---|
| Request shape: missing field, wrong format | Request DTO (`@Valid`) | 400 |
| Business rule | Domain constructor or domain method | Domain exception → 422 |
| Rule that needs data (funds, ownership) | Service fetches the data, the domain decides | Domain exception → 422 |
| `null` inside the code | `Objects.requireNonNull` | NPE: a bug → 500 |
| Storage format (column scale) | Entity | `IllegalArgumentException`: a bug → 500 |
| Everything, again | Database constraint | Last line of defense |

## Exceptions
- A broken business rule throws a **domain exception**. `IllegalArgumentException` and `NullPointerException` mean a bug.
- Services let domain exceptions pass. **One `@RestControllerAdvice`** maps them to responses.
- Messages say which rule broke, without leaking data the caller may not see.
- An exception that wraps another keeps it as the cause.
