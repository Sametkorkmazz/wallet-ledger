# Documentation and diagrams

## Docs as code
Docs that live next to the code are reviewed with the code and stay true.

| In the repo | Elsewhere (wiki), linking to the repo |
|---|---|
| How the code is built, tested, released (these conventions) | Business requirements, sign-offs |
| Architecture and flow diagrams | Runbooks for support |
| ADRs | Meeting notes, roadmap |
| OpenAPI contract, Postman collection | |

Never copy repo docs to a wiki. Link to them; two copies means one is wrong.

## What a change must update
| You changed… | Update in the same PR |
|---|---|
| An endpoint or DTO | OpenAPI (generated), Postman, sequence diagram if the flow changed |
| A business rule | Tests; the domain Javadoc if the rule isn't obvious |
| A schema | Migration, ER diagram in the README |
| A decision with trade-offs | A new ADR |
| Config keys | `application*.yml` and the [configuration](11-configuration.md) checklist |
| A convention | The file in `docs/conventions`, in the PR that first follows it |

## README
The root README answers only: what is this, how do I run it, how do I test it, where are the docs, what's the status. Everything else lives in `docs/`.

## Diagrams
| Need | Tool |
|---|---|
| Default, in Markdown | **Mermaid**: plain text, diffable, rendered by GitHub. Check it on mermaid.live |
| Free-form drawings | draw.io saved as `*.drawio.svg`: renders as an image, stays editable |

Draw at the level that answers the question (C4 model):

| Level | Question | Example |
|---|---|---|
| System context | Who uses us, what do we depend on? | User → wallet → bank rails |
| Container | Which deployables and data stores? | React app, API, PostgreSQL, Kafka |
| Component | Which packages inside the service? | api → service → domain / persistence |
| Dynamic | How does one request flow? | Sequence diagram of `POST /transfers` |
| Data | Which tables and relations? | ER diagram in the README |

## ADRs
- One page in `docs/adr/NNNN-title.md`: **Context, Decision, Consequences (gains and costs), Alternatives considered**, with a status (Proposed, Accepted, Superseded by NNNN) and a date.
- Write one when you add a library, choose a pattern (locking, outbox), deviate from these conventions, or make a choice someone will later ask "why?" about.
- An accepted ADR is never rewritten. A new ADR supersedes it.
- Numbers are never reused; a reserved number is noted in the list.

## Code comments and Javadoc
- Javadoc on public domain types and methods with rules: what is allowed, what throws.
- Comments explain **why**, never what the code already says.
- A `TODO` names the issue: `// TODO(#52): funds check`.
