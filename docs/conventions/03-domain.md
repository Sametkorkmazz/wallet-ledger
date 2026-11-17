# Domain model and business rules

## Principles
1. **Business rules live only in the domain.** Services orchestrate; api, mappers, stores and SQL hold none.
2. **Every rule has one home and one test.** A condition that appears twice is a future bug.
3. **Invalid states are impossible, not just checked.** Objects are born valid, stay valid, and say what they mean.
4. **Put each rule on the smallest thing that owns it.**
5. **The domain knows nothing about infrastructure.** What it needs (time, balances, rates) is passed in.
6. **Broken rules have names**: a domain exception that says which rule broke.

## Where does this rule go?
Ask in order, stop at the first yes:

| Question | Home | Example |
|---|---|---|
| About the shape of the input? | Request DTO (`@Valid`) | amount is present and a decimal string |
| About one value? | Value object | `Money`: at most 2 decimals, same-currency arithmetic |
| About one object's own fields or children? | Record constructor / domain method | `LedgerEntry`: at least 2 lines, sums to zero per currency |
| About allowed status changes? | The enum (`canMoveTo`) | only `PENDING` can become `POSTED` |
| A calculation with no state? | Calculator / strategy | FX conversion with `HALF_EVEN` |
| Needs data from elsewhere (balance, owner)? | Policy method in the domain; the service fetches the inputs | funds check, ownership check |
| None of these? | The order of steps → service | load, check, act, save, respond |

## Domain classes
- **Records**, immutable, validated in the compact constructor. Copy incoming collections with `List.copyOf`.
- **Factory methods** only when they fill fields for the caller: `of`, `from`, or a name that says what is built (`LedgerEntry.reversal`). A separate creator class only when creation needs a collaborator (policy, calculator, clock) or has several callers.
- **Behaviour named after the business:** `entry.reverse()`, not `setType(...)`.
- Small: a few hundred lines at most. Bigger means rules belong in a value object, policy or strategy.

## Value objects
- Use one whenever a value **has rules** or is **several fields that belong together**: `Money`, `AccountId`, `EntryId`, `UserId`, `Percentage`.
- Never a bare `UUID`, `BigDecimal` or `String` for these in the domain.
- Validated once in the constructor; every method returns a new value. Holding a `Money` means it is valid.
- A narrower rule gets its own type instead of an `if`, e.g. `PositiveMoney` when an amount must be above zero.
- Money is never rounded silently. Rounding happens in one named place (FX conversion, ADR 4).

## Status and lifecycle
- One enum per aggregate, never a shared `Status` enum.
- Allowed transitions live in the enum, as one table; the domain object changes status only through one private `moveTo`. No `setStatus`.
```java
public enum TransferStatus {
    PENDING, COMPLETED, FAILED;
    private static final Map<TransferStatus, Set<TransferStatus>> NEXT = Map.of(
            PENDING, EnumSet.of(COMPLETED, FAILED),
            COMPLETED, EnumSet.noneOf(TransferStatus.class),
            FAILED, EnumSet.noneOf(TransferStatus.class));
    public boolean canMoveTo(TransferStatus next) { return NEXT.get(this).contains(next); }
}
// in the domain object: if (!status.canMoveTo(next)) throw new LedgerRuleException(...);
```
- Named groups (`OPEN_FOR_PAYMENT`) instead of status lists copied into services and queries.
- `switch` expressions without `default`, so a new value is a compile error wherever a decision is needed.
- Draw the state diagram in the docs; one parameterized test checks every from→to pair.

## Policies, calculators, strategies
- **Policy:** the checks of one use case that need outside data. It only checks, never changes data. One per use case, not one big `Validator`.
- **Calculator:** a pure calculation with a business name, tested with tables of cases.
- **Strategy:** one class per variant instead of a long `switch`. A test asserts every enum value has a strategy.

## Policies that can't be skipped
A record checks its own rules automatically, but nothing forces anyone to call a separate policy. For rules where a skipped check means lost money (funds, ownership), close every door, the compiler first and ArchUnit for the rest:

1. **The policy returns a proof.** Only the policy can create it (private constructor).
2. **The factory requires the proof** and checks it covers the same account and amount. Forgetting the policy on this path is a **compile error**.
```java
public static FundsApproval check(AccountId from, Money balance, Money amount) { … return new FundsApproval(from, amount); }

public static LedgerEntry transfer(TransferCommand command, FundsApproval approval) {
    Objects.requireNonNull(approval, "the funds check must run first");
    if (!approval.covers(command.from(), command.amount())) throw new LedgerRuleException("approval for another account or amount");
    return new LedgerEntry(…);
}
```
3. **Only factories (and the store, to rebuild loaded rows) call the record's constructor.** A public record's constructor is public, and Java has no "only this other package" access, so this door can't be closed by the compiler. ArchUnit closes it: a direct `new LedgerEntry(...)` fails the build.
4. **One service calls the factory** (ArchUnit), so there is one place where lock → read → check happens in order.
5. **Tests** prove it end to end: insufficient funds → 422 and nothing saved; the concurrency test never goes negative.

The resulting chain: `policy → proof → domain factory → entity built from the domain object → database constraints`. No path through the code skips a link; raw SQL is stopped by the constraints.

The proof shows the check **ran**, not that it ran on a fresh balance after the lock: that order is guarded by step 4 and the tests. Use proofs only for money-critical rules; a plain policy plus a test is enough for the rest.

## Domain exceptions
- One base (`LedgerRuleException`) or a few specific subclasses; each message names the broken rule.
- A dedicated class only when code must react to that case differently (`AccountNotFoundException` → 404).
- Never `new RuntimeException("invalid")`.

## Defense in depth
Take "a journal line amount is never zero":

| Layer | Job |
|---|---|
| Request DTO | Friendly 400 for HTTP input |
| Domain (`LedgerLine`) | **The guarantee** for every code path |
| Database (`ck_journal_lines_amount_not_zero`) | Catches raw SQL, scripts and bugs |
| Tests | Prove all three exist |
