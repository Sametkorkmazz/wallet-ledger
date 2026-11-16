# Versioning and release

## Three versions, never mixed
| What | Format | Changes when |
|---|---|---|
| Application | SemVer `MAJOR.MINOR.PATCH` | Every production release |
| API | Path segment `/api/v1` | Only on a breaking API change ([api.md](06-api.md#versioning)) |
| Database | `V<n>__…` migration | Every schema change |

## SemVer from the commit history
- `PATCH`: only `fix` and `perf` commits. `MINOR`: at least one `feat`. `MAJOR`: a breaking change (`!` in the type, or a `BREAKING CHANGE:` footer).
- Because commits follow Conventional Commits ([git.md](09-git.md)), the next version and the changelog come from the history instead of guesswork.

## Know what is running
- The build embeds its version and Git commit (`spring-boot-maven-plugin` `build-info`, `git-commit-id-maven-plugin`); `/actuator/info` shows them and the app logs them at startup.
- Tags: `v1.5.0` on the commit; images tagged with the version **and** `git-<sha>`. Never deploy `latest`.

## Build once, promote the same artifact
- CI builds the JAR and the container image once; every environment deploys that exact image. No rebuilds per environment.
- Image: multi-stage build, slim JRE, non-root user, pinned base image ([docker.md](10-docker.md)).
- `server.shutdown: graceful` + readiness probe → rolling deploys without downtime.

## Dependencies
- Spring Boot's BOM manages versions. Overriding one needs a comment with the reason (usually a CVE).
- The Maven wrapper (`./mvnw`) pins Maven for everyone; never a local `mvn`.
- Dependabot opens upgrade PRs; patch upgrades go out with the next release. Plan one Spring Boot minor upgrade a year.

## Release checklist
- [ ] CI green on this exact commit, and the post-deploy Postman run green in the test environment
- [ ] `CHANGELOG.md` updated
- [ ] Migrations are backward compatible; they run before the new app version starts
- [ ] Config and secrets for production set **before** deploy
- [ ] Unfinished features switched off
- [ ] Rollback plan: previous image tag noted

## Rollback
- App: redeploy the previous image. This works **because** migrations are backward compatible.
- Feature: switch the flag off.
- Database: never "down" migrations in production. Fix forward.

## CHANGELOG.md
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format, newest first, grouped by version with issue numbers:
```markdown
## [1.5.0] - 2026-11-20
### Added
- Idempotent transfers (#55)
### Fixed
- Balance of an account without lines (#62)
```
