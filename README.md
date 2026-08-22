# Progressive Student Dashboard

A professional full-stack web application that tracks student progress across courses, visualizes learning insights, provides data-driven next-step recommendations, and enables mentors to monitor student risk performance.

## Overview

*   **Problem:** Self-paced online learning often suffers from high attrition and low engagement due to a lack of feedback loops, progress tracking, and personalized guidance.
*   **Solution:** A comprehensive learning dashboard that gamifies progress (streaks, visual progress bars), plots daily learning trends, presents explainable next steps, and lets mentors track student status proactively.
*   **Target Users:** Students who need structured pacing and analytics; Mentors who manage student cohorts and identify students needing attention.
*   **Main Capabilities:** Dynamic JWT security, in-depth learning metrics calculations, time-series activity trend tracking, and at-risk student classification matrices.

---

## Features

### Student Console
*   **Secure Authentication:** Secure session management via JWT, registration for students, and automatic login after signing up.
*   **Summary Indicators:** Displays enrolled courses, lessons completed, hours spent, overall progression, and a glowing daily learning streak indicator.
*   **Learning Trend Analytics:** Time-series area charts displaying daily study minutes for 7-day or 30-day filters.
*   **Course Distribution:** Donut charts summarizing completion status groups (Completed vs In Progress vs Not Started).
*   **Adaptive Recommendations:** Priority-based, explainable suggestions guiding students to resume courses, start lessons, or check in with their mentor.
*   **Recent Activity timeline:** Chronologically displays study milestones and lesson completions.

### Mentor Cohort Directory
*   **Performance Metrics Summary:** Cohort averages tracking active students count, average course completion, and at-risk student counts.
*   **Student Cohort Directory:** A fully pageable table showing enrolled course counts, overall progress percentages, last active dates, and status categories (`HEALTHY`, `NEEDS_ATTENTION`, `AT_RISK`). Include name/email keyword search filters and status filter dropdowns.
*   **Student Profile Monitoring:** Complete overview of individual student analytics, trends, activity histories, and course progression.
*   **RFC-Compliant CSV Export:** One-click spreadsheet exports detailing student progress, status metrics, and study hours.

### Admin Course Management
*   **Dynamic Course Management:** Create, update, and delete courses with customizable categories, difficulty levels, and duration estimates.
*   **Syllabus & Lesson Management:** Reorder and manage lessons for any course dynamically.
*   **Rich Text / Markdown Editor:** Live split-pane editor to author lesson content in GitHub-Flavored Markdown. Rendered seamlessly on the student side.

---

## Technology Stack

### Frontend
*   **Framework:** React (Vite-powered, ES6 Javascript)
*   **Styling:** Vanilla CSS & Tailwind CSS utility layer
*   **Charting:** Recharts (responsive Area and Pie charts)
*   **Icons:** Lucide-react icons

### Backend
*   **Framework:** Spring Boot 3.3.3 (Java 21)
*   **Security:** Spring Security 6, BCrypt password hashing, JWT session authentication
*   **Data Access:** JPA / Hibernate persistence layer
*   **Build Tool:** Maven

### Database
*   **Database:** MySQL (local or containerized)
*   **Alternative:** In-memory H2 support for isolated local testing/packaging

### Testing
*   **Testing Frameworks:** JUnit 5, Mockito, MockMvc controllers testing, Spring Security integration testing

---

## Architecture

```text
       React (Vite Server: Port 5173)
                   │
                   ▼  (REST API calls over JWT Authorization headers)
    Spring Boot (Tomcat API Server: Port 8080)
                   │
                   ▼  (Business Logic & DTO mappings)
         Service Implementation Layer
                   │
                   ▼  (Data Access Object Interfaces)
         Repository Layer (Spring Data JPA)
                   │
                   ▼  (Query execution)
            MySQL Database (Port 3306)
```

---

## Database Design (ER Diagram Summary)

```text
  ┌──────────────┐          ┌──────────────┐
  │    Users     │ 1      * │ Enrollments  │
  │ (Student/    ├─────────►│  (Status/    │
  │  Mentor)     │          │  Date)       │
  └──────┬───────┘          └──────┬───────┘
         │ 1                       │ *
         │                         ▼
         │                  ┌──────────────┐
         │                  │   Courses    │
         │                  │  (Category/  │
         │                  │  Difficulty) │
         │                  └──────┬───────┘
         │ 1                       │ 1
         ▼                         ▼ *
  ┌──────────────┐          ┌──────────────┐
  │LessonProgress│ *      1 │   Lessons    │
  │ (Completed/  ├─────────►│   (Order/    │
  │  Minutes)    │          │   Minutes)   │
  └──────────────┘          └──────────────┘
```

For complete database descriptions, entity fields, and constraints, refer to [docs/DATABASE_DESIGN.md](file:///a:/Progressive%2520Student%2520Dashboard/docs/DATABASE_DESIGN.md).

---

## Local Setup

Detailed requirements, environment parameters, and build commands are fully documented in [docs/SETUP.md](file:///a:/Progressive%2520Student%2520Dashboard/docs/SETUP.md).

### Quick Launch Summary:
1.  **Launch MySQL:** Make sure MySQL is running on port 3306 or start the container:
    ```bash
    docker-compose up -d
    ```
2.  **Start Backend:**
    *   **In-Memory Mode (H2 - Default):** Run directly without database setup:
        ```bash
        cd backend
        mvn spring-boot:run
        ```
    *   **MySQL Mode:** Run with the `mysql` profile:
        ```bash
        cd backend
        mvn spring-boot:run -Dspring-boot.run.profiles=mysql
        ```
3.  **Start Frontend:**
    ```bash
    cd frontend
    npm install
    npm run dev
    ```

---

## Demo Seed Credentials

*   **Admin Console:**
    *   Email: `admin@example.com`
    *   Password: `password`
*   **Mentor Console:**
    *   Email: `mentor@example.com`
    *   Password: `password`
*   **Student Console:**
    *   Students can register dynamic accounts directly from the UI sign-up page (`/register`), which will automatically enroll them in the baseline `React Fundamentals` course and assign them to the default mentor.

---

## Technical Documentation Links

*   [System Architecture Guide](file:///a:/Progressive%2520Student%2520Dashboard/docs/ARCHITECTURE.md)
*   [Database Design Schema](file:///a:/Progressive%2520Student%2520Dashboard/docs/DATABASE_DESIGN.md)
*   [API Endpoints Reference](file:///a:/Progressive%2520Student%2520Dashboard/docs/API_DOCUMENTATION.md)
*   [Local Setup Instructions](file:///a:/Progressive%2520Student%2520Dashboard/docs/SETUP.md)
*   [Interview Notes & FAQ](file:///a:/Progressive%2520Student%2520Dashboard/docs/INTERVIEW_NOTES.md)
