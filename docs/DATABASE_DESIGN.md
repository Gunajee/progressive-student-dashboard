# Database Design Document

This document outlines the relational database schema design, indexes, and constraints for the **Progressive Student Dashboard** application.

---

## 1. Entity Relationship (ER) Summary

The database uses standard Foreign Keys to maintain data integrity across tables:
*   `Users` holds login accounts. A student user optionally links to a mentor user (`mentor_id`).
*   `Enrollments` links a student to a `Course` in a many-to-many relationship.
*   `Lessons` belong to a single `Course` in a one-to-many relationship.
*   `LessonProgress` records a student's completion state and time spent on a specific `Lesson`.
*   `ActivityEvents` logs discrete study sessions and lesson completion milestones for analytics.

---

## 2. Table Schemas

### users
Stores both students and mentors.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `name` (VARCHAR(100), NOT NULL)
*   `email` (VARCHAR(150), UNIQUE, NOT NULL)
*   `password` (VARCHAR(255), NOT NULL)
*   `role` (ENUM('STUDENT', 'MENTOR'), NOT NULL)
*   `mentor_id` (BIGINT, Foreign Key referencing `users(id)`, Nullable)
*   `created_at` (TIMESTAMP, NOT NULL)
*   `updated_at` (TIMESTAMP, NOT NULL)

### courses
Stores course meta details.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `title` (VARCHAR(150), UNIQUE, NOT NULL)
*   `description` (VARCHAR(1000), Nullable)
*   `category` (VARCHAR(50), NOT NULL)
*   `difficulty` (ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'), NOT NULL)
*   `estimated_hours` (INT, NOT NULL)
*   `created_at` (TIMESTAMP, NOT NULL)

### lessons
Stores the topics belonging to courses.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `course_id` (BIGINT, Foreign Key referencing `courses(id)`, NOT NULL)
*   `title` (VARCHAR(150), NOT NULL)
*   `description` (VARCHAR(1000), Nullable)
*   `order_index` (INT, NOT NULL)
*   `estimated_minutes` (INT, NOT NULL)

### enrollments
Maps student course registration status.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `student_id` (BIGINT, Foreign Key referencing `users(id)`, NOT NULL)
*   `course_id` (BIGINT, Foreign Key referencing `courses(id)`, NOT NULL)
*   `status` (ENUM('ACTIVE', 'COMPLETED', 'PAUSED'), NOT NULL)
*   `enrolled_at` (TIMESTAMP, NOT NULL)

### lesson_progress
Tracks progress indicators per student per lesson.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `student_id` (BIGINT, Foreign Key referencing `users(id)`, NOT NULL)
*   `lesson_id` (BIGINT, Foreign Key referencing `lessons(id)`, NOT NULL)
*   `status` (ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'), NOT NULL)
*   `time_spent_minutes` (INT, DEFAULT 0, NOT NULL)
*   `completed_at` (TIMESTAMP, Nullable)

### activity_events
Detailed audit logs for analytics and streaks.
*   `id` (BIGINT, Primary Key, Auto-Increment)
*   `student_id` (BIGINT, Foreign Key referencing `users(id)`, NOT NULL)
*   `course_id` (BIGINT, Foreign Key referencing `courses(id)`, Nullable)
*   `lesson_id` (BIGINT, Foreign Key referencing `lessons(id)`, Nullable)
*   `event_type` (ENUM('LOGIN', 'LESSON_START', 'STUDY_SESSION', 'LESSON_COMPLETE'), NOT NULL)
*   `duration_minutes` (INT, DEFAULT 0, NOT NULL)
*   `metadata` (VARCHAR(500), Nullable)
*   `event_date` (TIMESTAMP, NOT NULL)

---

## 3. Unique Constraints
*   `users.email` is protected by a unique database constraint to ensure no duplicate accounts exist.
*   `courses.title` is unique to prevent duplicate courses from being created.
*   `enrollments(student_id, course_id)` is a composite unique key preventing a student from enrolling in the same course multiple times.
*   `lesson_progress(student_id, lesson_id)` is a composite unique key ensuring only one progress entry exists per student per lesson.

---

## 4. Indexes & Performance Optimizations

To ensure fast query response times under high load, database indexes are explicitly defined:
1.  `idx_users_email` on `users(email)`: Speeds up authentication lookups during logins.
2.  `idx_users_mentor` on `users(mentor_id)`: Speeds up student cohort queries for the mentor dashboard.
3.  `idx_enrollments_student` and `idx_enrollments_course`: Optimizes many-to-many lookups.
4.  `idx_progress_student` and `idx_progress_lesson`: Speeds up calculations of course completion rates.
5.  `idx_lessons_course_order` on `lessons(course_id, order_index)`: Accelerates sorting and retrieving lessons in chronological order.
6.  `idx_activity_student` and `idx_activity_date`: Optimizes query retrieval for time-series analytics (daily learning trends) and recent activities timelines.
