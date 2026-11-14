# ADR 0004: Money type and rounding

Status: Proposed · Date: 2026-11-14

## Context

Amounts are `BigDecimal` in code and `NUMERIC(19, 4)` in the database, with 4 decimals for every currency. This lets a
balance hold amounts below the currency's smallest unit (for example 10.0037 USD). Those amounts can never be paid out
to a user. FX transfers between EUR, USD and TRY will produce such amounts unless rounding is defined. A JSON API and a
JavaScript frontend are also coming, and JavaScript stores every number as a binary `double`.

## Decision

1. **Type:** amounts are `BigDecimal`, wrapped in a `Money` record (currency + amount).
2. **Scale:** the column stays `NUMERIC(19, 4)`. The domain only allows amounts in the currency's minor units: 2 for
   USD, EUR and TRY. The extra column precision leaves room for a currency with 3 minor units (e.g. BHD) without a
   migration.
3. **Input:** an amount with more decimals than its currency allows (e.g. 10.005 USD) is rejected with 400. It is never
   rounded.
4. **Rounding:** happens only in FX conversion, with `HALF_EVEN`, to the target currency's minor units. No other code
   rounds money.
5. **Remainder:** a cross-currency transfer goes through a house FX account per currency. Each currency sums to zero on
   its own, and the rounding difference stays with the house. Example: 100 EUR at a rate of 1.08374 is 108.374 USD,
   rounded to 108.37:

   | Account        | Amount      |
   |----------------|-------------|
   | User A (EUR)   | −100.00 EUR |
   | House FX (EUR) | +100.00 EUR |
   | House FX (USD) | −108.37 USD |
   | User B (USD)   | +108.37 USD |

6. **Comparison:** amounts are compared with `compareTo` in code and `isEqualByComparingTo` in tests, never `equals`:
   `new BigDecimal("2.0").equals(new BigDecimal("2.00"))` is `false`.
7. **JSON:** amounts are strings in requests and responses (e.g. `"108.37"`), because JavaScript numbers are doubles.
8. **Ownership:** `Money` owns the rules it can check with its own two fields: the scale check, `add`, `subtract` and
   `negate`, which throw on a currency mismatch. It does not check zero lines (`LedgerLine`), negative balances (the
   posting service, ADR 3) or exchange rates (an `ExchangeRate` record that converts a `Money`).

## Consequences

**Gains**

- Users never hold amounts below their currency's smallest unit, so every balance can be paid out.
- Cross-currency transfers still balance per currency, and the rounding result is visible in the house FX account.
- Other code can't add two currencies or convert without a rate; `Money` throws.
- Amounts reach the frontend without precision loss. The frontend only displays them and does no math with them.
- A currency with 3 minor units can be added without a migration.

**Costs**

- Mapping between `Money`, the entity's `BigDecimal` and JSON strings at each edge.
- The house FX account builds up a small gain or loss from rounding. Someone must understand and check that position.
- The column allows 4 decimals but the domain allows 2, so the database alone does not enforce the rule (a `CHECK` can
  be
  added later).
- Every new currency needs its minor units set.

## Alternatives considered

- **`double`:** binary floating point. Values like 0.1 can't be stored exactly, so sums drift and equality checks fail
  (`0.1 + 0.2 != 0.3`). Unacceptable for a ledger where entries must sum to exactly zero.
- **`long` minor units** (32.03 = 3203): exact and fast, used by Stripe. Rejected because every currency needs its
  exponent at every boundary, FX math needs decimals anyway, overflow is silent without `Math.addExact`, and
  `BigDecimal` + `NUMERIC` is already in place.
- **Column scale 2:** enforces cents in the database, but a 3-minor-unit currency would need a migration.
- **`HALF_UP`:** ties always round up, so totals drift upward over many conversions.
- **Rounding input instead of rejecting:** the amount moved would differ from the amount requested, with no error.
