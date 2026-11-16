# Tests by type

| Class under test | Test class | Annotations | One `@Nested` group per | Docker |
|---|---|---|---|---|
| Domain record / class | `LedgerEntryTest` | none | rule | No |
| JPA entity | `JournalLineEntityTest` | none | rule | No |
| Repository | `JournalLineRepositoryTest` | `@DataJpaTest`, `@Import(TestcontainersConfiguration.class)`, `@AutoConfigureTestDatabase(replace = NONE)` | repository method | Yes |
| Database schema | `LedgerSchemaTest` | `@SpringBootTest`, `@Import(TestcontainersConfiguration.class)` | table | Yes |
| Service logic | `PostingServiceTest` | `@ExtendWith(MockitoExtension.class)` | public method | No |
| Service transactions, locks | `PostingServiceIntegrationTest` | `@SpringBootTest`, `@Import(TestcontainersConfiguration.class)` | scenario | Yes |
| Controller | `TransferControllerTest` | `@WebMvcTest(TransferController.class)` | endpoint | No |
| Architecture | `LedgerArchitectureTest` | `@AnalyzeClasses` | none | No |

## Domain and entity (plain JUnit)
```java
class JournalLineEntityTest {
    private static final BigDecimal AMOUNT = new BigDecimal("10.00");

    @Nested @DisplayName("Null arguments")
    class NullArguments {
        @Test void rejectsNullAmount() { ... }   // new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, null)
    }
}
```
- Exactly one happy-path test in "Valid construction". Scale and normalization get their own group.

## Repository (`@DataJpaTest`)
- Prepare rows with `insertX` / `saveX` helpers, never copy-pasted SQL.
- Call `testEntityManager.clear()` between saving and querying, so the read hits the database.
- Rolled back after each test: no cleanup.
- Isolation tests: give other accounts amounts that cancel out (+15 / −15), so a query that sums everything fails.
- Constraint violations: `DataIntegrityViolationException` through a repository, Hibernate's `ConstraintViolationException` through the `EntityManager`.

## Schema (`@SpringBootTest` + `JdbcTemplate`)
```java
@Nested @DisplayName("Accounts table")
class AccountsTable {
    @Test void rejectsUnknownCurrency() {
        assertThatThrownBy(() -> insertAccount(userId, "XYZ"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("ck_accounts_currency");
    }
}
```
- Clean the tables in `@BeforeEach` (one `TRUNCATE` of all tables).
- Use a bad value that breaks **only** that rule (`'XYZ'`, not `'BANANA'`, which fails `VARCHAR(3)` first).
- Rules across several rows (debits = credits) belong in domain or service tests.

## Service unit (Mockito)
- Stub only what the test needs. Prefer asserting the result or exception; `verify(...)` only for side effects that matter (saved, never saved, published once).
- Never mock domain classes. Build real ones.

## Service integration (`@SpringBootTest`)
- For what mocks can't prove: transactions, rollback, locking, concurrency, idempotency.
- No automatic rollback: clean the tables in `@BeforeEach`.
- Group by scenario (`"Transaction behaviour"`, `"Concurrent transfers"`), not by method.
- Concurrency: fixed thread count, start all threads on a `CountDownLatch`, assert the final balances **and** that all lines sum to zero.

## Controller (`@WebMvcTest` + MockMvc)
- Mock the service with `@MockitoBean`. No database.
- Group `@DisplayName` = method + path exactly: `"POST /transfers"`.
- Test name: `returns<Status>When<Condition>`, e.g. `returns409WhenIdempotencyKeyWasUsedWithDifferentBody`.
- Checks mapping, validation, status codes and security. Business rules are tested elsewhere.

## Architecture (ArchUnit)
- Name `<Scope>ArchitectureTest`, in the root package it guards. No `@Nested`, no `@DisplayName`.
- One `@ArchTest static final ArchRule` per rule, named as a statement: `entitiesResideInPersistencePackage`.
- Match by annotation or package, not class-name suffix. Put the reason in `because(...)`.
- Sabotage check: break the rule once, see it fail.
