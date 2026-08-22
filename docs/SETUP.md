# Local Setup Guide

This document describes the steps required to configure, build, and run the **Progressive Student Dashboard** application locally.

---

## 1. System Requirements

Ensure the following tools are installed on your host system:
*   **Java:** JDK 21 (Temurin, Corretto, or Oracle)
*   **Node.js:** v18.x or later (includes `npm`)
*   **Database:** MySQL Server 8.0+ or Docker Desktop
*   **Build Tool:** Maven (or use the supplied `mvnw` wrapper)

---

## 2. Database Configuration

By default, the application runs on MySQL. A containerized configuration is provided in `docker-compose.yml` for convenience.

### Option A: Run via Docker Compose
To start the containerized MySQL instance:
```bash
docker-compose up -d
```
This spins up a container mapping port `3306:3306` with database `student_dashboard` and password `rootpassword`.

### Option B: Local MySQL Setup
1.  Connect to your local MySQL instance:
    ```sql
    CREATE DATABASE student_dashboard;
    ```
2.  Override the default credentials by setting environment variables:
    *   `DB_URL`: `jdbc:mysql://localhost:3306/student_dashboard?useSSL=false&serverTimezone=UTC`
    *   `DB_USERNAME`: `<your_db_user>`
    *   `DB_PASSWORD`: `<your_db_password>`

---

## 3. Run Backend

1.  Navigate to the backend directory:
    ```bash
    cd backend
    ```
2.  Launch the application:
    *   **In-Memory Mode (H2 - Default):** Run directly without database setup:
        ```bash
        .\mvnw.cmd spring-boot:run
        ```
    *   **MySQL Mode:** Run with the `mysql` profile:
        ```bash
        .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
        ```
    *(For macOS/Linux, run `./mvnw` instead of `.\mvnw.cmd`)*

### Run Backend Jar Package Directly
If running the packaged JAR, you can select the active profile as:
```bash
java -jar target/studentdashboard-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql
```

---

## 4. Run Frontend

1.  Navigate to the frontend directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Launch the Vite dev server:
    ```bash
    npm run dev
    ```
    This starts the web server locally on [http://localhost:5173/](http://localhost:5173/).

---

## 5. Demo Logins

*   **Mentor Console:**
    *   Email: `mentor@example.com`
    *   Password: `password`
*   **Student Console:**
    *   Register a student on the sign-up page (`/register`), which will automatically assign you to the default mentor.
