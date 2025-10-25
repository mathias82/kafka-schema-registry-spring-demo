FROM postgres:latest

ENV POSTGRES_DB=users
ENV POSTGRES_USER=kafka
ENV POSTGRES_PASSWORD=kafkaConfluent

COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
VOLUME ["/var/lib/postgresql/data"]
