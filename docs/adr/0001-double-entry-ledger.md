# ADR 0001: Double-entry ledger

Status: Accepted · Date: 2026-11-02 · Amended: 2026-11-09 (signed amounts, currency on accounts)

## Context

A money transfer can be retried, can fail or can be sent at the same time as another transfer on the same account.
Account balance must be consistent, correct and traceable on all occasions. A single balance column can be overwritten
anytime, without a trace of what changed.

## Decision

Every transaction is recorded as one journal entry made of at least two lines. Each line posts a signed amount to one
account: a positive amount increases the account's balance and a negative amount decreases it. An entry balances when
its lines sum to zero per currency. Currency lives on the account, not on the line, and an account appears at most once
per entry.

New lines are appended and existing lines are never updated or deleted. To fix a wrong transaction, a new reversing
entry is added. Money entering the system uses an external account, so the entry still balances. An account's balance is
derived from its lines.

## Consequences

**Gains**

- Full audit trail of every money movement, with nothing lost.
- The books can be verified at any time, because everything must sum to zero per currency.

**Costs**

- More tables and joins than a simple balance column.
- A balance needs a query or a cache instead of a simple select.
- A flipped sign still sums to zero, so tests must check real balances, not only that entries balance.
- The "sums to zero" rule spans several rows, so a single `CHECK` can't enforce it. It is enforced when a `LedgerEntry`
  is created, with a database-level safety net possible later (to be decided in ADR 3).

## Amendment (2026-11-09)

The original text described a debit and a credit of the same amount. The implementation uses one signed amount per
line, with currency on the account. The decision to use double-entry bookkeeping is unchanged.
