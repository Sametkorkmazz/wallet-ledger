# Naming

Code is in English. A business term gets one English name and keeps it everywhere: `JournalEntry`, never `Posting` in one class and `Entry` in another.

## Classes
| Kind | Pattern | Example |
|---|---|---|
| Domain record / value object | Business noun | `LedgerEntry`, `Money`, `AccountId` |
| Enum | Singular noun | `EntryType.TRANSFER` |
| Entity | `<Table>Entity` | `JournalEntryEntity` |
| Spring Data interface | `<Entity>Repository` | `JournalEntryRepository` |
| Persistence entry point | `<Feature>Store` | `LedgerStore` |
| Use case | `<Feature>Service`; split by use case: `<Thing><UseCase>Service` | `PostingService` |
| Controller | `<Resource>Controller` | `TransferController` |
| API input / output | `<Verb><Thing>Request`, `<Thing>Response` | `CreateTransferRequest`, `TransferResponse` |
| Checks for one use case | `<Thing><UseCase>Policy` | `TransferFundsPolicy` |
| Pure calculation | `<Thing>Calculator` | `FxCalculator` |
| Variant by type | `<Type><Rule>Strategy` | `EqualSplitStrategy` |
| Exception | `<WhatWentWrong>Exception` | `AccountNotFoundException` |
| Typed config | `<Area>Properties` | `IdempotencyProperties` |
| Background job | `<Thing><What>Job` | `IdempotencyKeyCleanupJob` |
| Test | `<Class>Test`; with Spring + database: `<Class>IntegrationTest` | `PostingServiceIntegrationTest` |

Never `Impl`, `Util`, `Helper`, `Manager`, `Dto` as a class name.

## Methods
- Verbs for actions: `postLedger`, `saveEntry`. Services use business verbs; controllers HTTP-ish ones (`create`, `getById`, `list`).
- `find…` may return nothing (`Optional`, empty list); `get…` fails if missing.
- Questions start with `is`, `has`, `can`: `isZero`, `canMoveTo`.
- Plain nouns for derived values: `balanceOf`, `negate`.

## Variables
- Full words: `account`, not `acc`; `entryId`, not `eid`. Name it after what it holds: `savedEntryId`, not `uuid`.
- Collections are plural (`lines`); maps say what maps to what (`accountById`).
- Units in the name when the type doesn't carry them: `timeoutMs`, `retentionHours`.
- Constants: `UPPER_SNAKE_CASE` (`MAX_DESCRIPTION_LENGTH`).

## REST
- Plural nouns, kebab-case, versioned: `/api/v1/transfers`, `/api/v1/accounts/{accountId}/entries`.
- No verbs in paths, except real actions that aren't CRUD: `POST /api/v1/entries/{id}/reversal`.
- JSON fields and query parameters in `camelCase`.

## Metrics
- `wallet.<feature>.<what>`, lower case, dots: `wallet.transfer.posted`. Details: [observability.md](13-observability.md).

## Database
- Tables: plural `snake_case` (`journal_entries`). Columns: `snake_case`; foreign keys end in `_id`.
- Constraints and indexes: `<prefix>_<table>_<what>`

| Prefix | Kind | Example |
|---|---|---|
| `pk_` | Primary key | `pk_idempotency_keys` |
| `fk_` | Foreign key | `fk_journal_lines_accounts` |
| `uq_` | Unique | `uq_accounts_user_currency` |
| `ck_` | Check | `ck_journal_lines_amount_not_zero` |
| `idx_` | Index | `idx_journal_lines_account_id` |
