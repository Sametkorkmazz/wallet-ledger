# Transactions, locking and bulk work

## Rules
| # | Rule | Trap it avoids |
|---|---|---|
| 1 | One public service method = one use case = one transaction | Half-done use cases |
| 2 | `@Transactional` only on **public** service methods | Spring's proxy ignores private ones |
| 3 | Never call a `@Transactional` method from the same class expecting a new transaction | Self-invocation bypasses the proxy silently |
| 4 | Rollback happens on unchecked exceptions; ours are unchecked | A checked exception commits half the work |
| 5 | **No remote calls** (HTTP, Kafka, e-mail) inside a transaction | Long locks; "sent the notification, rolled back the payment" |
| 6 | Keep transactions short; no waiting, no big loops | Pool exhaustion, lock waits |
| 7 | Never catch and swallow inside a transaction | `UnexpectedRollbackException` at commit |
| 8 | `REQUIRES_NEW` only with a written reason | Hidden extra connections, deadlocks |
| 9 | Long writes get a timeout: `@Transactional(timeout = 30)` | Runaway transactions |

## Isolation and locking
- Default isolation is PostgreSQL's `READ COMMITTED`. Pin it on methods whose correctness depends on it: `@Transactional(isolation = READ_COMMITTED)`.
- **Optimistic locking** (`@Version` → 409) for rows users edit.
- **Pessimistic locking** for money checks that read then write (funds check): `SELECT … FOR UPDATE` on the account rows, **always in `id` order** (no deadlocks), then re-read the balance in a separate statement. Every path that debits must take the lock.
- Every lock has a timeout; never wait forever.
- The choice and its alternatives (e.g. `SERIALIZABLE` + retry) are recorded in an ADR.
- Proven by a concurrency test: threads start together on a `CountDownLatch`; assert the final balances **and** that all lines sum to zero.

## Side effects after commit
- Something that may only happen if the commit succeeded: `@TransactionalEventListener(phase = AFTER_COMMIT)`.
- If losing it is unacceptable (it usually is with money): **transactional outbox**. Insert an outbox row in the same transaction; a separate publisher sends it.

## Bulk work (thousands of rows)
| Change | Approach |
|---|---|
| One-off data fix | A migration |
| Same change for every matching row | One set-based `UPDATE`; bump `version` if the rows have one |
| Value differs per row, no domain logic | `JdbcTemplate.batchUpdate`, ~1,000 rows per transaction |
| Each row needs domain logic | Chunks of ~500 with keyset paging (`id > :lastId`), one transaction per chunk |
| Scheduled, restartable, huge | Spring Batch |

- Commit per chunk and make the job safe to re-run (the `WHERE` skips rows already done).
- Never run bulk work inside an HTTP request: return `202 Accepted` with a job id, run on a bounded executor, expose the status.
- Scheduled jobs on several instances run once (ShedLock or a lock row).
