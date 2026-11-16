# Postman

Postman is the shared, runnable API documentation: every endpoint has a request with examples, and every request has tests that CI runs with Newman.

## Where things live
```
postman/
├── wallet-ledger.postman_collection.json   ← exported collection (v2.1), committed
└── environments/
    ├── local.postman_environment.json
    └── <env>.postman_environment.json       ← baseUrl and non-secret values only
```
- The committed JSON is the source of truth that CI runs and reviewers see.
- Changed a request in Postman? Export it and commit it **in the same PR** as the code change.
- If cloud workspaces aren't allowed, use Postman offline with the Git files, or Bruno.

## Structure
```
Wallet Ledger API
├── 00 Smoke (read-only: safe for production)   health, info, list accounts
├── 01 Accounts
├── 02 Transfers
│   ├── Create transfer                 ← saves {{entryId}} for the next requests
│   ├── Create transfer: replay, same key → same response
│   ├── Create transfer: same key, other body → 422
│   └── Errors                          unbalanced → 422 · unknown account → 404 · bad amount → 400
└── 99 Cleanup (test environments only)
```
Requests in a folder run in order and form a scenario.

## Variables
| Variable | Where |
|---|---|
| `baseUrl` | environment |
| `token` | environment, **current value only** (never the initial value), or Postman Vault |
| `entryId`, `accountId` | set by test scripts at run time |
| `idempotencyKey` | pre-request script: `{{$guid}}` |

Initial values are exported; current values stay on your machine. CI passes secrets on the command line.

## Scripts
```javascript
// Collection pre-request: correlation id on every call, a fresh key on every money-moving POST
pm.request.headers.upsert({ key: 'X-Correlation-Id', value: 'postman-' + pm.variables.replaceIn('{{$guid}}') });
if (pm.request.method === 'POST' && !pm.request.headers.has('Idempotency-Key')) {
    pm.request.headers.upsert({ key: 'Idempotency-Key', value: pm.variables.replaceIn('{{$guid}}') });
}
```
```javascript
// Request tests: status, contract, the money rule
pm.test('201 Created', () => pm.response.to.have.status(201));
pm.test('Lines sum to zero', () => {
    const cents = pm.response.json().lines.map(l => Math.round(Number(l.amount) * 100));
    pm.expect(cents.reduce((a, b) => a + b, 0)).to.eql(0);
});
pm.collectionVariables.set('entryId', pm.response.json().id);
```
A collection-level test checks every error is `application/problem+json` with an `errorCode`, and that the correlation id is echoed.

## Documentation inside Postman
Each request has a description (what it does, which rules apply), saved examples for success and each error (the same JSON files as the controller tests in `src/test/resources/requests/`), and synthetic data only.

## Running in CI
```bash
npx newman run postman/wallet-ledger.postman_collection.json \
  -e postman/environments/test.postman_environment.json \
  --env-var "token=${API_TOKEN}" --reporters cli,junit --reporter-junit-export target/newman.xml
```
After a deploy to a test environment: everything. Production: `--folder "00 Smoke (read-only: safe for production)"` only. Never create real data in production.

## Kept in sync with the contract
The build regenerates `docs/api/openapi.yaml` → update the collection from it → add tests → export and commit.
