# Architecture and database design

The diagrams below are written in Mermaid. GitHub draws them automatically. To get a PNG for a report or PPT, paste the code block into https://mermaid.live and use Actions, PNG.

## 1. System architecture

```mermaid
flowchart LR
    B["Browser<br/>HTML, CSS, JavaScript,<br/>Bootstrap 5, Chart.js"] -->|"REST JSON<br/>Authorization: Bearer JWT"| SEC["Spring Security<br/>CORS + JwtAuthFilter"]
    SEC --> C["Controllers<br/>REST endpoints"]
    C --> S["Services<br/>business rules"]
    S --> R["Repositories<br/>Spring Data JPA"]
    R --> DB[("MySQL 8")]
    S --> N["NotificationService"]
    S --> F["FileStorageService<br/>complaint images"]
    SCH["EscalationScheduler<br/>every 15 minutes"] --> S
```

Layers and responsibilities:

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller` | Maps HTTP requests, validates input (`@Valid`), reads the logged in user, no business logic |
| Service | `service` | All business rules: priority, status transitions, assignment, feedback rules, analytics, escalation, paging |
| Repository | `repository` | Database access with Spring Data JPA, `@EntityGraph` to avoid N+1 queries, `Specification` for search |
| Entity | `entity` | JPA mapping of the 11 tables |
| DTO | `dto` | Request and response objects. Entities and password hashes are never sent to the client |
| Security | `security`, `config` | JWT creation and validation, filter, role rules, CORS, BCrypt |
| Exception | `exception` | One global handler that turns errors into JSON with the correct HTTP status |
| Scheduler | `scheduler` | Periodic overdue complaint check |

## 2. Login and request flow (JWT)

```mermaid
sequenceDiagram
    participant B as Browser
    participant F as JwtAuthFilter
    participant C as Controller
    participant S as Service
    participant D as MySQL
    B->>C: POST /api/auth/login (email, password)
    C->>S: AuthService.login
    S->>D: find user by email
    S-->>C: BCrypt matches, JWT created
    C-->>B: token and user details
    B->>F: GET /api/complaints with Bearer token
    F->>F: validate JWT, load user, set role
    F->>C: authenticated request
    C->>S: ComplaintService.list(userId, role)
    S->>D: query scoped by role
    D-->>S: rows
    S-->>B: JSON response
```

The server keeps no session. Every request carries the token, so the API is stateless.

## 3. Database design (ER diagram)

```mermaid
erDiagram
    DEPARTMENTS ||--o{ USERS : "staff belong to"
    DEPARTMENTS ||--o{ COMPLAINTS : "handles"
    COMPLAINT_CATEGORIES ||--o{ COMPLAINTS : "classifies"
    USERS ||--o{ COMPLAINTS : "submits"
    USERS ||--o{ COMPLAINTS : "assigned to"
    COMPLAINTS ||--o{ COMPLAINT_ATTACHMENTS : "has"
    COMPLAINTS ||--o{ COMPLAINT_ASSIGNMENTS : "assignment log"
    COMPLAINTS ||--o{ STATUS_HISTORY : "timeline"
    COMPLAINTS ||--o{ ESCALATIONS : "escalated"
    COMPLAINTS ||--o| FEEDBACK : "rated once"
    USERS ||--o{ NOTIFICATIONS : "receives"
    USERS ||--o{ FEEDBACK : "gives"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"
    USERS ||--o{ STATUS_HISTORY : "changes status"
    USERS ||--o{ COMPLAINT_ASSIGNMENTS : "staff or assigner"

    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password "BCrypt hash"
        varchar phone
        varchar role "USER, ADMIN, STAFF"
        bit active
        bigint department_id FK
        datetime created_at
        datetime updated_at
    }
    DEPARTMENTS {
        bigint id PK
        varchar name UK
        varchar description
    }
    COMPLAINT_CATEGORIES {
        bigint id PK
        varchar name UK
        varchar default_priority
    }
    COMPLAINTS {
        bigint id PK
        varchar tracking_id UK "CMP-2026-1001"
        varchar title
        text description
        varchar location
        varchar urgency
        varchar priority
        bit priority_overridden
        varchar status
        bigint category_id FK
        bigint department_id FK
        bigint user_id FK "submitted by"
        bigint assigned_staff_id FK
        text resolution_notes
        datetime deadline
        datetime created_at
        datetime updated_at
        datetime resolved_at
    }
    COMPLAINT_ATTACHMENTS {
        bigint id PK
        bigint complaint_id FK
        varchar file_name
        varchar file_path
        varchar content_type
        datetime uploaded_at
    }
    COMPLAINT_ASSIGNMENTS {
        bigint id PK
        bigint complaint_id FK
        bigint department_id FK
        bigint staff_id FK
        bigint assigned_by_id FK
        datetime assigned_at
        datetime accepted_at
    }
    STATUS_HISTORY {
        bigint id PK
        bigint complaint_id FK
        varchar status
        varchar note
        bigint changed_by_id FK
        datetime changed_at
    }
    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        varchar message
        bit is_read
        datetime created_at
    }
    FEEDBACK {
        bigint id PK
        bigint complaint_id FK "unique"
        bigint user_id FK
        int rating "1 to 5"
        varchar comment
        datetime created_at
    }
    ESCALATIONS {
        bigint id PK
        bigint complaint_id FK
        varchar reason
        datetime escalated_at
    }
    PASSWORD_RESET_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token_hash UK "SHA-256"
        datetime expires_at
        bit used
        datetime created_at
    }
```

Design notes:
- Every table has a primary key and foreign keys. Frequently filtered columns are indexed (`complaints.status`, `priority`, `user_id`, `assigned_staff_id`, `notifications(user_id, is_read)`).
- `feedback.complaint_id` is unique, so a complaint can be rated only once.
- `status_history` is append only. It is the source of the tracking timeline.
- Reset tokens are stored only as SHA-256 hashes.

## 4. Complaint life cycle

```mermaid
stateDiagram-v2
    [*] --> SUBMITTED
    SUBMITTED --> UNDER_REVIEW : admin
    SUBMITTED --> ASSIGNED : admin assigns staff
    UNDER_REVIEW --> ASSIGNED : admin assigns staff
    ASSIGNED --> IN_PROGRESS : staff accepts
    IN_PROGRESS --> RESOLVED : staff resolves with notes
    RESOLVED --> CLOSED : admin
    SUBMITTED --> REJECTED : admin
    UNDER_REVIEW --> REJECTED : admin
    CLOSED --> [*]
    REJECTED --> [*]
```

Rules (class `StatusTransitions`, unit tested):
- Staff can only move ASSIGNED to IN_PROGRESS and IN_PROGRESS to RESOLVED, and only on complaints assigned to them.
- Admin can move to any status except out of CLOSED or REJECTED.
- RESOLVED needs resolution notes.
- Assigning a staff member to a new complaint sets the status to ASSIGNED automatically.

## 5. Smart priority and escalation

Priority (`PriorityCalculator`):
1. Start with the higher of the category default priority and the urgency chosen by the user.
2. If the title or description contains a critical keyword (fire, shock, short circuit, gas leak, collapse, electrocut, flood), the priority becomes CRITICAL.
3. If it contains a high keyword (leak, broken, danger, sparking, no water, power cut, accident, urgent), the priority is raised to at least HIGH.
4. Admin can override. The deadline is then recalculated.

Deadlines (`SlaPolicy`, configurable): Critical 24 h, High 72 h, Medium 120 h, Low 168 h from submission.

Escalation (`EscalationScheduler` and `EscalationService`):
1. Every 15 minutes the scheduler loads complaints whose deadline has passed and whose status is not RESOLVED, CLOSED or REJECTED.
2. Complaints that were already escalated are skipped.
3. For each new one an escalation record is saved ("Deadline exceeded by Nh") and every admin gets an in-app notification.
4. Admin can also trigger the same check from the Escalations page.

## 6. Where to find things in the code

| Topic | File |
|---|---|
| JWT create and validate | `security/JwtService.java` |
| Reads the token on every request | `security/JwtAuthFilter.java` |
| Role rules, CORS, BCrypt | `config/SecurityConfig.java` |
| Registration and login | `service/AuthService.java` |
| Priority rules | `service/PriorityCalculator.java` |
| Complaint submit, update, search | `service/ComplaintService.java`, `service/ComplaintSpecs.java` |
| Status rules | `service/StatusTransitions.java` |
| Escalation | `service/EscalationService.java`, `scheduler/EscalationScheduler.java` |
| Analytics | `service/AnalyticsService.java` |
| Password reset | `service/PasswordResetService.java`, `security/ResetTokens.java` |
| Error handling | `exception/GlobalExceptionHandler.java` |
| First run data | `config/DataInitializer.java` |
| Frontend API calls | `complaint-frontend/js/api.js` |
