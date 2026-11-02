# ADR 0001: Double-entry ledger

Status: Accepted · Date: 2026-11-02

## Context
A Money transfer can be retried, can fail or can be sent at the same time as another transfer on the same account. Account balance must be consistent, correct and traceable on all occasions. A single balance column can be overwritten anytime, without a trace of what changed.

## Decision
Every transaction is recorded as one journal entry made of at least two lines: a debit on one account and a credit on another, for the same amount. For every entry, total debits equal total credits. For new transactions, new lines are appended and existing lines are never updated or deleted. To fix a wrong transaction, a new reversing entry is added. Money entering the system also uses a second (external) account, so the entry still balances. An account's balance is derived from its lines.

## Consequences
**Gains**
- Full audit trail of every money movement, with nothing lost.
- The books can be verified at any time, because everything must sum to zero.

**Costs**
- More tables and joins than a simple balance column.
- A balance needs a query or a cache instead of a simple select.