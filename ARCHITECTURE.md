# EPermit Service (Architecture Breakdown)

The EPermit Service is a microservice that handles permit applications across multiple government
ministry tenants. It enforces strict data isolation between ministries at the database level,
processes a payment charge on every application with built-in protection against payment gateway
failures, and notifies the rest of the platform via a message broker each time a permit is saved.
The entire system starts with a single `docker-compose up` command and requires no manual
configuration from the reviewer.

---

## 1. Multi-Tenancy Strategy

Each ministry is a tenant, and the core requirement is that one ministry must never be able to see
another ministry's permits. This isolation is enforced at the database level using PostgreSQL
**Row-Level Security (RLS)** on a shared set of tables. The alternative approach, giving each
ministry its own separate database schema, was considered and rejected because it adds significant
operational complexity without providing any stronger isolation guarantee.

When RLS is enabled on a table, PostgreSQL filters every query automatically based on a session
variable called `app.current_tenant`. The policy simply checks that the `tenant_id` column on a
row matches this variable. If the variable is not set, the policy matches nothing by default, so
an unauthenticated or misconfigured request sees zero rows rather than all rows.

Populating this variable correctly is what makes the entire isolation strategy work. Every time the
application checks out a database connection from the pool, a custom wrapper called
`TenantAwareDataSource` immediately sets `app.current_tenant` to the current tenant value using a
session-level `SET` command. It is important that this is a session-level `SET` and not `SET LOCAL`.
`SET LOCAL` would only hold the value for the duration of a single transaction, but because this
command runs before Spring has had a chance to configure the connection, the value would revert
almost immediately and RLS would receive no tenant context. Using `SET` keeps the value alive for
the full lifetime of the connection and overwrites it on every checkout, which means there is no
risk of one request's tenant context leaking into another request.

The tenant value itself comes from the `X-Tenant-ID` request header, which every API call must
include. A filter called `TenantValidationFilter` checks this header on every incoming request. If
the header is missing or contains an unrecognised tenant value, the request is rejected immediately
with a `400 Bad Request` before it touches any business logic or database. If the header value does
not match the tenant on the authenticated user's account, the request is rejected with a
`403 Forbidden`. Valid tenant values are `Ministry_Health` and `Ministry_Education`. The filter
always cleans up the tenant context after each request to prevent it from accidentally carrying
over to the next request on the same thread.

On the schema side, **Flyway** manages all database migrations rather than letting Hibernate
auto-generate the schema. Hibernate's schema generation tools cannot execute PostgreSQL-specific
commands like `ENABLE ROW LEVEL SECURITY` or `CREATE POLICY`, so if Hibernate owned the schema,
the entire RLS setup would never be applied. Flyway runs on every startup and ensures the RLS
policies are always in place. Hibernate is configured to only validate that the schema matches the
application's entity definitions, never to modify it.

The `GET /api/permits/summary` endpoint fetches all permits along with their attached documents in
a single database query using a `JOIN FETCH`. Without this, the application would run one query to
fetch the list of permits and then a separate query for each permit's documents, which becomes
increasingly expensive as data grows. A child entity called `PermitDocument` was introduced
specifically to demonstrate this pattern, since the spec required it to be documented and a
permit with no child records creates no meaningful example to show.

---

## 2. Mocked Service Failures and Resilience

Every permit application triggers a payment charge to an external payment gateway. Since no real
gateway is available in this environment, a mock payment controller is included inside the
application at `POST /internal/payment/charge`. It always waits 3 seconds before responding, then
fails with a `503 Service Unavailable` error 30% of the time and succeeds with a payment reference
the other 70%. This behaviour simulates the kind of unreliable external dependency the real service
would face. An internal controller was used instead of a separate tool like WireMock because it
proves the same behaviour without needing an additional Docker container or configuration file.

To handle these failures gracefully, the payment call is protected by two Resilience4j mechanisms.
First, a **Retry** policy attempts the payment up to 3 times with a 1 second pause between each
attempt before giving up. Second, a **Circuit Breaker** monitors the overall failure rate and, if
more than 50% of calls across a window of 5 attempts fail, it stops sending requests to the gateway
for 10 seconds to give it time to recover. The retry runs first, and only after it is exhausted
does the circuit breaker record the failure. In the worst case this means a payment call can take
roughly 11 seconds before the fallback kicks in.

The fallback always returns a `FAILED` payment status and never throws an error. This means a
permit application is always saved to the database and always returns a `201 Created` response.
Payment failure is recorded as a status on the permit record rather than treated as a reason to
reject the request.

The `createPermit` method does not use a database transaction across its full execution. Doing so
would hold a database connection open for the entire duration of the payment call, which could be
up to 11 seconds under retries. Under concurrent load this would exhaust the connection pool. Each
individual database save in the method runs its own short transaction and releases the connection
immediately.

---

## 3. Event Publishing Pattern

After a permit is saved and the payment status is recorded, the application publishes a
`PermitCreatedEvent` to RabbitMQ. This event carries the permit ID, tenant ID, applicant email,
permit type, amount, and timestamp. It is sent to a topic exchange called `permit.exchange` with
the routing key `permit.created`, where it is delivered to a durable queue called
`permit.created.queue`. Any downstream service that needs to react to new permits, such as a
notification service or a reporting tool, can consume from this queue independently.

The publish is best-effort. If RabbitMQ is unavailable or the publish fails for any reason, the
error is logged and the application moves on. The permit has already been saved to the database
before the publish is attempted, so a messaging failure never affects the permit creation itself.

The **Transactional Outbox Pattern** was considered as a more reliable alternative. That pattern
works by writing the event into a database table within the same transaction as the permit record
and then relaying it to the broker asynchronously, which guarantees the event is never lost even if
the broker is down at the moment of creation. It was deliberately excluded from this implementation
because no downstream consumer is defined within the scope of this service. Building the full
outbox infrastructure without a consumer to benefit from it would be unnecessary complexity. It is
the correct next step to implement when a real consumer is introduced.