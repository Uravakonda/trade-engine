# Trade Execution and Settlement Engine

The system accepts trade requests via a REST API, publishes them to a Kafka message queue, persists them to PostgreSQL, and processes batch settlements using Spring Batch. 
Redis is used for low-latency caching of user balances and market prices.

## What it does

- Accepts trade submissions (BUY/SELL orders) via a REST API with input validation
- Publishes each trade as an event to a Kafka topic using idempotent exactly-once delivery
- Consumes Kafka events and persists trades to PostgreSQL with status tracking
- Runs a scheduled batch settlement job that reads pending trades and marks them as settled
- Caches user account balances and live market prices in Redis
- Includes BDD acceptance tests written with Cucumber and unit/integration tests with JUnit 5

## Tech stack

- Java 21, Spring Boot 4.0.6
- Spring Batch 6.0.3 for batch settlement processing
- Apache Kafka with Spring Kafka 4.x for event streaming
- PostgreSQL 16 for trade persistence
- Redis 7 for in-memory caching
- Docker and Docker Compose for local infrastructure
- GitHub Actions for CI
- Maven for build management
- Lombok for reduced boilerplate
- JUnit 5, Mockito, Cucumber for testing

## Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker Desktop

## Setup and running locally

1. Clone the repository

        git clone https://github.com/Uravakonda/trade-engine.git
        cd trade-engine

2. Start the infrastructure containers

        docker compose up -d

   This starts PostgreSQL on port 5432, Redis on port 6379, Kafka on port 9092, and Zookeeper on port 2181. Wait about 10 seconds for all containers to report healthy.

3. Run the application

        mvn spring-boot:run

   The application starts on port 8080.

4. Run the tests

        mvn test

## Manual testing

Set up a user balance and market price, then submit a trade:

        curl -X POST http://localhost:8080/api/v1/users/user1/balance/100000
        curl -X POST http://localhost:8080/api/v1/market/prices/AAPL/185.50
        curl -X POST http://localhost:8080/api/v1/trades \
          -H "Content-Type: application/json" \
          -d '{"userId":"user1","ticker":"AAPL","type":"BUY","quantity":10,"price":185.50}'

Trigger the batch settlement job manually:

        curl -X POST http://localhost:8080/api/v1/batch/run-settlement

Check the health endpoint:

        curl http://localhost:8080/api/v1/health

## Project structure

    src/main/java/com/tradeengine/trade_engine/
      batch/          Spring Batch settlement job configuration
      config/         Kafka producer configuration
      controller/     REST API endpoints
      domain/         Trade entity and enums
      dto/            Request and response objects
      events/         Kafka event types
      repository/     Spring Data JPA repository
      service/        Redis caching and trade processing services
    src/test/
      controller/     MockMvc controller tests
      service/        Mockito unit tests
      cucumber/       BDD acceptance tests

## Stopping

    docker compose down
