# System Architecture Documentation

The **Progressive Student Dashboard** is built using a clean **Modular Monolith** pattern following a decoupled multi-layered architecture.

---

## 1. Layered Architecture

The application separates concerns cleanly into distinct execution layers:

```text
  ┌─────────────────────────────────────────────────────────────┐
  │                      React Frontend App                     │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ HTTP requests with JWT
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                      Controller Layer                       │
  │  - Exposes REST Endpoints (StudentController, AuthController)│
  │  - Validates request payloads and handles exceptions         │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                        Service Layer                        │
  │  - Implements business logic (StudentDashboardServiceImpl)  │
  │  - Orchestrates transactions and status updates             │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                      Repository Layer                       │
  │  - Manages database querying (UserRepository, CourseRepository)│
  │  - Utilizes Spring Data JPA for persistence mappings        │
  └──────────────────────────────┬──────────────────────────────┘
                                 │
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │                        Database Layer                       │
  │  - Relational Schema mapping MySQL or H2                    │
  └─────────────────────────────────────────────────────────────┘
```

### Components Breakdown:
*   **Controller Layer:** Houses REST endpoints. Restricts inputs, executes binding check validations (`@Valid`), and maps responses using clean Data Transfer Objects (DTOs) to hide database entities from outer layers.
*   **Service Layer:** Executes business logic. Encapsulates transaction demarcations (`@Transactional`), progress calculations, active study minutes logging, and student risk status classification models.
*   **Repository Layer:** Spring Data JPA interfaces representing database tables. Uses customized JPQL/native queries to aggregate metrics efficiently without running duplicate queries.

---

## 2. Authentication & Authorization

Securing student and mentor sessions is handled by **Spring Security** using state-free **JWT** authorization.

### Key Aspects:
*   **Password Protection:** All user passwords are encrypted using **BCrypt** hashing with a strength factor of 10 during registration. Plaintext passwords are never stored in the database.
*   **Token Generation:** Upon successful authentication, the server generates a signed compact JWT.
    *   **Algorithm:** HMAC SHA-256 (`HS256`)
    *   **Claims:** Subject (`email`), Issued At, Expiration Time, and Roles (`role`).
*   **Validation Filter:** Every incoming request intercepts via a custom `JwtAuthenticationFilter`. The filter parses the token from the `Authorization: Bearer <token>` header, validates its expiration and signature, loads `UserDetails` from the database, and injects authentication tokens into the Spring `SecurityContextHolder`.
*   **Role-Based Security:** Endpoints are protected at the filter chain level:
    *   `/api/student/**` requires the `STUDENT` role.
    *   `/api/mentor/**` requires the `MENTOR` role.
    *   `/api/auth/**` endpoints are public (registration and logins).

---

## 3. Data Flow

### Sample Request Lifecycle (Marking Lesson Complete):
1.  **Client:** The React frontend invokes a `POST /api/student/progress` request containing the lesson ID and study minutes, attaching the JWT header.
2.  **Filter Chain:** `JwtAuthenticationFilter` validates the token and registers the student's email context.
3.  **Controller:** `StudentController` captures the payload, retrieves the student's email from the security context, and delegates to `ProgressService.updateProgress()`.
4.  **Service:**
    *   Verifies enrollment validity.
    *   Saves/updates a `LessonProgress` record with the status `COMPLETED` and appends duration minutes.
    *   Fires an `ActivityEvent` logging the study session.
    *   Commit transactions.
5.  **Controller:** Yields a `200 OK` status with a confirmation message.
6.  **Client:** The UI captures the response, triggers a success toast, and issues background re-fetches to update dashboards.

---

## 4. Analytics Engine

The analytics module gathers insights directly from student study tables:
*   **Daily Learning Trends:** Aggregates total daily study minutes from `ActivityEvent` records. The repository matches records on dates using H2/MySQL time conversions. To ensure days with zero activity still appear, the service initializes an array covering the full window (7 days or 30 days) with zeros, then populates days that have activity logs.
*   **Course Distribution:** Evaluates enrolled courses, classifying them into:
    *   `COMPLETED`: course progress is 100%.
    *   `IN_PROGRESS`: course progress is > 0% and < 100%.
    *   `NOT_STARTED`: course progress is 0%.

---

## 5. Recommendation Engine

The next-step generator operates using a deterministic ruleset:
1.  **Priority 1 (HIGH):** Looks for enrolled courses that are currently `NOT_STARTED` or have progress `< 10%`. Recommends starting the course to build early momentum.
2.  **Priority 2 (MEDIUM):** Looks for active courses `IN_PROGRESS` and identifies the next unfinished lesson based on its `orderIndex`. Recommends continuing that specific lesson.
3.  **Priority 3 (LOW):** Evaluates if a student has completed all enrolled courses (progress is 100%). Recommends contacting their mentor to unlock advanced paths.
