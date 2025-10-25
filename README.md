# Minimal Demo: Kafka Schema Registry with Spring Boot (Java 21, Avro)

This repo shows a tiny **producer** and **consumer** using **Avro** with **Confluent Schema Registry**.

## Quickstart

1) Start infra:
   Postgrsql : Run Dockerfile and the docker compose file 
```bash
docker compose up -d
# Kafka at localhost:29092, Schema Registry at http://localhost:8081
```
it will create database schema with record

2) Build all:
```bash
./mvnw -q -DskipTests package || mvn -q -DskipTests package
```

4) Run consumer:
```bash
cd consumer-app
../mvnw spring-boot:run || mvn spring-boot:run
```

5) Run producer (new terminal):
```bash
cd producer-app
../mvnw spring-boot:run || mvn spring-boot:run
```

6) Send a message:
```bash
curl -X POST http://localhost:8080/users   -H "Content-Type: application/json"   -d '{"id":"u-20","email":"mstauroy@gmail.com","phone":"2109456738","firstName":"Manthos","lastName":"Staurou","isActive":true,"age":35}'
```

You should see the consumer log the received `User` record.

---

### Evolution test
- Create account https://confluent.cloud/ 
- Update `common-schemas/src/main/avro/User.avsc` (e.g., add optional field with default).
- Use **BACKWARD** compatibility in confluent cloud and register the new version.
- Apps have `auto.register.schemas=false` and `use.latest.version=true` to force CI-driven registration.

## Use with Confluent Cloud (bring your own keys)
Create a `.env` file or export env vars:

```bash
export CLOUD_BOOTSTRAP_SERVERS="pkc-xxxxx.us-central1.gcp.confluent.cloud:9092"
export CLOUD_API_KEY="******"
export CLOUD_API_SECRET="******"
export SR_URL="https://xxxxx.us-central1.gcp.confluent.cloud"
export SR_API_KEY="******"
export SR_API_SECRET="******"
```

Then run with the **cloud** profile:

```bash
# Consumer
cd consumer-app
../mvnw spring-boot:run -Dspring-boot.run.profiles=cloud

# Producer
cd ../producer-app
../mvnw spring-boot:run -Dspring-boot.run.profiles=cloud
```

> Τα apps είναι ρυθμισμένα με `auto.register.schemas=false` και `use.latest.version=true`. Κάνε register schema με το `scripts/schema-ci.sh` ή μέσω UI/CLI στο Confluent Cloud πριν στείλεις μηνύματα.
