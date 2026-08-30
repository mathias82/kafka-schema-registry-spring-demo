# Kafka Schema Registry Spring Boot Demo (Avro Producer & Consumer with PostgreSQL)

A practical, runnable **end-to-end Spring Boot example** using **Apache Kafka**, **Confluent Schema Registry**, **Avro serialization** and **PostgreSQL persistence**.

This repository demonstrates a real event-driven flow: a REST request enters a Spring Boot producer, is validated and mapped to Avro, published to Kafka using Schema Registry, consumed by a separate Spring Boot application and persisted to PostgreSQL.

---

## What This Demo Includes

- Spring Boot Kafka producer using Avro
- Spring Boot Kafka consumer deserializing Avro messages
- Confluent Schema Registry integration
- PostgreSQL persistence using Spring Data JPA
- Schema evolution and compatibility concepts
- Local development using Docker Compose
- Confluent Cloud support through the `cloud` Spring profile
- Bean Validation at the producer REST boundary
- Idempotent consumer behavior by logical user id
- Unit tests for producer and consumer behavior
- GitHub Actions build and real end-to-end verification

---

## Who Should Use This Project

This demo is useful for developers who want to:

- Learn Kafka Schema Registry with Spring Boot
- Understand Avro serialization and deserialization in Kafka
- Build producer/consumer pipelines backed by PostgreSQL
- Understand practical schema evolution
- Create a reproducible local Kafka development environment
- See a complete REST → Kafka → database flow rather than isolated snippets

---

## ✨ Key Features

- **Java 21 + Spring Boot** producer and consumer applications.
- **Confluent Schema Registry** integration with Avro serialization/deserialization.
- **PostgreSQL** persistence using Spring Data JPA and the `users.contact` table.
- **Schema evolution** with compatibility-aware configuration.
- **Docker Compose** stack for Kafka, Zookeeper, Schema Registry and PostgreSQL.
- **Confluent Cloud** support using environment variables and SASL/SSL.
- **Request validation** before invalid data reaches Kafka.
- **Stable Kafka keys** using the logical user id.
- **Idempotent persistence** so duplicate delivery for the same logical user does not create duplicate database rows.
- **Automated E2E verification** covering the complete application flow.

---

## 🏗️ Architecture Overview

```text
                            ┌─────────────┐
                            │ Postman/curl│
                            │   client    │
                            └──────┬──────┘
                                   │ HTTP POST /users
                                   ▼
                        ┌─────────────────────┐
                        │ Spring Boot Producer│
                        │ REST + Validation   │
                        │ + Avro              │
                        └─────────┬───────────┘
                                  │ users.v1
                                  │ Avro + Schema Registry
                                  ▼
                        ┌─────────────────────┐
                        │    Apache Kafka     │
                        └─────────┬───────────┘
                                  │
                                  ▼
                        ┌─────────────────────┐
                        │ Spring Boot Consumer│
                        │ Avro + Spring Data  │
                        │ JPA                 │
                        └─────────┬───────────┘
                                  │
                                  ▼
                              PostgreSQL
```

1. The **producer** exposes `POST /users`. It validates a `UserCreateRequest`, converts it to the generated Avro `User` record and publishes it to `users.v1`.
2. The **Schema Registry** stores the Avro schema used by the Kafka serializer/deserializer and provides the foundation for compatibility-aware schema evolution.
3. The producer uses the logical user id as the **Kafka record key**, giving stable partition routing for events belonging to the same user.
4. The **consumer** listens to `users.v1`, deserializes the Avro record, maps it to `UserEntity` and persists it to PostgreSQL.
5. If the same logical user is delivered again, the consumer reuses the existing database row instead of blindly creating another one.

This separation keeps producer and consumer loosely coupled while giving the event payload an explicit schema contract.

---

## 📁 Repository Layout

```text
common-schemas/   Avro schema and generated model
producer-app/     REST API, validation and Kafka producer
consumer-app/     Kafka consumer and PostgreSQL persistence
docker/           Kafka, Schema Registry and PostgreSQL stack
docker/postgres/  Database initialization
postman/          Postman collection
scripts/          Automated real E2E verification
.github/workflows CI build and E2E workflow
```

---

## 🗄️ Database Schema

The consumer stores events in `users.contact`. The database is initialized from `docker/postgres/init.sql`:

```sql
DROP TABLE IF EXISTS users.contact;
CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE users.contact (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    userid TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL,
    phone TEXT,
    first_name TEXT,
    last_name TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TEXT,
    age INTEGER
);

CREATE INDEX idx_users_created_at ON users.contact (created_at DESC);
CREATE INDEX idx_users_email ON users.contact (email);
CREATE INDEX idx_users_is_active ON users.contact (is_active);
```

The generated `id` is the JPA/database primary key. `userid` stores the logical identifier from the Avro `id` field and is unique, which allows the consumer to handle redelivery idempotently.

---

## Prerequisites

- Java 21
- Maven 3.9+
- Docker with Docker Compose v2
- `curl`

---

## 🐳 Full Local Stack

The project includes Docker Compose services for:

- Apache Kafka
- Zookeeper
- Confluent Schema Registry
- PostgreSQL

### Start the infrastructure

```bash
docker compose -f docker/docker-compose.yml up -d --wait
```

Local endpoints:

- Kafka: `localhost:29092`
- Schema Registry: `http://localhost:8081`
- PostgreSQL: `localhost:5432`
- Database: `users`
- PostgreSQL user/password: `kafka` / `kafkaConfluent`

The PostgreSQL password is intentionally a **local demo credential**. Do not reuse it outside this environment.

### Build the project

```bash
mvn clean verify
```

### Run the consumer

```bash
mvn -pl consumer-app -am spring-boot:run
```

The consumer runs on port `8089` by default and listens to `users.v1`.

### Run the producer

In another terminal:

```bash
mvn -pl producer-app -am spring-boot:run
```

The producer runs on port `8080` and exposes `POST /users`.

---

## 🚀 Produce a User Event

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

The verified runtime flow is:

```text
HTTP POST
   → producer validation
   → generated Avro User
   → Avro serializer
   → Schema Registry
   → Kafka users.v1
   → Avro deserializer
   → consumer
   → Spring Data JPA
   → PostgreSQL users.contact
```

For the local producer, the Schema Registry subject is `users.v1-value`.

---

## ✅ Request Validation

The producer validates the HTTP payload before mapping and publishing it. `id` and `email` are required, email must be syntactically valid and `age` cannot be negative.

Example invalid request:

```bash
curl -i -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"id":"u-101","email":"not-an-email","age":-1}'
```

The application returns HTTP `400` with structured field errors and does **not** publish the invalid request to Kafka.

---

## 🧪 Automated Testing & Real E2E CI

Unit tests cover producer controller/service behavior and consumer persistence/idempotency behavior.

For a real integration smoke test, run:

```bash
bash scripts/verify-e2e.sh
```

The script automatically:

1. starts Kafka, Schema Registry and PostgreSQL,
2. builds all Maven modules,
3. starts the producer and consumer applications,
4. sends a real HTTP request to the producer,
5. verifies that the Avro event travels through Kafka and is persisted by the consumer,
6. verifies that `users.v1-value` exists in Schema Registry,
7. sends the same logical user again,
8. verifies that duplicate delivery does not create a second database row.

GitHub Actions runs:

```bash
docker compose -f docker/docker-compose.yml config
mvn -B verify
bash scripts/verify-e2e.sh
```

This means the repository now continuously verifies the actual REST → Avro → Schema Registry → Kafka → consumer → PostgreSQL path, rather than only documenting how it should work.

---

## 🔄 Schema Evolution

The Avro schema lives at:

```text
common-schemas/src/main/avro/User.avsc
```

A typical backward-compatible change is adding an optional field with a suitable default. After changing the schema, rebuild the project to regenerate the Avro Java model and register/deploy the schema according to your environment's schema-management process.

The **local** profile uses automatic registration for convenience.

The **Confluent Cloud producer** profile uses:

```text
auto.register.schemas=false
use.latest.version=true
latest.compatibility.strict=true
```

This keeps schema registration out of the application in higher environments and lets CI/CD or a dedicated schema-management process own registration.

Schema Registry compatibility should be configured for the actual subject used by this demo, for example `users.v1-value` when using the default topic and TopicNameStrategy.

---

## ☁️ Running with Confluent Cloud

Secrets are not committed to the repository. `.env` files are ignored and the repository provides `.env.example` as a template.

```bash
cp .env.example .env
```

Set your own Kafka and Schema Registry credentials:

```bash
export CLOUD_BOOTSTRAP_SERVERS='pkc-xxxxx.region.provider.confluent.cloud:9092'
export CLOUD_API_KEY='<kafka-api-key>'
export CLOUD_API_SECRET='<kafka-api-secret>'
export SR_URL='https://xxxxx.region.provider.confluent.cloud'
export SR_API_KEY='<schema-registry-api-key>'
export SR_API_SECRET='<schema-registry-api-secret>'
```

Optional PostgreSQL overrides for the cloud consumer profile:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/users'
export DB_USERNAME='kafka'
export DB_PASSWORD='kafkaConfluent'
```

Run with the `cloud` profile:

```bash
# Consumer
mvn -pl consumer-app -am spring-boot:run -Dspring-boot.run.profiles=cloud

# Producer (another terminal)
mvn -pl producer-app -am spring-boot:run -Dspring-boot.run.profiles=cloud
```

The cloud profile uses SASL/SSL for Kafka and Schema Registry API-key authentication.

---

## 📸 Demo Screenshots

The original practical walkthrough screenshots are preserved below.

### Producer logs

Producer publishing a user to `users.v1` using Avro and Schema Registry:

<img width="2048" height="604" alt="Producer logs publishing an Avro Kafka event" src="https://github.com/user-attachments/assets/db7f8292-9673-4f3f-bf04-a9bce8c4d1b4" />

### Consumer logs

Consumer receiving the Avro record and persisting it to `users.contact`:

<img width="2048" height="606" alt="Consumer logs receiving and persisting an Avro Kafka event" src="https://github.com/user-attachments/assets/f883fec5-b933-47be-88d5-ea3f1c7f17be" />

### Postman request

The repository includes a Postman collection for the Create User request:

<img width="2048" height="744" alt="Postman Create User request for the Kafka demo" src="https://github.com/user-attachments/assets/4f30c6e8-7481-4724-9733-88c05d52fb4e" />

---

## 📬 Postman Collection

Import:

```text
postman/kafka-schema-registry-spring-demo.postman_collection.json
```

Start the infrastructure, consumer and producer, then execute the Create User request to observe the full end-to-end flow.

---

## 🛡️ Related Project: Fail-Fast Kafka Contract Validation

This repository demonstrates the **practical runtime producer/consumer application**. For reusable startup-time Schema Registry contract enforcement in Spring Boot, see the companion projects:

### Spring Kafka Contract Starter

https://github.com/mathias82/spring-kafka-contract-starter

Maven Central artifact:

```xml
<dependency>
    <groupId>io.github.mathias82.spring.kafka</groupId>
    <artifactId>spring-kafka-contract-starter</artifactId>
    <version>0.2.2</version>
</dependency>
```

The starter validates expected Schema Registry subjects, compatibility modes and schemas during Spring Boot startup, allowing contract violations to fail startup before the application starts serving traffic.

### Focused Contract E2E Demo

https://github.com/mathias82/spring-kafka-contract-demo

The focused demo verifies a real producer → Kafka → consumer round trip together with compatible schema evolution and intentional breaking-schema rejection.

Together, the repositories cover complementary concerns:

```text
kafka-schema-registry-spring-demo
    Practical REST → Kafka → PostgreSQL application

spring-kafka-contract-starter
    Reusable Spring Boot startup contract enforcement

spring-kafka-contract-demo
    Focused E2E proof for compatible and breaking schema evolution
```

---

## 🧭 What This Project Teaches

This project provides a practical reference for:

- defining explicit Kafka data contracts with Avro and Schema Registry,
- producing and consuming schema-backed Kafka events with Spring Boot,
- validating REST input before publishing events,
- persisting consumed events to PostgreSQL,
- handling duplicate delivery with a simple idempotent persistence strategy,
- evolving schemas safely,
- configuring a local Kafka development stack,
- connecting the same applications to Confluent Cloud,
- and continuously verifying the complete architecture in CI.

It is intended as a learning and reference implementation. Production systems will usually add further concerns such as authentication/authorization, observability, retries and dead-letter handling, deployment configuration, migrations and environment-specific operational controls.

---

## 🤝 Contributing & Feedback

Contributions, feedback and issues are welcome. Feel free to open a pull request or issue.

If the project is useful, a ⭐ GitHub star helps other Kafka and Spring developers discover it.
