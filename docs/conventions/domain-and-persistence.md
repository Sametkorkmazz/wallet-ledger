# Domain, value objects, entities and migrations

## Domain classes
- **Records**, immutable, validated in the compact constructor. An invalid one can't exist.
- Copy incoming collections (`List.copyOf`) so the record owns its data.
- A **factory method** exists only when it fills fields for the caller: `of`, `from`, or a name that says what it builds (`reversal`).

## Value objects
- Wrap any value that has rules or can be confused with another: `Money` (amount + currency, fixed scale), `AccountId`, `EntryId`, `UserId`.
- Never use a bare `UUID`, `BigDecimal` or `String` for these in the domain.
- Money is never rounded silently: too many decimals is an error.

## Entities
- Package-private, in `persistence`. Only mapping: no business rules, no setters.
- Built **only from a domain object**: `new JournalEntryEntity(LedgerEntry)`.
- The protected no-arg constructor is for JPA only; no code calls it (ArchUnit).
- Column attributes match the schema (`nullable`, `length`, `precision`, `scale`).
- `ddl-auto: validate`: Hibernate checks the mapping, Flyway owns the schema.

## Mapping
- Entity ← domain: in the entity constructor.
- Entity → domain: in the store, through the domain constructor (so rules run again).
- Read-only screens (lists, history): query straight into a DTO or projection, no domain object needed.
- A separate mapper class (or MapStruct) only when mapping code grows beyond a few lines per entity.

## Migrations (Flyway)
- One change per file: `V<n>__<what_it_does>.sql`.
- **Never edit a migration that has been applied.** Fix forward with a new one.
- Every rule the domain enforces gets a constraint, with a name (see [naming.md](naming.md)) and a schema test.
