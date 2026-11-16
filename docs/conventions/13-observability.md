# Metrics, dashboards and alerts

## Three signals
| Signal | Answers | Tool |
|---|---|---|
| Metrics | How much, how fast, how often? | Micrometer → Prometheus → Grafana |
| Logs | What exactly happened? | JSON logs → Loki (or CloudWatch) → Grafana |
| Traces | Where did the time go in one request? | OpenTelemetry → Tempo → Grafana |
They are linked by `traceId`: from a slow point on a chart to its trace, from the trace to its log lines.

## Setup
- `spring-boot-starter-actuator` + `micrometer-registry-prometheus`.
- Expose only `health`, `info`, `prometheus`, on a separate management port that isn't public.
- Liveness and readiness probes on (`management.endpoint.health.probes.enabled: true`).
- `/actuator/info` shows the version and Git commit: the first question in every incident is "what is running?"
- Tag every metric with `application`, and turn on `percentiles-histogram` for `http.server.requests`, so p95 per endpoint works.

## HTTP metrics come free
Spring times every request as `http_server_requests_seconds` with `method`, `uri` (the route template), `status` and `outcome`.

```promql
# requests per second, per endpoint
sum by (method, uri) (rate(http_server_requests_seconds_count{uri!~"/actuator.*"}[5m]))
# p95 per endpoint
histogram_quantile(0.95, sum by (le, method, uri) (rate(http_server_requests_seconds_bucket{uri!~"/actuator.*"}[5m])))
# server error rate per endpoint, %
100 * sum by (method, uri) (rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[5m]))
    / sum by (method, uri) (rate(http_server_requests_seconds_count[5m]))
```
Use percentiles, not averages: the average describes nobody.

## Business metrics
- Added where the business event happens: `wallet.transfer.posted`, `wallet.idempotency.replayed`, `wallet.job.duration`.
- Counters for events, timers for durations, gauges for "how many right now".
- **Labels have few possible values** (`entryType`, `currency`, `result`). Never an id, e-mail, IBAN or amount.
- **Never record money amounts as metrics.** Counts yes, amounts belong in the database.
- Unit-tested with `SimpleMeterRegistry`.

## One standard dashboard
| Row | Panels |
|---|---|
| Overview | Requests/s, 5xx %, p95 and p99, instances up, version running |
| Endpoints | Table per `method + uri`: requests/s, error %, p95, p99, sorted by p95 |
| Business | Transfers posted/min, idempotent replays, job duration and last success |
| Dependencies | DB pool active/pending, slow repository calls, outbound HTTP p95 |
| JVM | Heap, GC pauses, CPU, busy threads, restarts |
| Logs | Live ERROR lines |

Variables `env` and `uri` at the top; deploys marked as annotations. The dashboard JSON lives in the repo (`observability/dashboards/`) and changes through PRs.

## Alerts
Alert on what users feel, each with a runbook link:

| Alert | Example condition |
|---|---|
| High error rate | 5xx > 1% for 5 min |
| Slow endpoint | p95 > 1 s for 10 min |
| DB pool starving | pending connections > 0 for 5 min |
| Job failed or late | no success within its window |
| Restarts | > 2 in 15 min |

Tune thresholds after two weeks of real data. An alert nobody acts on gets deleted.

## Targets (p95)
Read one item < 300 ms · paged list < 500 ms · create/transfer < 800 ms.
