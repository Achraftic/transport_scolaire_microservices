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

## 2. Authentication Service

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
*   **Output (Response Body):**
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

## 3. Student Service

*   **Service Name:** `student-service`
*   **Port / Base URL:** `8083` / `http://localhost:8083`
*   **Purpose / Responsibility:** Manages all operations related to students, such as creating, retrieving, updating, and deleting student records.

### Endpoints

#### Create a New Student
*   **Endpoint:** `POST /students`
*   **Description:** Adds a new student to the system.
*   **Authentication:** Required. A valid JWT must be provided in the `Authorization` header.
*   **Input (Request Body):**
    ```json
    {
        "firstName": "Jane",
        "lastName": "Doe",
        "latitude": 34.0522,
        "longitude": -118.2437
    }
    ```
    *   `firstName` (string, required)
    *   `lastName` (string, required)
    *   `latitude` (double)
    *   `longitude` (double)
*   **Output (Response Body):**
    ```json
    {
        "id": 1,
        "firstName": "Jane",
        "lastName": "Doe",
        "latitude": 34.0522,
        "longitude": -118.2437
    }
    ```
*   **Error Handling:**
    *   `400 Bad Request`: If the request body is malformed or missing required fields.
    *   `401 Unauthorized`: If the request is not authenticated.

#### Get All Students
*   **Endpoint:** `GET /students`
*   **Description:** Retrieves a list of all students.
*   **Authentication:** Required.
*   **Input:** None.
*   **Output (Response Body):**
    ```json
    [
        {
            "id": 1,
            "firstName": "Jane",
            "lastName": "Doe",
            "latitude": 34.0522,
            "longitude": -118.2437
        },
        {
            "id": 2,
            "firstName": "Peter",
            "lastName": "Jones",
            "latitude": 34.0525,
            "longitude": -118.2440
        }
    ]
    ```
*   **Error Handling:**
    *   `401 Unauthorized`: If the request is not authenticated.

#### Get Student by ID
*   **Endpoint:** `GET /students/{id}`
*   **Description:** Retrieves a single student by their ID.
*   **Authentication:** Required.
*   **Input:**
    *   `id` (long, path variable, required)
*   **Output (Response Body):**
    ```json
    {
        "id": 1,
        "firstName": "Jane",
        "lastName": "Doe",
        "latitude": 34.0522,
        "longitude": -118.2437
    }
    ```
*   **Error Handling:**
    *   `401 Unauthorized`: If the request is not authenticated.
    *   `404 Not Found`: If no student with the given ID exists.

#### Update a Student
*   **Endpoint:** `PUT /students/{id}`
*   **Description:** Updates the details of an existing student.
*   **Authentication:** Required.
*   **Input:**
    *   `id` (long, path variable, required)
    *   Request Body:
        ```json
        {
            "firstName": "Janet",
            "lastName": "Doe",
            "latitude": 34.0530,
            "longitude": -118.2450
        }
        ```
*   **Output (Response Body):**
    ```json
    {
        "id": 1,
        "firstName": "Janet",
        "lastName": "Doe",
        "latitude": 34.0530,
        "longitude": -118.2450
    }
    ```
*   **Error Handling:**
    *   `400 Bad Request`: If the request body is malformed.
    *   `401 Unauthorized`: If the request is not authenticated.
    *   `404 Not Found`: If no student with the given ID exists.

#### Delete a Student
*   **Endpoint:** `DELETE /students/{id}`
*   **Description:** Deletes a student from the system.
*   **Authentication:** Required.
*   **Input:**
    *   `id` (long, path variable, required)
*   **Output:** `204 No Content`
*   **Error Handling:**
    *   `401 Unauthorized`: If the request is not authenticated.
    *   `404 Not Found`: If no student with the given ID exists.
