# API Documentation

The Progressive Student Dashboard backend exposes a secured JSON REST API.

*   **Swagger Documentation URL (UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **OpenAPI JSON Specification:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 1. Authentication Endpoints

### Register Student
*   **Method:** `POST`
*   **URL:** `/api/auth/register`
*   **Authentication:** None
*   **Request Body:**
    ```json
    {
      "name": "John Student",
      "email": "student@example.com",
      "password": "password"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "user": {
        "id": 2,
        "name": "John Student",
        "email": "student@example.com",
        "role": "STUDENT"
      }
    }
    ```

### Login
*   **Method:** `POST`
*   **URL:** `/api/auth/login`
*   **Authentication:** None
*   **Request Body:**
    ```json
    {
      "email": "student@example.com",
      "password": "password"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "user": {
        "id": 2,
        "name": "John Student",
        "email": "student@example.com",
        "role": "STUDENT"
      }
    }
    ```

### Get Current User Profile
*   **Method:** `GET`
*   **URL:** `/api/auth/me`
*   **Authentication:** JWT Bearer Header
*   **Response (200 OK):**
    ```json
    {
      "id": 2,
      "name": "John Student",
      "email": "student@example.com",
      "role": "STUDENT"
    }
    ```

---

## 2. Student Console Endpoints

### Student Dashboard Aggregates
*   **Method:** `GET`
*   **URL:** `/api/student/dashboard`
*   **Authentication:** JWT (Role = `STUDENT`)
*   **Response (200 OK):**
    ```json
    {
      "totalCourses": 1,
      "completedLessons": 1,
      "totalLearningMinutes": 25,
      "overallProgress": 33.333333333333336,
      "currentStreak": 1,
      "recentActivity": [
        {
          "id": 1,
          "eventType": "LESSON_COMPLETE",
          "courseTitle": "React Basics",
          "lessonTitle": "Introduction to React",
          "durationMinutes": 25,
          "eventDate": "2026-08-22T13:30:00"
        }
      ],
      "recommendations": [
        {
          "id": "rec_react_basics",
          "title": "Continue React Basics",
          "description": "Resume your next topic inside React Basics.",
          "priority": "MEDIUM",
          "reason": "You completed the lesson: Introduction to React.",
          "courseId": 1
        }
      ],
      "courseProgress": [
        {
          "id": 1,
          "title": "React Basics",
          "category": "Frontend",
          "difficulty": "BEGINNER",
          "progressPercentage": 33.333333333333336,
          "completedLessons": 1,
          "totalLessons": 3,
          "timeSpentMinutes": 25
        }
      ]
    }
    ```

### Get Enrolled Courses List
*   **Method:** `GET`
*   **URL:** `/api/student/courses`
*   **Authentication:** JWT (Role = `STUDENT`)

### Get Specific Course Details
*   **Method:** `GET`
*   **URL:** `/api/student/courses/{courseId}`
*   **Authentication:** JWT (Role = `STUDENT`)

### Update Lesson Progress / Log Study Time
*   **Method:** `POST`
*   **URL:** `/api/student/progress`
*   **Authentication:** JWT (Role = `STUDENT`)
*   **Request Body:**
    ```json
    {
      "lessonId": 2,
      "status": "COMPLETED",
      "durationMinutes": 30
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "message": "Lesson progress updated successfully"
    }
    ```

---

## 3. Student Analytics Endpoints

### Daily Learning Activity Trend
*   **Method:** `GET`
*   **URL:** `/api/student/analytics/learning-trend?range=7d`
*   **Authentication:** JWT (Role = `STUDENT`)
*   **Response (200 OK):**
    ```json
    {
      "range": "7d",
      "data": [
        { "date": "2026-08-16", "minutes": 0 },
        { "date": "2026-08-17", "minutes": 0 },
        { "date": "2026-08-18", "minutes": 0 },
        { "date": "2026-08-19", "minutes": 0 },
        { "date": "2026-08-20", "minutes": 0 },
        { "date": "2026-08-21", "minutes": 0 },
        { "date": "2026-08-22", "minutes": 25 }
      ]
    }
    ```

### Course Completion Distribution
*   **Method:** `GET`
*   **URL:** `/api/student/analytics/course-distribution`
*   **Authentication:** JWT (Role = `STUDENT`)
*   **Response (200 OK):**
    ```json
    {
      "completed": 0,
      "inProgress": 1,
      "notStarted": 0
    }
    ```

---

## 4. Mentor Console Endpoints

### Mentor Dashboard Aggregates
*   **Method:** `GET`
*   **URL:** `/api/mentor/dashboard`
*   **Authentication:** JWT (Role = `MENTOR`)
*   **Response (200 OK):**
    ```json
    {
      "totalStudents": 1,
      "activeStudents": 1,
      "averageProgress": 33.333333333333336,
      "atRiskStudents": 1
    }
    ```

### Paginated Cohort Directory
*   **Method:** `GET`
*   **URL:** `/api/mentor/students?page=0&size=10`
*   **Authentication:** JWT (Role = `MENTOR`)

### Student Details & Monitoring Profile
*   **Method:** `GET`
*   **URL:** `/api/mentor/students/{studentId}`
*   **Authentication:** JWT (Role = `MENTOR`)
*   **Response (200 OK):**
    ```json
    {
      "student": {
        "id": 2,
        "name": "John Student",
        "email": "student@example.com",
        "role": "STUDENT"
      },
      "courses": [
        {
          "id": 1,
          "title": "React Basics",
          "category": "Frontend",
          "difficulty": "BEGINNER",
          "progressPercentage": 33.333333333333336,
          "completedLessons": 1,
          "totalLessons": 3,
          "timeSpentMinutes": 25
        }
      ],
      "learningMinutes": 25,
      "currentStreak": 1,
      "overallProgress": 33.333333333333336,
      "lastActive": "2026-08-22T13:30:00",
      "status": "AT_RISK"
    }
    ```

### Export CSV
*   **Method:** `GET`
*   **URL:** `/api/mentor/export/students.csv`
*   **Authentication:** JWT (Role = `MENTOR`)
*   **Response (200 OK):** Yields file attachment stream (`text/csv`).

---

## 5. Admin Course Management Endpoints

### List All Courses
*   **Method:** `GET`
*   **URL:** `/api/admin/courses`
*   **Authentication:** JWT (Role = `ADMIN`)

### Get Course By ID
*   **Method:** `GET`
*   **URL:** `/api/admin/courses/{id}`
*   **Authentication:** JWT (Role = `ADMIN`)

### Create Course
*   **Method:** `POST`
*   **URL:** `/api/admin/courses`
*   **Authentication:** JWT (Role = `ADMIN`)
*   **Request Body:**
    ```json
    {
      "title": "Advanced React Patterns",
      "category": "Frontend",
      "difficulty": "ADVANCED",
      "estimatedHours": 8,
      "description": "Master hooks, context API, and performance optimization."
    }
    ```

### Update Course
*   **Method:** `PUT`
*   **URL:** `/api/admin/courses/{id}`
*   **Authentication:** JWT (Role = `ADMIN`)

### Delete Course
*   **Method:** `DELETE`
*   **URL:** `/api/admin/courses/{id}`
*   **Authentication:** JWT (Role = `ADMIN`)

### List Lessons for Course
*   **Method:** `GET`
*   **URL:** `/api/admin/lessons/course/{courseId}`
*   **Authentication:** JWT (Role = `ADMIN`)

### Create Lesson (with Markdown Content)
*   **Method:** `POST`
*   **URL:** `/api/admin/lessons/course/{courseId}`
*   **Authentication:** JWT (Role = `ADMIN`)
*   **Request Body:**
    ```json
    {
      "title": "Custom Hooks",
      "description": "Building reusable stateful logic.",
      "orderIndex": 1,
      "estimatedMinutes": 30,
      "content": "# Custom Hooks\nLearn how to encapsulate component logic into custom hooks..."
    }
    ```

### Update Lesson
*   **Method:** `PUT`
*   **URL:** `/api/admin/lessons/{id}`
*   **Authentication:** JWT (Role = `ADMIN`)

### Delete Lesson
*   **Method:** `DELETE`
*   **URL:** `/api/admin/lessons/{id}`
*   **Authentication:** JWT (Role = `ADMIN`)

