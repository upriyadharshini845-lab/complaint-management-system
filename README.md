# Smart Complaint Management and Resolution System

A full stack web application where users register complaints, track their progress and give feedback, while administrators and department staff assign, resolve and analyse them.

Final year B.Tech Information Technology project. Java 17, Spring Boot 3, MySQL 8, Bootstrap 5.

## Features

| Module | What it does |
|---|---|
| Authentication | Register, login, JWT, BCrypt password hashing, role based access (USER, ADMIN, STAFF), profile, forgot and reset password |
| Complaints | Submit with title, category, description, location, department and image. Unique tracking ID (CMP-2026-1001) |
| Tracking | Search by tracking ID, current status, assigned department and staff, status history, visual progress timeline |
| Smart priority | Priority (Critical, High, Medium, Low) calculated from category, urgency chosen by the user and configurable keywords. Admin can override |
| Admin | All complaints with server side search, filters, sorting and pagination. Assign department and staff, change priority and status. Manage users and departments |
| Staff | See assigned complaints, accept, add progress notes, resolve with resolution notes, view history |
| Notifications | In-app notifications for submission, status changes, assignment and overdue complaints |
| Feedback | Rating from 1 to 5 after resolution, feedback history, average satisfaction |
| Analytics | Chart.js dashboards: category pie, monthly bar, pending vs resolved, department performance, average resolution time, satisfaction |
| Smart escalation | Deadlines per priority (configurable). A scheduled task finds overdue complaints, escalates them to admins and keeps an escalation history |

## Tech stack

- Frontend: HTML5, CSS3, JavaScript, Bootstrap 5, Chart.js
- Backend: Java 17, Spring Boot 3.3, Spring MVC, Spring Data JPA, Spring Security, JWT (jjwt), Bean Validation, Maven
- Database: MySQL 8 (11 tables)
- Tools: VS Code, Postman or PowerShell, Git and GitHub

## Architecture

Layered backend: `controller` (REST API) -> `service` (business rules) -> `repository` (Spring Data JPA) -> `entity` (MySQL). DTOs are used for every request and response, so passwords and internal fields are never exposed. A global exception handler returns clean JSON errors with correct HTTP status codes. Security is stateless: the frontend sends `Authorization: Bearer <token>` with every request.

```
cms/
├── complaint-backend/
│   ├── pom.xml
│   ├── database/            schema.sql, seed.sql
│   └── src/main/java/com/cms/complaint/
│       ├── controller/      REST endpoints
│       ├── service/         business logic (priority, status rules, analytics, escalation, paging)
│       ├── repository/      Spring Data JPA
│       ├── entity/          JPA entities
│       ├── dto/             request and response objects
│       ├── config/          security, CORS, first-run data
│       ├── security/        JWT filter and service
│       ├── exception/       global error handling
│       └── scheduler/       overdue complaint check
├── complaint-frontend/      user/ admin/ staff/ dashboards, css/, js/
├── tests/api-tests.ps1      55 automated API checks
├── README.md
└── DEPLOYMENT.md
```

## Run it locally

Prerequisites: JDK 17, Maven 3.9+, MySQL 8, VS Code with the Live Server extension.

1. Start MySQL. The database `complaint_db` is created automatically.
2. Backend password: create `complaint-backend/src/main/resources/application-local.properties` (it is git ignored):
   ```
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```
3. Start the backend:
   ```
   cd complaint-backend
   mvn spring-boot:run
   ```
   Check http://localhost:8080/api/health
4. Open `complaint-frontend` in VS Code and click "Go Live" on `index.html` (http://127.0.0.1:5500).

Tables, departments, categories and the demo accounts are created on the first run.

### Demo accounts (development only)

| Role | Email | Password |
|---|---|---|
| Admin | admin@cms.com | Admin@123 |
| Staff | staff@cms.com / staff2@cms.com | Staff@123 |
| User | user@cms.com | User@123 |

Public registration always creates a USER. Admin creates staff and other admins from the Users page.

### Forgot password

The reset link is written to the backend console (no mail server needed for the demo). Copy it into the browser. Links are valid for 30 minutes and work only once.

## Configuration

| Property | Default | Meaning |
|---|---|---|
| `spring.datasource.*` | local MySQL | Database connection |
| `app.jwt.secret` | sample value | Signing key. Change it before deploying |
| `app.jwt.expiration-ms` | 86400000 | Token lifetime (24 hours) |
| `app.cors.allowed-origins` | Live Server ports | Frontend origins allowed to call the API |
| `app.sla.critical/high/medium/low` | 24 / 72 / 120 / 168 | Resolution deadline in hours |
| `app.priority.critical-keywords`, `app.priority.high-keywords` | see code | Words that raise priority |
| `app.escalation.interval-ms` | 900000 | How often overdue complaints are checked |
| `app.reset.token-minutes` | 30 | Password reset link lifetime |
| `app.seed.demo-users` | true | Create demo staff and user accounts on first run |
| `app.admin.email`, `app.admin.password` | admin@cms.com / Admin@123 | First admin account |

## REST API

All endpoints except those marked public need a JWT.

| Area | Endpoints |
|---|---|
| Auth (public) | `POST /api/auth/register`, `/login`, `/forgot-password`, `/reset-password` |
| Profile | `GET /api/auth/me`, `PUT /api/users/me` |
| Complaints | `POST /api/complaints` (USER), `GET /api/complaints`, `GET /api/complaints/search` (page, size, sort, dir, status, priority, category, department, q, overdue), `GET /api/complaints/track/{id}`, `PATCH /api/complaints/{id}` (ADMIN, STAFF) |
| Users | `GET /api/users`, `POST /api/users`, `PATCH /api/users/{id}/toggle` (ADMIN) |
| Departments | `GET /api/departments`, `POST`, `DELETE /api/departments/{id}` (ADMIN) |
| Notifications | `GET /api/notifications`, `PATCH /api/notifications/read-all` |
| Feedback | `POST /api/feedback` (USER), `GET /api/feedback` |
| Analytics | `GET /api/analytics` (ADMIN) |
| Escalations | `GET /api/escalations`, `POST /api/escalations/run` (ADMIN) |
| Health (public) | `GET /api/health` |

## Testing

- Unit tests (priority rules, status transitions, tracking IDs, analytics, escalation, reset tokens, paging): `cd complaint-backend` then `mvn test`
- API tests (55 checks against the running backend): `powershell -ExecutionPolicy Bypass -File .\tests\api-tests.ps1`

## Security notes

- Passwords are stored only as BCrypt hashes. Reset tokens are stored only as SHA-256 hashes, are random, expire and work once.
- Role rules are enforced on the server for every endpoint. Staff can touch only complaints assigned to them, users only their own.
- Input validation and a global exception handler. Page size and sort fields are limited.
- Before deploying: change `app.jwt.secret`, the admin password, turn demo users off. See DEPLOYMENT.md.

## Database

`complaint-backend/database/schema.sql` holds the 11 tables (users, departments, complaint_categories, complaints, complaint_attachments, complaint_assignments, status_history, notifications, feedback, escalations, password_reset_tokens). The application also creates them automatically (`spring.jpa.hibernate.ddl-auto=update`). `seed.sql` is an optional manual seed.

## Documentation

- [Architecture, ER diagram, flows](docs/ARCHITECTURE.md)
- [Deployment guide](DEPLOYMENT.md)
- [Interview questions and answers](docs/INTERVIEW_QA.md)
- [5 minute demo script](docs/DEMO_SCRIPT.md)
- [Screenshots checklist](docs/SCREENSHOTS_CHECKLIST.md)

## Screenshots

Add screenshots here after taking them (see docs/SCREENSHOTS_CHECKLIST.md for the list and a ready to paste block).
