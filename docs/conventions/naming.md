# Naming

## Classes
| Kind | Pattern | Example |
|---|---|---|
| Domain record / value object | Noun | `LedgerEntry`, `Money`, `AccountId` |
| Entity | `<Table>Entity` | `JournalEntryEntity` |
| Spring Data interface | `<Entity>Repository` | `JournalEntryRepository` |
| Persistence entry point | `<Feature>Store` | `LedgerStore` |
| Use case | `<Feature>Service` | `PostingService` |
| Controller | `<Resource>Controller` | `TransferController` |
| API input / output | `<Action>Request`, `<Thing>Response` | `TransferRequest` |
| Exception | `<What went wrong>Exception` | `AccountNotFoundException` |
| Test | `<Class>Test`; with Spring + database: `<Class>IntegrationTest` | `PostingServiceIntegrationTest` |

## Methods
- Verbs for actions: `postLedger`, `saveEntry`.
- `find…` may return nothing (`Optional`, empty list); `get…` fails if missing.
- Questions start with `is`, `has`, `can`: `isZero`.
- Plain nouns for derived values: `balanceOf`, `negate`.

## Variables
- Full words: `account`, not `acc`; `entryId`, not `eid`.
- Name it after what it holds, not its type: `savedEntryId`, not `uuid`.
- Constants: `UPPER_SNAKE_CASE` (`MAX_DESCRIPTION_LENGTH`).

## Database
- Tables: plural `snake_case` (`journal_entries`). Columns: `snake_case`, foreign keys end in `_id`.
- Constraints and indexes: `<prefix>_<table>_<what>`

| Prefix | Kind | Example |
|---|---|---|
| `pk_` | Primary key | `pk_idempotency_keys` |
| `fk_` | Foreign key | `fk_journal_lines_accounts` |
| `uq_` | Unique | `uq_accounts_user_currency` |
| `ck_` | Check | `ck_journal_lines_amount_not_zero` |
| `idx_` | Index | `idx_journal_lines_account_id` |
