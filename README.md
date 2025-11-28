# Microservices Documentation

This document provides detailed documentation for the microservices in the school transport management system.

## 1. API Gateway

*   **Service Name:** `api-gateway`
*   **Port / Base URL:** `8080` / `http://localhost:8080`
*   **Purpose / Responsibility:** The single entry point for all client requests. It is responsible for routing requests to the appropriate downstream microservice, handling authentication by validating JWTs, and passing authenticated user details to the services via HTTP headers.

### Routing
The API gateway manages the following routes:

| Path Prefix         | Downstream Service |
| ------------------- | ------------------ |
| `/auth/**`          | `auth-service`     |
| `/buses/**`         | `bus-service`      |
| `/students/**`      | `student-service`  |
| `/locations/**`     | `location-service` |
| `/notifications/**` | `notification-service` |
| `/routes/**`        | `planning-service` |
| `/groups/**`        | `group-service`    |

### Authentication / Authorization
**General Header Requirement:** For all authenticated API calls, a JSON Web Token (JWT) must be provided in the `Authorization` header in the format `Bearer <token>`.

The gateway uses a custom `JwtAuthentication` filter for all routes except `/auth/**`.

*   It expects a JWT in the `Authorization` header (`Bearer <token>`).
*   If the token is valid, it forwards the request to the downstream service.
*   If the token is missing or invalid, it returns a `401 Unauthorized` error.

### Headers Passed to Downstream Services
Upon successful authentication, the gateway adds the following headers to the request before forwarding it:

*   `X-Auth-User`: The username (email) of the authenticated user.
*   `X-Auth-Roles`: A comma-separated string of roles associated with the user (e.g., "ADMIN,USER").
*   **Note:** `X-Auth-UserId` is not currently passed.

### Error Handling
*   `401 Unauthorized`: If the JWT is missing, invalid, or expired.
*   `404 Not Found`: If a route does not match any of the configured predicates.
*   `503 Service Unavailable`: If a downstream service is unavailable.

---

## 2. Inter-Service Communication

Microservices communicate with each other in two primary ways:

### Synchronous Communication (Direct API Calls)
For immediate, request-response interactions, services can communicate directly with each other using RESTful API calls. 

*   **Service Discovery:** In a production environment, a service discovery mechanism (like Eureka or Consul) should be used to resolve the network locations of service instances. For local development, services can be accessed via their configured `localhost` URLs (e.g., `http://localhost:8083/students`).
*   **Authentication:** When one service calls another, it must propagate the original user's identity and permissions. This is typically done by forwarding the JWT from the initial request.

### Asynchronous Communication (Events/Messages)
For processes that do not require an immediate response, services communicate asynchronously using a message broker (e.g., RabbitMQ, Kafka).

*   **Example:** The `group-service` might publish a `groups.created` event. The `planning-service` would then subscribe to this event to recalculate routes without the two services being tightly coupled.

### Reading User Information
Downstream services can read authenticated user information from the HTTP headers forwarded by the API Gateway. As noted in the gateway documentation, these headers include:

*   `X-Auth-User`: The username (email).
*   `X-Auth-Roles`: The user roles.
*   `X-Auth-UserId`: The user id.

Services can use this information to perform authorization checks or to associate data with a specific user.

---

## 3. Authentication Service

*   **Service Name:** `auth-service`
*   **Port / Base URL:** `8087` / `http://localhost:8087`
*   **Purpose / Responsibility:** Handles user registration and authentication, and issues JWT access tokens.

### Endpoints

#### Register a New User
*   **Endpoint:** `POST /auth/register`
*   **Description:** Creates a new user account.
*   **Authentication:** None. This is a public endpoint.
*   **Input (Request Body):**
    ```json
    {
        "email": "user@example.com",
        "password": "password123",
        "fullName": "John Doe",
        "role": "USER"
    }
    ```
    *   `email` (string, required, valid email format)
    *   `password` (string, required, min 6 characters)
    *   `fullName` (string, required)
    *   `role` (string, required, e.g., "USER", "ADMIN", "DRIVER")
*   **Output (Response Body):
    ```json
    {
        "message": "User registered successfully"
    }
    ```
*   **Error Handling:**
    *   `400 Bad Request`: If validation fails (e.g., invalid email, short password) or if the email is already in use.

#### Log In a User
*   **Endpoint:** `POST /auth/login`
*   **Description:** Authenticates a user and returns a JWT access token.
*   **Authentication:** None. This is a public endpoint.
*   **Input (Request Body):**
    ```json
    {
        "email": "user@example.com",
        "password": "password123"
    }
    ```
    *   `email` (string, required)
    *   `password` (string, required)
*   **Output (Response Body):**
    ```json
    {
        "accessToken": "ey...[jwt]...",
        "username": "user@example.com",
        "roles": "USER",
        "id": "c1f8a8b8-4d32-4d23-9c1c-3e6f2b4b4e1e"
    }
    ```
*   **Error Handling:**
    *   `401 Unauthorized`: If the credentials are invalid.

---

