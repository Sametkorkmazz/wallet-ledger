# API: endpoints, DTOs and errors

## Endpoints
- Paths: see [naming.md](02-naming.md#rest). Every path starts with `/api/v1`.
- Controllers are thin: map HTTP to one service call, pick the status. No `if` about business data, no `try/catch`.
- Return the DTO directly; use `ResponseEntity` only for headers or a non-200 status.
- Records are never deleted through the API. A ledger is append-only: corrections are reversals.

| Situation | Status |
|---|---|
| Read or update OK | `200` |
| Created | `201` + `Location` header |
| Done, no body | `204` |
| Request shape invalid | `400` |
| Not signed in / not allowed | `401` / `403` |
| Not found (or not yours) | `404` |
| Conflict: version, request still running | `409` |
| Business rule broken | `422` |
| Our bug | `500`, never thrown on purpose |

## DTOs
- **Records**, one request DTO per operation (`CreateTransferRequest`), even if two look alike today.
- Validate everything that comes in with Bean Validation; business rules stay in the domain.
- A create request never carries `id`, `status`, `createdAt` or `createdBy`. The server decides.
- **Money is a decimal string** (`"108.37"`) in requests and responses, because JavaScript numbers are doubles (ADR 4).
- Dates are ISO-8601 UTC strings (`2026-11-16T10:15:30Z`).
- Unknown JSON fields are ignored (`spring.jackson.deserialization.fail-on-unknown-properties: false`), so either side can add fields first.
- Mappers go one way: domain → response DTO. New domain objects are built through their factories, never by a mapper.

## Errors: one shape, RFC 9457 Problem Details
```json
{ "type": "https://wallet.example/problems/entry-not-balanced", "title": "Entry does not balance",
  "status": 422, "detail": "Entry does not balance for EUR", "errorCode": "LEDGER-422-001",
  "correlationId": "4f1c2a9e7b3d4c51" }
```
- One `@RestControllerAdvice` turns exceptions into this shape; validation errors add `fieldErrors`.
- One handler per error category, not per exception: `NotFoundException` → 404, `LedgerRuleException` → 422, conflict → 409. A new exception extends its category's base class and brings its own `errorCode`; the advice doesn't change.
- Every error has a stable `errorCode`. Messages may change, codes may not.
- Never return stack traces, SQL or class names (`server.error.include-stacktrace: never`).
- 4xx are logged at `WARN` without a stack trace, 5xx at `ERROR` with one.
- "Not found" and "not yours" give the same answer, so ids can't be probed.

## Lists
- Always paged: a `PageResponse<T>(content, page, size, totalElements, totalPages)` record, never a raw list or Spring's `Page`.
- Default size 20, maximum 100 (`spring.data.web.pageable.max-page-size`). Pages start at 0.
- Every list sets a default sort that ends with the unique `id`, so rows don't repeat or vanish between pages.
- Only whitelisted fields can be sorted; anything else is a 400.
- Large or growing tables: `Slice` (no count query) or keyset paging (`WHERE id > :lastId`).

## Idempotency
Every request that moves money sends an `Idempotency-Key` header: same key and body → the stored response is replayed; same key, different body → 422. Full rules: ADR 2.

## OpenAPI
- `@Tag` on the class, `@Operation(summary = …)` on every method, `@ApiResponse` only for statuses that endpoint can really return. Shared 400/500 go on the class once.
- Describe fields on the DTO with `@Schema(description, example)`.
- The generated spec is committed (`docs/api/openapi.yaml`), so every PR shows the contract diff.
- Swagger UI is on locally and in test environments, off in production.

## Versioning
- Within `v1`, only add: endpoints, optional request fields, response fields, error codes.
- **Breaking** (removing or renaming a field, changing a type, making a field required) → `/api/v2`, only for the endpoints that changed; both versions share the service layer.
- When `v2` ships, `v1` responses carry `Deprecation`, `Sunset` and `Link: rel="successor-version"` headers. `v1` is removed on the sunset date, once its traffic is zero.
