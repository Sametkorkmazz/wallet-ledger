# Persistence: entities, stores, repositories and migrations

## Stores and repositories
- The **store** (public) is the only door into `persistence`. It takes and returns domain objects and holds no rules.
- **Repositories** (package-private) only contain queries.
- Derived query names up to ~3 conditions; longer → `@Query` with a text block.
- JPQL first; native SQL only with a reason, and always covered by a Testcontainers test.
- **Never concatenate strings into a query.** Always parameters.
- **Every list query is paged.** Never `findAll()` on a business table.
- Watch for N+1: `join fetch`, `@EntityGraph`, or a projection.
- Read-only screens (history, lists) query straight into a DTO or projection; no domain object needed.

## Entities
- Package-private, in `persistence`. Only mapping: no business rules, no setters.
- Built **only from a domain object**: `new JournalEntryEntity(LedgerEntry)`. The protected no-arg constructor is for JPA only (ArchUnit).
- Column attributes match the schema (`nullable`, `length`, `precision`, `scale`).
- `@Enumerated(EnumType.STRING)`, never `ORDINAL`: reordering constants must not change stored data.
- `Instant` (UTC) for timestamps, `BigDecimal` for amounts.
- `@Version` on any row that can change (optimistic locking → 409). Append-only ledger rows don't need it.
- `FetchType.LAZY` on every `@ManyToOne`; collections initialised (`= new ArrayList<>()`).
- No Lombok `@Data`, `@EqualsAndHashCode` or `@ToString` on entities (lazy loading, recursion, personal data in logs).
- `spring.jpa.open-in-view: false` and `ddl-auto: validate`: Hibernate checks the mapping, Flyway owns the schema.

## Mapping
- Entity ← domain: in the entity constructor.
- Entity → domain: in the store, through the domain constructor, so the rules run again.
- A separate mapper class (or MapStruct) only when mapping grows beyond a few lines per entity.

## Migrations (Flyway)
- One change per file: `V<n>__<what_it_does>.sql`.
- **Never edit a migration that has been applied.** Fix forward with a new one; data fixes are migrations too.
- **Backward compatible** (expand → migrate → contract), so the previous app version still runs on the new schema: add a nullable column → backfill → make it `NOT NULL` in a later release; rename = add, copy, drop later.
- No hand-made changes in any shared database. If it isn't in a migration, it doesn't exist.
- Every rule the domain enforces and SQL can express gets a named constraint ([naming.md](02-naming.md)) and a schema test.
- A save-and-reload test per entity catches what `validate` doesn't: `NOT NULL` columns the entity doesn't fill, `CHECK` constraints, lengths.
