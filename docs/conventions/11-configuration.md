# Configuration and environments

## Files: one base, thin overrides
```
application.yml          ← every key, with safe defaults
application-local.yml    ← developer machine: text logs, DEBUG, SQL logging
application-<env>.yml    ← only what differs in that environment
```
- Precedence: environment variables → `application-<profile>.yml` → `application.yml`.
- The active profile comes from the platform (`SPRING_PROFILES_ACTIVE`), never from a YAML file.

## What goes where
| Value | Where |
|---|---|
| Same everywhere (Jackson, paging limits, batch size) | `application.yml` |
| Differs per environment, not secret (log level, flags, timeouts) | `application-<env>.yml` |
| Hosts, URLs | Environment variable: `${DB_HOST}` |
| **Secrets** | **Secret store only** (e.g. AWS Secrets Manager) → environment variable |
| Developer-only | `application-local.yml`, or a git-ignored `.env` |

A default after the colon (`${POSTGRES_PASSWORD:wallet}`) is only for local development; production must set the variable.

## Typed and validated
- Every custom key is bound to a record: `@ConfigurationProperties(prefix = "wallet.idempotency") @Validated`. No `@Value` scattered in the code.
- A bad value stops the app at startup, not at the first request.
- Add `spring-boot-configuration-processor` so the IDE autocompletes and documents the keys.

## Kept in sync by a test
A plain unit test checks on every PR that:
- every key in an environment file also exists in `application.yml` (catches typos and missing defaults);
- secret-looking keys (`password`, `secret`, `token`, `key`) are always `${ENV_VAR}` placeholders;
- every profile binds and validates the `@ConfigurationProperties` records.

## Secrets
- Nothing secret in Git: not in YAML, tests, Postman environments or history. Leaked? **Rotate first**, then clean history.
- `.env.example` lists the variables with dummy values; `.env` is git-ignored.
- A secret scanner (Gitleaks) runs in CI.

## Feature flags
- Risky changes ship switched off (`wallet.feature.<name>: false`), are turned on per environment, and roll back by flipping the flag.
- Remove the flag and the old code path within two releases.

## Checklist for a new key
- [ ] In `application.yml` with a safe default
- [ ] Bound to a validated `@ConfigurationProperties` record
- [ ] Environment overrides added where needed; the config test is green
- [ ] Mentioned in the PR under *Notes*
