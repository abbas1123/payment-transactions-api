# Payment Transactions API

![CI](https://github.com/abbas1123/payment-transactions-api/actions/workflows/ci.yml/badge.svg)

Banking-style money transfer API built to demonstrate a production-shaped backend stack:
**Spring Boot** REST services on top of **Oracle XE**, with business rules in **PL/SQL packages**,
transaction events streamed through **Kafka**, hot lookups cached in **Redis**, and CI on **GitHub Actions**.

## Architecture

```
                 ┌──────────────────────────┐
  HTTP/JSON      │  Spring Boot API          │
 ───────────────►│  controllers → services   │
  Swagger UI     │                           │
                 │   ┌───────────────────┐   │        ┌──────────────┐
                 │   │ SimpleJdbcCall     │──┼───────►│ Oracle XE     │
                 │   └───────────────────┘   │        │ PKG_TRANSFERS │
                 │       │            │      │        │ (PL/SQL)      │
                 │       ▼            ▼      │        └──────────────┘
                 │   Kafka producer  Redis   │
                 └───────┬───────────cache───┘
                         │
                         ▼
                  transaction-events topic ──► NotificationConsumer
```

The transfer itself is **one atomic PL/SQL call** (`PKG_TRANSFERS.TRANSFER_FUNDS`): both account rows are
locked in deterministic order, status and balance are validated, commission is calculated, balances are
updated and the ledger row is inserted — all inside the database. The Java side maps the outcome to HTTP
semantics, evicts affected cache entries, and publishes a `TransactionEvent` to Kafka.

## Tech stack

| Layer | Technology |
|---|---|
| API | Java 17, Spring Boot 3, springdoc / Swagger UI |
| Business rules | Oracle XE 21c, PL/SQL package (`db/init/02_pkg_transfers.sql`) |
| Data access | Spring Data JPA (reads), `SimpleJdbcCall` (transfer procedure) |
| Messaging | Apache Kafka (spring-kafka), JSON events |
| Caching | Redis via Spring Cache (`@Cacheable`, TTL + explicit eviction) |
| Tests | JUnit 5, Mockito, MockMvc |
| CI | GitHub Actions (`mvn verify` on every push) |

## Run it

Prerequisites: Docker, JDK 17, Maven.

```bash
# 1. Start Oracle XE, Kafka and Redis (first Oracle start takes a minute or two)
docker compose up -d

# 2. Run the API
mvn spring-boot:run
```

Swagger UI: http://localhost:8080/swagger-ui.html

The database is seeded with demo accounts (ids start at 100), so you can immediately try:

```bash
# Transfer 50 AZN from account 100 to 101 (0.5% commission, min 0.10)
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{"fromAccountId": 100, "toAccountId": 101, "amount": 50.00}'

# Cached account lookup
curl http://localhost:8080/api/accounts/100
```

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/accounts` | Open an account |
| GET | `/api/accounts/{id}` | Get account (Redis-cached, 10 min TTL) |
| GET | `/api/accounts/{id}/transactions` | Ledger entries for an account |
| POST | `/api/transfers` | Atomic transfer via PL/SQL, publishes Kafka event |
| GET | `/api/transactions/{id}` | Single ledger entry |

Rejections come back as RFC 7807 problem responses, e.g. `422` with `code: INSUFFICIENT_FUNDS`.

## Design notes

- **Why business rules in PL/SQL?** The balance check, commission and ledger insert must be atomic and
  race-free. Doing it next to the data in one call avoids read-modify-write races and extra round trips;
  the API stays a thin, testable orchestration layer.
- **Deadlock avoidance:** account rows are locked in ascending id order, so two opposite transfers can
  never deadlock each other.
- **Cache strategy:** account reads are cached with a short TTL *and* explicitly evicted after transfers,
  so stale balances are bounded to the rare cross-instance case.
- **Kafka key = source account id** keeps per-account event ordering within a partition.

### Known simplifications

Single currency per transfer (no FX), no idempotency keys, no auth — out of scope for this demo;
each would be the natural next step.
