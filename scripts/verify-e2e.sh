#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="docker/docker-compose.yml"
PRODUCER_LOG="${TMPDIR:-/tmp}/kafka-demo-producer.log"
CONSUMER_LOG="${TMPDIR:-/tmp}/kafka-demo-consumer.log"
PRODUCER_PID=""
CONSUMER_PID=""

cleanup() {
  set +e
  if [[ -n "${PRODUCER_PID}" ]]; then kill "${PRODUCER_PID}" 2>/dev/null || true; fi
  if [[ -n "${CONSUMER_PID}" ]]; then kill "${CONSUMER_PID}" 2>/dev/null || true; fi
  docker compose -f "${COMPOSE_FILE}" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

wait_for_http() {
  local url="$1"
  local attempts="${2:-60}"

  for ((i=1; i<=attempts; i++)); do
    if curl --silent --output /dev/null --connect-timeout 1 "${url}"; then
      return 0
    fi
    sleep 2
  done

  echo "Timed out waiting for ${url}" >&2
  return 1
}

wait_for_user_count() {
  local expected="$1"
  local attempts="${2:-60}"

  for ((i=1; i<=attempts; i++)); do
    local count
    count="$(docker compose -f "${COMPOSE_FILE}" exec -T postgres \
      psql -U kafka -d users -tAc "SELECT COUNT(*) FROM users.contact WHERE userid = 'e2e-user-1';" | tr -d '[:space:]')"
    if [[ "${count}" == "${expected}" ]]; then
      return 0
    fi
    sleep 2
  done

  echo "Expected e2e-user-1 count=${expected}, but condition was not reached" >&2
  return 1
}

echo "Starting Kafka, Schema Registry and PostgreSQL..."
docker compose -f "${COMPOSE_FILE}" up -d --wait

echo "Building all Maven modules..."
mvn -B package

echo "Starting consumer..."
java -jar consumer-app/target/consumer-app-0.0.1-SNAPSHOT.jar >"${CONSUMER_LOG}" 2>&1 &
CONSUMER_PID=$!

# A 404 is sufficient to prove the embedded web server is accepting connections.
for ((i=1; i<=60; i++)); do
  if curl --silent --output /dev/null --connect-timeout 1 http://localhost:8089/; then break; fi
  if ! kill -0 "${CONSUMER_PID}" 2>/dev/null; then
    echo "Consumer terminated during startup" >&2
    cat "${CONSUMER_LOG}" >&2
    exit 1
  fi
  sleep 2
done

echo "Starting producer..."
java -jar producer-app/target/producer-app-0.0.1-SNAPSHOT.jar >"${PRODUCER_LOG}" 2>&1 &
PRODUCER_PID=$!

for ((i=1; i<=60; i++)); do
  if curl --silent --output /dev/null --connect-timeout 1 http://localhost:8080/users; then break; fi
  if ! kill -0 "${PRODUCER_PID}" 2>/dev/null; then
    echo "Producer terminated during startup" >&2
    cat "${PRODUCER_LOG}" >&2
    exit 1
  fi
  sleep 2
done

PAYLOAD='{"id":"e2e-user-1","email":"e2e@example.com","phone":"2101234567","firstName":"E2E","lastName":"User","isActive":true,"age":28}'

echo "Publishing a real Avro event through the REST producer..."
curl --fail --silent --show-error \
  -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d "${PAYLOAD}" >/dev/null

wait_for_user_count 1

echo "Verifying the Schema Registry subject was created..."
curl --fail --silent --show-error http://localhost:8081/subjects \
  | grep -q 'users.v1-value'

echo "Publishing the same logical user again to verify idempotent persistence..."
curl --fail --silent --show-error \
  -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d "${PAYLOAD}" >/dev/null

sleep 3
wait_for_user_count 1 10

echo "E2E verification passed: REST -> Avro producer -> Kafka -> consumer -> PostgreSQL, with Schema Registry and duplicate-event protection."
