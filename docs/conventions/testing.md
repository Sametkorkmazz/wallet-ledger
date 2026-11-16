# Testing

Templates for each kind of test are in [testing-by-type.md](testing-by-type.md).

## What to test
- **Your own business rules and queries**, both sides: one case that passes and one that fails, at the boundary (max length passes, one more fails).
- **Each `null` check** of a domain or entity class once per parameter.
- **A factory** only for what it adds, plus one test showing the constructor rules still apply.
- **Every database constraint** you wrote, asserting its name.
- **Not:** the framework, plain getters, private methods (test them through the public one), or a rule the code under test can't break.
- **Sabotage check** for every new rule or query: break it, see the test fail, restore it.

## Names
| Item | Rule | Example |
|---|---|---|
| Test class | `<ClassUnderTest>Test`, same package. Spring + database: `<Class>IntegrationTest`. Always ends in `Test` (Surefire skips `*IT`) | `JournalLineRepositoryTest` |
| `@Nested` class | PascalCase, no `Test` suffix: the method under test, or the rule being checked | `SumAmountByAccountId`, `NullArguments` |
| `@DisplayName` | On every `@Nested` class, **never on methods**. Sentence case, no full stop | `"Sum amount by account id"` |
| Test method | `<verb><Result>[When<Condition>]`. Never `test1`, `shouldX` or just the method name | `returnsZeroWhenAccountHasNoLines`, `rejectsNullAccountId` |
| Helper | `insertX`: raw SQL row, returns the id. `saveX`: through the code under test. `createX`: in memory only. `assertX`: the only helper that asserts | `insertAccount()`, `saveLine(...)`, `createEntry()` |

Helpers are private, dumb (no branching, no asserts except `assertX`), one level of abstraction, never `get…`, `setup()` or `insertAccount2`. Move one to `TestData` only when a second class needs it.

## Inside a test
- One behaviour, one reason to fail. A name that needs "And" means two tests.
- Arrange, act, assert, separated by blank lines. No `// given / when / then` comments.
- Variables: `account1`, `account2`; results named `found`, `sum`, `result`, `thrown`.
- Valid defaults as constants; each test changes only the one value it is about.
- Money: `new BigDecimal("10.50")`, never a `double`; compare with `isEqualByComparingTo`. Assert scale only in the scale test.
- Rejections: `assertThatThrownBy(...).isInstanceOf(...).hasMessageContaining(...)`. Always assert the message or constraint name.
- `@ParameterizedTest` only when 4+ inputs share one assertion.
- No test depends on another.

## Order
- **Members:** class annotations, constants, `@Autowired` / `@Mock` fields, `@BeforeEach` (only what every test needs), private helpers, `@Nested` groups.
- **Groups:** same order as the methods of the class under test. Domain classes: valid construction, null arguments, validation rules, normalization, behaviour.
- **Tests in a group:** empty or zero case, happy path, combinations, isolation (other rows ignored), rejections and edge cases.

## Before every commit
- Names, `@DisplayName` and order follow the rules above.
- Rejections assert the message or constraint name.
- Sabotage check done for each new rule or query.
- `./mvnw test` is green.
