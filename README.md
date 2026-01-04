# Kafka Schema Registry with Spring Boot – Complete Demo

A **production-ready Spring Boot demo** showcasing how to use **Apache Kafka with Confluent Schema Registry and Avro serialization**.

This repository demonstrates **best practices** for schema management, message compatibility, and event-driven microservices using **Kafka + Spring Boot**.

---

## 🚀 Features

- Apache Kafka producer & consumer with Spring Boot
- Confluent Schema Registry integration
- Avro serialization / deserialization
- Schema evolution & compatibility handling
- Clean, minimal, production-style configuration
- Docker-ready Kafka stack (optional)
- Java & Spring Kafka best practices

---

## 🧩 Tech Stack

- **Java**
- **Spring Boot**
- **Spring for Apache Kafka**
- **Apache Kafka**
- **Confluent Schema Registry**
- **Avro**
- **Docker / Docker Compose**

---

## 🏗️ Architecture Overview

Spring Boot Producer
|
| (Avro)
v
Kafka Topic
|
| (Schema Registry)
v
Spring Boot Consumer


Schemas are centrally managed via **Schema Registry**, ensuring:
- Strong data contracts
- Backward / forward compatibility
- Safe evolution of events

---

## 📦 Why Schema Registry?

Using Schema Registry allows you to:

- Avoid breaking consumers
- Enforce schema compatibility rules
- Share contracts across microservices
- Version events safely
- Reduce serialization errors in production

This is **mandatory knowledge** for serious Kafka systems.

---

## ▶️ How to Run

### 1️⃣ Start Kafka & Schema Registry

```bash
docker-compose up -d

2️⃣ Run Spring Boot Application

mvn spring-boot:run

🧪 Testing the Flow

- Produce Kafka messages using Avro schemas
- Observe schema registration in Schema Registry UI
- Consume messages safely with enforced schema compatibility

📚 Who Is This For?

- Java / Spring Boot developers
- Kafka engineers
- Backend & microservices architects

Developers preparing for:

- Confluent Kafka certifications
- Event-driven architecture interviews

💡 Best Practices Covered

- Strongly typed Kafka events
- Schema evolution strategies
- Clean Kafka configuration
- Separation of producer & consumer concerns
- Avoiding JSON pitfalls in distributed systems

🌟 Star the Repo

If this repository helped you:

⭐ Star it
🍴 Fork it
🧠 Learn Kafka the right way

📬 Author

Created by Mathias
Senior Java & Spring Boot Engineer
Kafka • Event Streaming • Microservices

Feel free to open issues or contribute 🚀

