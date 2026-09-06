# Digital Banking System

A microservices-based digital banking backend built with Spring Boot, demonstrating distributed transaction management, event-driven fraud detection, and secure payment processing.

## Architecture

- **API Gateway** — single entry point routing requests to all services, configured with rate limiting
- **Account Service** — manages accounts, balances, debit/credit/refund operations
- **Transaction Service** — orchestrates distributed transactions using the SAGA pattern, with rollback/compensation on failure
- **Payment Service** — handles payment processing via Razorpay integration
- **Fraud Detection Service** — consumes transaction events from Kafka in real time, flags suspicious activity, and can trigger account blocking
- **Notification Service** — sends notifications based on transaction/account events

Each service follows the **database-per-service** pattern for independence and fault isolation. Services communicate asynchronously via **Apache Kafka**, and **Redis** is used for caching.

## Tech Stack

Java, Spring Boot, Spring Security (JWT), Spring Data JPA, Apache Kafka, Redis, MySQL, Razorpay API

## Key Design Decisions

- **SAGA Pattern**: Used for managing distributed transactions across services without a central coordinator, with compensating actions on failure to maintain consistency.
- **Event-driven Fraud Detection**: Every transaction event is published to Kafka and checked by the Fraud Detection Service before completion, enabling real-time flagging without blocking the main transaction flow.
- **API Gateway with Rate Limiting**: Protects backend services from abuse and controls traffic at a single entry point.

## Running Locally

1. Clone the repo
2. Ensure Kafka, Redis, and MySQL are running (locally or via Docker)
3. Update `application.properties` in each service with your local DB/Kafka/Redis config
4. Run each service (`API-Gateway-Services`, `Account-Services`, `Transaction-Services`, `Payment-Services`, `Fraud-Detection-Services`, `Notification-Services`) individually via your IDE or `mvn spring-boot:run`

## Author

Yarra Nagendra Rao — [LinkedIn](https://linkedin.com/in/yarra-nagendra-rao2003) | [GitHub](https://github.com/nagendrarao-yarra)
