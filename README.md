# Kafka Schema Registry Spring Boot Demo

A runnable end-to-end example of **Spring Boot + Apache Kafka + Confluent Schema Registry + Avro + PostgreSQL**.

The demo accepts a user over HTTP, serializes it as a generated Avro record, publishes it to Kafka, consumes it in a separate Spring Boot application, and persists it to PostgreSQL.

## Architecture

```text
POST /users
    |
    v
Spring Boot Producer
    |
    | Avro + Schema Registry
    v
Kafka topic: users.v1
    |
    v
Spring Boot Consumer
    |
    v
PostgreSQL: users.contact
```

The producer uses the logical user id as the Kafka record key, so events for the same user are routed consistently. The consumer persists by logical `userId` and reuses the existing database row on duplicate delivery, demonstrating a simple idempotent-consumer pattern.

## What the repository demonstrates

- Java 21 and Spring Boot
- Spring for Apache Kafka producer and consumer
- Generated Avro classes from `common-schemas/src/main/avro/User.avsc`
- Confluent Schema Registry serialization/deserialization
- Local Kafka, Schema Registry and PostgreSQL with Docker Compose
- Confluent Cloud configuration using environment variables
- Bean Validation at the producer REST boundary
- PostgreSQL persistence with Spring Data JPA
- Duplicate-event protection by logical user id
- Unit tests for producer validation/service logic and consumer persistence behavior
- GitHub Actions build plus a real end-to-end Kafka verification

## Repository layout

```text
common-schemas/   Avro schema and generated model
producer-app/     REST API and Kafka producer
consumer-app/     Kafka consumer and PostgreSQL persistence
docker/           Kafka, Schema Registry and PostgreSQL
docker/postgres/  Database initialization
postman/          Postman collection
scripts/          Automated E2E verification
```

## Prerequisites

- Java 21
- Maven 3.9+
- Docker with Docker Compose v2
- `curl`

## Run locally

### 1. Start infrastructure

```bash
docker compose -f docker/docker-compose.yml up -d --wait
```

Local endpoints:

- Kafka: `localhost:29092`
- Schema Registry: `http://localhost:8081`
- PostgreSQL: `localhost:5432`
- PostgreSQL database: `users`
- PostgreSQL user/password: `kafka` / `kafkaConfluent`

The local password is intentionally a demo credential. Do not reuse it outside this local environment.

### 2. Build the project

```bash
mvn clean verify
```

### 3. Start the consumer

```bash
mvn -pl consumer-app -am spring-boot:run
```

The consumer listens to `users.v1` and persists records into `users.contact`.

### 4. Start the producer

In another terminal:

```bash
mvn -pl producer-app -am spring-boot:run
```

The producer exposes `POST http://localhost:8080/users`.

### 5. Produce an event

```bash
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{
    "id": "u-100",
    "email": "ada@example.com",
    "phone": "2101234567",
    "firstName": "Ada",
    "lastName": "Lovelace",
    "isActive": true,
    "age": 28
  }'
```

The flow is:

```text
HTTP -> producer -> Avro serializer -> Schema Registry -> Kafka
     -> Avro deserializer -> consumer -> PostgreSQL
```

The Schema Registry subject created by the local producer is `users.v1-value`.

## Automated E2E verification

The repository includes a real smoke/E2E scenario:

```bash
bash scripts/verify-e2e.sh
```

It automatically:

1. starts Kafka, Schema Registry and PostgreSQL,
2. builds all Maven modules,
3. starts the producer and consumer applications,
4. publishes a real HTTP request,
5. verifies that the Avro event reaches Kafka and is persisted by the consumer,
6. verifies that `users.v1-value` exists in Schema Registry,
7. sends the same logical user again and verifies that only one database row exists.

GitHub Actions runs both `mvn verify` and this E2E scenario for pull requests and pushes to `master`.

## Request validation

The producer validates the HTTP payload before mapping it to Avro. `id` and `email` are required, email must be syntactically valid, and `age` cannot be negative.

Example invalid request:

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"id":"u-101","email":"not-an-email","age":-1}'
```

The application returns HTTP `400` with structured field errors and does not publish the invalid record to Kafka.

## Schema evolution

The Avro schema lives at:

```text
common-schemas/src/main/avro/User.avsc
```

For backward-compatible evolution, add fields with suitable defaults or make them nullable as appropriate, regenerate/build the model, and register/deploy the compatible schema version.

The local profile uses `auto.register.schemas=true` for convenience. The Confluent Cloud producer profile uses:

```text
auto.register.schemas=false
use.latest.version=true
latest.compatibility.strict=true
```

This keeps schema registration out of the application in higher environments and makes CI/CD or a dedicated schema-management process responsible for registration.

## Confluent Cloud

Copy the environment template and fill in your own credentials locally:

```bash
cp .env.example .env
```

Required Kafka and Schema Registry variables:

```bash
export CLOUD_BOOTSTRAP_SERVERS='pkc-xxxxx.region.provider.confluent.cloud:9092'
export CLOUD_API_KEY='<kafka-api-key>'
export CLOUD_API_SECRET='<kafka-api-secret>'
export SR_URL='https://xxxxx.region.provider.confluent.cloud'
export SR_API_KEY='<schema-registry-api-key>'
export SR_API_SECRET='<schema-registry-api-secret>'
```

Optional database overrides used by the cloud consumer profile:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/users'
export DB_USERNAME='kafka'
export DB_PASSWORD='kafkaConfluent'
```

Run the applications with the `cloud` profile:

```bash
mvn -pl consumer-app -am spring-boot:run -Dspring-boot.run.profiles=cloud
mvn -pl producer-app -am spring-boot:run -Dspring-boot.run.profiles=cloud
```

The cloud profile uses SASL/SSL for Kafka and Schema Registry API-key authentication. Secrets are not committed to the repository; `.env` files are ignored.

## PostgreSQL model

The database is initialized from `docker/postgres/init.sql`. The logical event id is stored in `userid`, which is `NOT NULL` and `UNIQUE`. The JPA primary key remains a generated `BIGINT`.

That distinction lets the consumer detect redelivery of the same logical user and update the existing row instead of blindly inserting a duplicate.

## Postman

Import:

```text
postman/kafka-schema-registry-spring-demo.postman_collection.json
```

and run the Create User request after starting the local stack and both applications.

## Related project: fail-fast contract validation

For startup-time Schema Registry contract enforcement in Spring Boot, see:

- `spring-kafka-contract-starter`: https://github.com/mathias82/spring-kafka-contract-starter
- focused E2E contract demo: https://github.com/mathias82/spring-kafka-contract-demo

The starter complements this practical application demo by validating expected subjects, compatibility modes and schemas before an application starts serving traffic.

## Contributing

Issues and pull requests are welcome. If the demo is useful, a GitHub star helps other Kafka/Spring developers discover it.
