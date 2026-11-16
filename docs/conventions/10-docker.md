# Docker

Docker runs the local PostgreSQL and powers Testcontainers in the tests. Kafka, Redis and an app image come later.

## Rules
- Every service the app needs runs from `docker-compose.yml`, never installed on your machine.
- Each service has a named volume, a health check, and credentials from `.env` with local-only defaults.
- Ports bind to `127.0.0.1` only. Use a non-default host port when a local install could clash (PostgreSQL: `5433`).
- Tests never use the Compose database. Testcontainers starts a throwaway one; Docker just has to be running.
- Image tags are pinned (`postgres:16`, not `latest`), and the same version runs in Compose and in tests.
- A later app image: multi-stage `Dockerfile` (build on a JDK 21 image, run on a slim JRE 21 image as a non-root user).

## Setup
```bash
docker --version && docker compose version
cp .env.example .env          # optional: every value has a default
```
`.env` is read by Compose only, not by Spring Boot. Its credentials are for local development; never reuse them.

## Everyday commands
| Need | Command |
|---|---|
| Start the database | `docker compose up -d postgres` |
| Status (wait for "healthy") | `docker compose ps` |
| Logs | `docker compose logs -f postgres` |
| Stop, keep data | `docker compose stop` or `docker compose down` |
| Full reset, **deletes data** | `docker compose down -v` |
| SQL shell | `docker exec -it wallet-postgres psql -U wallet -d wallet` |
| Disk usage / cleanup | `docker system df`, `docker image prune` |

In `psql`: `\dt` lists tables, `\d journal_entries` describes one, `SELECT * FROM flyway_schema_history;` shows applied migrations, `\q` quits.

Connection with the defaults: `jdbc:postgresql://localhost:5433/wallet`, user `wallet`, password `wallet`. Data lives in the `pgdata` volume and survives restarts and `down`.

## Run the app and tests
```bash
docker compose up -d postgres
cd backend/walletledger
./mvnw test               # Testcontainers, not the Compose database
./mvnw spring-boot:run    # Compose database; Flyway applies migrations
```

## Troubleshooting
| Symptom | Fix |
|---|---|
| `Cannot connect to the Docker daemon` | Start Docker Desktop |
| Port 5433 already in use | `lsof -i :5433`, or change the host side of the port mapping and the JDBC URL |
| Changed user/password has no effect | They only apply to an empty volume: `docker compose down -v`, then `up` |
| Flyway checksum mismatch | An applied migration was edited. Add a new migration instead; locally, reset with `down -v` |
| Testcontainers can't find Docker | Docker must be running; on Docker Desktop allow the default Docker socket (advanced settings) |
| Container `unhealthy` | `docker compose logs postgres` |
