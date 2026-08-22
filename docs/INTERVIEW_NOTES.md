# Interview Notes

This document provides explanations of architectural decisions, logic implementations, and considerations for the **Progressive Student Dashboard** application.

---

## 1. Architectural Decisions

### Why Spring Boot?
*   **Production Readiness:** Provides a robust, pre-configured framework with embedded Tomcat servers, reducing boilerplate.
*   **Dependency Injection & Security:** Spring Security isolates authentication/authorization logic cleanly.
*   **Data Consistency:** Spring Data JPA / Hibernate handles transactional unit-of-work state management and prevents manual query building errors.

### Why MySQL?
*   **Relational Integrity:** Fits the learning domain which relies heavily on strong foreign key constraints (Users -> Enrollments -> Courses -> Lessons -> Progress).
*   **Transaction Controls:** ACID transactions are vital for managing enrollment states and progress audits.
*   **Performance:** Reliable index indexing and group-by querying speeds up time-series analytics.

### Why JWT?
*   **Stateless Operations:** Eliminates session tracking storage overhead on the server, allowing the backend to scale statelessly.
*   **Decoupled Frontend:** Enables the React frontend to run on a separate origin (`localhost:5173`) while authenticating safely over standard HTTP headers.

---

## 2. Implementation Logic Details

### How does authentication work?
*   **Registration:** The user inputs credentials. The server hashes the password with **BCrypt**, saves the student entity, and automatically issues a JWT token.
*   **Login:** The client posts username/password. Spring Security validates the credentials. If correct, a JWT signed with HMAC-SHA256 is generated containing the user's role and email claims.
*   **API Interception:** The client attaches `Authorization: Bearer <token>` to secure requests. A custom `JwtAuthenticationFilter` intercepts the request, validates the token signature, sets the Spring Security Context, and routes the call.

### How is progress calculated?
*   The overall course progress is calculated as:
    $$\text{Progress} = \left( \frac{\text{Completed Lessons in Course}}{\text{Total Lessons in Course}} \right) \times 100$$
*   Calculated dynamically by Jpa queries matching `lesson_progress` status equal to `COMPLETED` for the active student.

### How is learning time calculated?
*   Accumulated study duration is tracked inside `lesson_progress.timeSpentMinutes` and audited chronologically inside the `activity_events` logs. It represents the sum of minutes logged by the student.

### How is the learning trend generated?
*   The query groups `activity_events` duration minutes by date over the selected range (7 or 30 days). The service initializes a date map filled with zeros to represent days with no study activity, then populates active entries.

### How does the recommendation engine work?
*   Uses a deterministic, explainable priority flow:
    1.  **HIGH:** Courses enrolled with progress `< 10%` (Recommends starting the course).
    2.  **MEDIUM:** Enrolled courses currently in progress (Recommends resuming the next incomplete lesson).
    3.  **LOW:** All enrolled courses completed (Recommends contacting the mentor for advanced guidance).

### How are at-risk students identified?
*   Calculated dynamically based on learning indicators:
    *   `HEALTHY`: Overall progress average $\ge 70\%$.
    *   `NEEDS_ATTENTION`: Overall progress average $\ge 40\%$ and $< 70\%$.
    *   `AT_RISK`: Overall progress average $< 40\%$ OR no activity recorded for the past 5 consecutive days.

### How does mentor authorization work?
*   Before returning any student tracking profile details, the `MentorService` verifies that the target student is assigned to the authenticated mentor (`student.mentor.id == principal.id`). If the check fails, the controller rejects the request with a `403 Forbidden` status.

### How does CSV export work?
*   Queries the mentor's student summaries. Outputs headers and cells in RFC 4180 CSV format (escaping strings containing commas or quotes). The controller streams the response with `Content-Type: text/csv` and attaches a file attachment disposition header.

---

## 3. Production Enhancements

In a production version, we would implement:
1.  **JWT Refresh Tokens:** Use short-lived access tokens (15 mins) and persist database-backed refresh tokens to mitigate session hijacking risks.
2.  **Database Pagination & Sorting:** Implement server-side sorting and text search directly inside repository queries.
3.  **Real-Time WebSocket Notifications:** Notify mentors instantly when a student's status changes to `AT_RISK`.
4.  **Redis Cache Layer:** Cache student dashboard aggregates to reduce repetitive SQL queries.
5.  **Global Axios Retry/Refreshing:** Automatically catch expired JWT errors, fetch a new access token in the background, and retry the failed request transparently.
