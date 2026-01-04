# Kafka Schema Registry Spring Boot Demo (Avro Producer & Consumer with PostgreSQL)

This repository demonstrates how to build an **event-driven Spring Boot microservice** using **Apache Kafka**, **Confluent Schema Registry**, **Avro serialization** and **PostgreSQL persistence**.

---

## What this Demo has

This repository provides a **complete Spring Boot example**
using **Apache Kafka**, **Confluent Schema Registry** and **Avro serialization**.

It demonstrates a **Kafka Avro producer and consumer**
with **PostgreSQL persistence**, showing how schemas are registered,
evolved and consumed in a real-world event-driven application.

---

## What This Demo Includes

- Spring Boot Kafka producer using Avro
- Spring Boot Kafka consumer deserializing Avro messages
- Confluent Schema Registry integration
- PostgreSQL persistence using Spring Data JPA
- Schema evolution with backward compatibility
- Local development using Docker Compose

---

## Who Should Use This Project

This demo is useful for developers who want to:

- Learn Kafka Schema Registry with Spring Boot
- Understand Avro serialization in Kafka
- Build producer/consumer pipelines with PostgreSQL
- Create a local Kafka development environment

---

## ✨ Key Features

- **Kafka producer & consumer** written in Java 21 with Spring Boot.
- **Confluent Schema Registry** integration with Avro serialization/deserialization.
- **PostgreSQL** persistence using Spring Data JPA; includes ready‑made table schema (`users.contact`).
- **Schema evolution** demonstrated with backward compatibility and versioning.
- **Docker Compose** configuration to spin up Kafka, Schema Registry and PostgreSQL locally.
- **Confluent Cloud** support (bring your own API keys) via the `cloud` Spring profile.

---

## 🏗️ Architecture Overview

```
                            ┌─────────────┐
                            │  Postman    │
                            │  (client)   │
                            └──────┬──────┘
                                   │ HTTP POST /users
                                   ▼
                        ┌─────────────────────┐
                        │ Spring Boot Producer│
                        │ (REST + Avro)       │
                        └─────────┬───────────┘
                                  Kafka topic (users.v1)
                        ┌─────────▼───────────┐
                        │   Kafka Brokers     │
                        │   + Schema Registry │
                        └─────────┬───────────┘
                                   │ Avro → User
                                   ▼
                        ┌─────────────────────┐
                        │ Spring Boot Consumer│
                        │ (Avro + JPA)        │
                        └─────────┬───────────┘
                                   │
                                   ▼
                              PostgreSQL

```

1. The **producer** exposes a `/users` REST endpoint.  It receives a `UserCreateRequest`, validates it and converts it to an Avro `User` record.  The record is serialized and published to Kafka.
2. The **Schema Registry** stores Avro schemas and enforces compatibility rules when new versions are registered.
3. The **consumer** listens to the `users.v1` topic, deserializes Avro messages and maps them to a JPA `UserEntity`.  It persists each user to the `users.contact` table in PostgreSQL.

This separation of concerns ensures loose coupling between services and safe schema evolution.
The architecture represents a typical event-driven microservice using Kafka, Schema Registry and a relational database.


---

## 🗄️ Database Schema

The consumer stores events in a table named `contact` under the `users` schema.  The DDL is:

```sql
CREATE SCHEMA IF NOT EXISTS users;

CREATE TABLE users.contact (
    id          SERIAL PRIMARY KEY,
    userid      VARCHAR(64) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(32),
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    is_active   BOOLEAN,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    age         INTEGER
);

-- Optional index for faster lookups
CREATE INDEX ix_users_contact_userid ON users.contact (userid);
```

The JPA `UserEntity` maps to this table and uses a generated `id` as the primary key.  The `userid` column stores the logical identifier coming from the Avro record (`id` field).  You can customise this schema as needed.

---

## 🚀 Quickstart (Local)

### Prerequisites

- **Java 21** and **Maven 3.9+**
- **Docker** and **Docker Compose** installed

### Clone and build

```bash
git clone https://github.com/mathias82/kafka-schema-registry-spring-demo.git
cd kafka-schema-registry-spring-demo

# Build the common Avro schemas and applications
./mvnw clean install -DskipTests || mvn clean install -DskipTests
```

### Start infrastructure

The project includes a Docker Compose setup for running:
- Apache Kafka
- Confluent Schema Registry
- PostgreSQL

Start Kafka, Schema Registry and PostgreSQL using Docker Compose:

```bash
# spin up Kafka, Schema Registry and Postgres
docker compose -f docker-compose.yml up -d

# Kafka will be available on localhost:29092
# Schema Registry on http://localhost:8081
# PostgreSQL on localhost:5432 (user: kafka / password: kafkaConfluent)
```
## 🐳 Full Local Stack (Kafka + Schema Registry + PostgreSQL)

For a complete local development environment, this project provides
a `docker-compose-full.yml` file that starts:

- Apache Kafka
- Zookeeper
- Confluent Schema Registry
- PostgreSQL (used by the Kafka consumer)

### Start the full stack

```bash
docker compose -f docker-compose.yml up -d


### Run the consumer

```bash
# from the project root
cd consumer-app
../mvnw spring-boot:run || mvn spring-boot:run

# The application runs on port 8089 by default and listens to the `users.v1` topic.
```

### Run the producer

```bash
cd ../producer-app
../mvnw spring-boot:run || mvn spring-boot:run

# The application runs on port 8080 by default.  It exposes a POST /users endpoint.
```

### Produce a user event

Send a HTTP POST to create a user:

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "id": "u-20",
    "email": "mstauroy@gmail.com",
    "phone": "2109456738",
    "firstName": "Manthos",
    "lastName": "Staurou",
    "isActive": true,
    "age": 35
  }'
```

You should see logs in the consumer indicating that the record was received and saved to PostgreSQL.

---

📸 Demo Screenshots

Below are screenshots of the end‑to‑end flow:

Producer logs

This log shows the producer publishing a user to the topic users.v1 using the Schema Registry and Avro serializer.
<img width="2048" height="604" alt="image" src="https://github.com/user-attachments/assets/db7f8292-9673-4f3f-bf04-a9bce8c4d1b4" />


Consumer logs

This log shows the consumer subscribing to users.v1, consuming the Avro record and persisting it to the users.contact table in PostgreSQL.
<img width="2048" height="606" alt="image" src="https://github.com/user-attachments/assets/f883fec5-b933-47be-88d5-ea3f1c7f17be" />

Postman request

Use the provided Postman collection to test the API easily. The screenshot below shows the request body when creating a user via Postman.

<img width="2048" height="744" alt="image" src="https://github.com/user-attachments/assets/4f30c6e8-7481-4724-9733-88c05d52fb4e" />


## ☁️ Running with Confluent Cloud

To run against Confluent Cloud, create an account at [confluent.cloud](https://confluent.cloud/) and provision a Kafka cluster and Schema Registry.  Then set the following environment variables (either in a `.env` file or exported in your shell):

```bash
export CLOUD_BOOTSTRAP_SERVERS="pkc-xxxxx.us-central1.gcp.confluent.cloud:9092"
export CLOUD_API_KEY="<your-kafka-api-key>"
export CLOUD_API_SECRET="<your-kafka-api-secret>"
export SR_URL="https://xxxxx.us-central1.gcp.confluent.cloud"
export SR_API_KEY="<your-schema-registry-api-key>"
export SR_API_SECRET="<your-schema-registry-api-secret>"
```

Then start the applications with the `cloud` profile:

```bash
# Consumer
cd consumer-app
../mvnw spring-boot:run -Dspring-boot.run.profiles=cloud

# Producer
cd ../producer-app
../mvnw spring-boot:run -Dspring-boot.run.profiles=cloud
```

With this profile enabled, the applications will connect to Confluent Cloud using SASL/SSL and will not automatically register schemas; instead they will use the latest registered schema version.

---

🔄 Schema Evolution

Avro schemas can evolve while maintaining compatibility. To test schema evolution:

1. Modify common-schemas/src/main/avro/User.avsc (e.g., add an optional field with a default).
2. Build the common-schemas module (mvn install) to generate new Java classes.
3. Register the new schema version in Schema Registry (via the UI or API). Set the compatibility mode to BACKWARD or FULL for the subject users-value.
4. Deploy your producer and consumer using the new jar.

Because the services are configured with `auto.register.schemas=false` and `use.latest.version=true` (in cloud mode), producers will not register schemas automatically in higher environments.  This encourages CI/CD pipelines to manage schemas explicitly.

---

## 🧪 Testing & CI

- **Unit tests**: Add tests for your mapper, service and controller layers using JUnit and Mockito.
- **Integration tests**: Use [Testcontainers](https://www.testcontainers.org/) to spin up Kafka, Schema Registry and PostgreSQL in Docker for reproducible integration tests.
- **Continuous Integration**: Configure GitHub Actions or your preferred CI to run `mvn test` and build the Docker images.

---

## 🧭 About / Description

This project is a learning template and production starter kit for event‑driven architectures. It shows how to:

- Define strong data contracts using Avro and Schema Registry.
- Produce and consume Kafka events with Spring Boot.
- Persist events to a relational database (PostgreSQL).
- Evolve schemas safely and manage compatibility in CI/CD.

Feel free to fork, star and extend it for your own microservice projects.

---

## 📬 Postman Collection

A Postman collection is provided in the postman/ directory. Import it into Postman to quickly test the REST API:

- postman import kafka-schema-registry-spring-demo.postman_collection.json

The collection includes the Create User request with the correct JSON payload. Run it after starting the producer and consumer to see end‑to‑end functionality.

## 🤝 Contributing & Feedback

Contributions, feedback and issues are welcome! Feel free to open pull requests or issues. If you find this project helpful, please ⭐ star it on GitHub and share it on social media – it helps others discover it.

---

## 📜 License

This project is licensed under the MIT License – see the [LICENSE](./LICENSE) file for details.
