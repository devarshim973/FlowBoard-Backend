<h1 align="center">FlowBoard Backend</h1>

<p align="center">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&weight=600&size=22&duration=2600&pause=1000&color=2563EB&center=true&vCenter=true&repeat=true&width=820&height=48&lines=Spring+Boot+Microservices+for+FlowBoard;Authentication%2C+Boards%2C+Cards+and+Comments;Notifications%2C+Payments+and+Admin+Controls" alt="Typing SVG" />
</p>

<div align="center">
  <img src="https://img.shields.io/badge/Java-21-0f172a?style=for-the-badge&logo=openjdk&logoColor=f89820" alt="Java badge" />
  <img src="https://img.shields.io/badge/Spring_Boot-Microservices-0f172a?style=for-the-badge&logo=springboot&logoColor=6db33f" alt="Spring Boot badge" />
  <img src="https://img.shields.io/badge/Eureka-Service%20Discovery-0f172a?style=for-the-badge" alt="Eureka badge" />
  <img src="https://img.shields.io/badge/API_Gateway-8080-0f172a?style=for-the-badge" alt="Gateway badge" />
</div>

<p align="center">
  A production-style microservices backend for FlowBoard, built with Spring Boot, Eureka, and API Gateway
  to power authentication, workspace management, boards, lists, cards, comments, notifications, admin tools, and payments.
</p>

---

## Overview

This backend is designed as a service-oriented system for a Trello-inspired collaboration product. It uses:

- `Spring Boot` for service development
- `Spring Cloud Netflix Eureka` for service discovery
- `Spring Cloud Gateway` for centralized routing
- `MySQL` for service databases
- `RabbitMQ` for notification-related messaging
- `Redis` for gateway-side data support
- `OpenFeign` and `Resilience4j` for service-to-service communication

The result is a backend that is modular, scalable, and easy to extend as FlowBoard grows.

---

## Architecture

```text
Frontend
   |
   v
API Gateway :8080
   |
   +--> Auth Service :8081
   +--> Notification Service :8082
   +--> Comment Service :8083
   +--> Workspace Service :8084
   +--> Board Service :8085
   +--> List Service :8086
   +--> Card Service :8087
   +--> Payment Service :8089
   +--> Admin Service :8091

All services register with Eureka Server :8761
RabbitMQ supports async notification flows
Redis supports gateway-side infrastructure
MySQL stores domain data per service
```

---

## Services Map

| Service | Port | Purpose |
| --- | --- | --- |
| `flowboard-server` | `8761` | Eureka service registry |
| `flowboard-api-gateway` | `8080` | Central API entry point and request routing |
| `auth-service` | `8081` | Authentication, JWT, OTP/mail flow, OAuth login |
| `notification-service` | `8082` | Notification delivery and messaging workflows |
| `comment-service` | `8083` | Card comments and attachment-related features |
| `workspace-service` | `8084` | Workspace management |
| `board-service` | `8085` | Board operations |
| `list-service` | `8086` | List operations |
| `card-service` | `8087` | Card creation, updates, moves, workflow actions |
| `payment-service` | `8089` | Subscription/payment operations |
| `admin-service` | `8091` | Admin-only management operations |

---

## Gateway Routes

The API gateway is configured to route requests like:

- `/api/v1/auth/**` -> `AUTH-SERVICE`
- `/api/v1/user/**` -> `AUTH-SERVICE`
- `/api/v1/admin/**` -> `ADMIN-SERVICE`
- `/api/v1/notifications/**` -> `NOTIFICATION-SERVICE`
- `/api/v1/comments/**` -> `COMMENT-SERVICE`
- `/api/v1/attachments/**` -> `COMMENT-SERVICE`
- `/api/v1/workspaces/**` -> `WORKSPACE-SERVICE`
- `/api/v1/boards/**` -> `BOARD-SERVICE`
- `/api/v1/board-members/**` -> `BOARD-SERVICE`
- `/api/v1/lists/**` -> `LIST-SERVICE`
- `/api/v1/cards/**` -> `CARD-SERVICE`
- `/api/v1/payments/**` -> `PAYMENT-SERVICE`
- `/api/v1/subscriptions/**` -> `PAYMENT-SERVICE`

This lets the frontend talk mainly to `http://localhost:8080` while the gateway forwards traffic to the right microservice.

---

## Core Capabilities

- JWT-based authentication
- Google OAuth client integration in auth flow
- Email/OTP-based account operations
- Workspace, board, list, and card management
- Comment and attachment support
- Notification pipelines through RabbitMQ
- Payment integration for workspace upgrades
- Admin management endpoints
- Swagger/OpenAPI support across services

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot |
| Service Discovery | Eureka |
| Gateway | Spring Cloud Gateway |
| Database | MySQL |
| Cache / Infra | Redis |
| Messaging | RabbitMQ |
| Inter-service Calls | OpenFeign |
| Resilience | Resilience4j |
| API Docs | springdoc OpenAPI |
| Payments | Razorpay |
| Media | Cloudinary |
| Email | Gmail SMTP / Brevo integrations |

---

## Project Structure

```bash
FlowBoard-Backend/
|-- flowboard-server/
|-- flowboard-api-gateway/
|-- auth-service/
|-- admin-service/
|-- workspace-service/
|-- board-service/
|-- list-service/
|-- card-service/
|-- comment-service/
|-- notification_service/
|-- payment-service/
`-- README.md
```

---

## Prerequisites

Install and run these locally before starting the backend:

- `Java 21`
- `Maven` or use each service's `mvnw` / `mvnw.cmd`
- `MySQL`
- `Redis` on default port `6379`
- `RabbitMQ` on default port `5672`

Recommended local defaults seen in the project config:

- MySQL on `localhost:3306`
- Redis on `localhost:6379`
- RabbitMQ on `localhost:5672`
- Eureka on `localhost:8761`
- Gateway on `localhost:8080`

---

## Databases

This backend uses separate MySQL databases for service boundaries, including:

- `flow_board_auth_service_db`
- `flow_board_notification_service_db`
- `flow_board_comment_service_db`
- `flow_board_workspace_service_db`
- `flow_board_board_service_db`
- `flow_board_list_service_db`
- `flow_board_card_service_db`
- `flow_board_payment_service_db`

Create the databases before first boot, or adjust each service config to match your local environment.

---

## Environment Variables

Several services read secrets and integration values from environment variables. Common ones include:

```env
DB_PASSWORD=your_mysql_password
db_password=your_mysql_password
JWT_SECRET_KEY=your_jwt_secret
jwt-secret-key=your_jwt_secret

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8081/login/oauth2/code/google

SMTP_USERNAME=your_email_username
SMTP_APP_PASSWORD=your_email_app_password
SMTP_FROM_EMAIL=no-reply@example.com
SMTP_FROM_NAME=FlowBoard
FRONTEND_URL=http://localhost:5173

RAZORPAY_KEY_ID=your_razorpay_key
RAZORPAY_KEY_SECRET=your_razorpay_secret

cloudinary-cloud-name=your_cloudinary_name
cloudinary-api-key=your_cloudinary_key
cloudinary-api-secret=your_cloudinary_secret

brevo-api-key=your_brevo_api_key
brevo-sender-mail=your_sender_email

RABBITMQ_ENABLED=false
```

Keep secrets out of source control and prefer a local `.env`, IDE run config, or system environment variables.

---

## Startup Order

Start the platform in this order for the smoothest local run:

1. Start `MySQL`
2. Start `Redis`
3. Start `RabbitMQ`
4. Start `flowboard-server`
5. Start `flowboard-api-gateway`
6. Start `auth-service`
7. Start `notification-service`
8. Start the remaining domain services:
   `workspace-service`, `board-service`, `list-service`, `card-service`, `comment-service`, `payment-service`, `admin-service`

---

## Running a Service

From any service folder, use Maven Wrapper or Maven directly.

### Using Maven Wrapper on Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### Using Maven

```bash
mvn spring-boot:run
```

### Build a service

```bash
mvn clean install
```

### Run tests

```bash
mvn test
```

Repeat that inside each service directory you want to build or run.

---

## Quick Start Example

```bash
cd flowboard-server
mvn spring-boot:run

cd ../flowboard-api-gateway
mvn spring-boot:run

cd ../auth-service
mvn spring-boot:run
```

Then continue with the remaining services.

---

## API Docs

Several services include `springdoc-openapi-starter-webmvc-ui`, so Swagger UI should be available per service when running.

Typical pattern:

```text
http://localhost:<service-port>/swagger-ui/index.html
```

Example:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## Messaging and Async Flow

RabbitMQ is used by the notification-related services and queue-based workflows. The project config includes:

- exchange: `notification-exchange`
- queue: `single-notification-queue`
- queue: `bulk-notification-queue`
- routing key: `single-notification-key`
- routing key: `bulk-notification-key`

This helps decouple events like comment or card actions from downstream notification delivery.

---

## Integrations

- `Google OAuth` for social login support
- `Gmail SMTP` for auth-related mail flows
- `Brevo` for notification mail delivery
- `Cloudinary` for attachment/media handling
- `Razorpay` for paid workspace upgrades

---

## Why This Backend Is Strong

- Clear microservice boundaries
- Centralized routing through the gateway
- Service discovery with Eureka
- Real-world integrations instead of demo-only architecture
- Independent databases per service domain
- Async event support with RabbitMQ
- Better resilience through Feign + circuit breakers

---

## Notes

- Most services use `ddl-auto: update`, which is convenient for local development.
- The gateway expects Redis locally on port `6379`.
- Notification-related services expect RabbitMQ locally on port `5672`.
- The frontend can typically point to `http://localhost:8080` as its API base URL.

---

## Security Reminder

Before publishing or deploying this backend, review configuration files carefully and move every secret, credential,
email address, API key, and password fully into environment variables or a secure secret manager.

<div align="center">
  <sub>Built for FlowBoard backend development, demos, and service orchestration.</sub>
</div>
