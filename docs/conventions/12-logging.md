# Logging

Logs are how we debug production, and they are also a data-protection risk. Both shape these rules.

## Setup
- SLF4J (`@Slf4j`). Never `System.out` or `printStackTrace()` (ArchUnit).
- Plain text locally, **structured JSON everywhere else**: `logging.structured.format.console: ecs` (Spring Boot 3.4+).
- Write to stdout; the platform ships the logs. The app manages no log files.

## Correlation id
- Every request gets an id (`X-Correlation-Id` header, or a new UUID), put into the MDC by a filter, returned in the response, forwarded on outgoing calls, and cleared afterwards (threads are reused).
- With tracing on, `traceId` is in every line too. Error responses carry the id, so support can find the logs.

## Levels
| Level | For | Example |
|---|---|---|
| `ERROR` | Someone must look at it. Always with the exception | DB down, unexpected exception |
| `WARN` | Unusual but handled; client errors; retries | Business rule rejected, version conflict |
| `INFO` | One line per business event or lifecycle step | `Transfer posted entryId=… lineCount=2` |
| `DEBUG` | Developer detail, off in production | Calculated FX rate |
| `TRACE` | Never in shared environments | — |

## What to log
```java
log.info("Transfer posted entryId={} lineCount={}", entryId, lines.size());   // ids, counts, codes
log.error("Outbox publish failed messageId={}", messageId, ex);              // exception last
```
- Placeholders, never string concatenation.
- Never log a whole entity or DTO (`toString` may hold personal data or trigger lazy loading).
- **Log once**, where the error is handled. No log-and-rethrow.

## Never log
Names, e-mail addresses, phone numbers, IBANs, card numbers, passwords, tokens, session ids, full request or response bodies, or amounts together with someone's identity. If a value must appear, mask it with one shared helper.

## Access and audit
- Access log: method, path template, status, duration, correlation id. No query strings if they can carry personal data.
- **Audit is not logging.** Who did what lives in the database; in this project the append-only ledger (`created_by`, `entry_type`) is the audit trail.

## Tests
- The correlation filter sets the response header and clears the MDC.
- The masking helper is unit-tested for every format, `null` and short values.
