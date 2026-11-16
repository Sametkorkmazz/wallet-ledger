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
- A new feature (e.g. `users`) gets the same sub-packages.
- No `utils`, `helpers` or `common` dumping grounds. A class without a home means the design is unclear.
- Classes are package-private unless another package needs them.
- Features call each other **through the other feature's service**, pass **ids**, and get back small records, never entities.

## Layers
| Layer | Its job | Must never |
|---|---|---|
| `api` | Parses requests, checks their shape (`@Valid`), calls one service method, picks the status code | Hold business rules, call stores, catch exceptions |
| `service` | Runs one use case; owns `@Transactional` | Hold business rules, catch and translate domain exceptions, know HTTP |
| `domain` | All business rules; plain Java, no Spring, no JPA, no Jackson | Know about HTTP or the database |
| `persistence` | **Store** (public): speaks domain objects. **Repository** and **entity** (package-private): table mapping | Hold business rules (they live in `domain`) |

Dependencies point inwards: `api → service → domain`, `service → persistence → domain`.

## The shape of a use case
Every write method in a service reads the same way:
```
1. load     → domain objects by id (or fail)
2. check    → rules that need data the domain object doesn't have: the service fetches, the domain decides
3. act      → call the domain factory or method
4. save     → through the store
5. respond  → return a result the api maps to a response DTO
```
- One public method = one use case, named after the business action (`postTransfer`, not `updateLines`).
- `@Transactional(readOnly = true)` on the class, `@Transactional` on write methods. Details: [transactions.md](05-transactions.md).
- Inject a `Clock`; never call `Instant.now()` in services or the domain.
- No interface with a single implementation. A class is enough until there are two.
- More than ~7 public methods or ~300 lines: split by use case (`TransferService`, `ReversalService`).

## Where each check lives
| Check | Where | On failure |
|---|---|---|
| Request shape: missing field, wrong format | Request DTO (`@Valid`) | 400 |
| Business rule | Domain constructor or domain method | Domain exception → 422 |
| Rule that needs data (funds, ownership) | Service fetches, the domain decides | Domain exception → 422 |
| `null` inside the code | `Objects.requireNonNull` | NPE: a bug → 500 |
| Storage format (column scale) | Entity | `IllegalArgumentException`: a bug → 500 |
| Everything, again | Database constraint | Last line of defense |

## Exceptions
- A broken business rule throws a **domain exception**. `IllegalArgumentException` and `NullPointerException` mean a bug.
- Services let domain exceptions pass. **One `@RestControllerAdvice`** maps them to responses ([api.md](06-api.md)).
- Messages say which rule broke, without leaking data the caller may not see.
- An exception that wraps another keeps it as the cause. Never `catch (Exception e) { log; continue }`.

## Enforced by ArchUnit
Entities stay in `persistence`; no code calls an entity's no-arg constructor; a domain record's constructor is called only by its own factories and the store; a money-moving factory (e.g. `LedgerEntry.transfer`) is called by one service only; controllers don't use stores, repositories or entities; `@Transactional` only in services; the domain doesn't import Spring, JPA or Jackson; no field injection; no `System.out`.

## Rejected in review
| Anti-pattern | Instead |
|---|---|
| `if … throw` chains in a service | A domain method or value object |
| The same condition in two places | One method on the owner |
| A service returning an entity | A domain object or DTO |
| Remote calls inside `@Transactional` | Event after commit or outbox ([transactions.md](05-transactions.md)) |
| Static `XxxUtils` with business logic | A named calculator, policy or value object |
