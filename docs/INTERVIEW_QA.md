# Interview questions and answers

Answers are written in simple words so you can say them in your own style. Use real examples from the project.

## A. Project overview

**1. Explain your project in one minute.**
It is a complaint management system for a college or organisation. A user registers and submits a complaint with a title, category, description, location and an image. The system gives a tracking ID and calculates a priority automatically. The admin assigns the complaint to a department and a staff member. The staff accepts it, updates progress and resolves it. The user tracks every step on a timeline and gives a rating. The admin sees analytics, and a scheduler escalates complaints that cross their deadline. It is built with Spring Boot, MySQL, JWT security and a Bootstrap frontend.

**2. What problem does it solve?**
Complaints on paper or WhatsApp get lost and nobody knows the status. This gives one place with a tracking ID, a clear owner, deadlines, and reports to see which department is slow.

**3. What are the user roles and what can each do?**
USER submits and tracks complaints and gives feedback. STAFF sees only complaints assigned to them and can accept, add notes and resolve. ADMIN sees everything, assigns, changes priority and status, manages users and departments, and sees analytics and escalations.

**4. What was the hardest part?**
Keeping lazy loaded JPA data safe with `open-in-view=false`, and getting the status rules right so that each role can do only its own actions. I solved the first with DTO mapping inside `@Transactional` services and `@EntityGraph`, and the second with a separate `StatusTransitions` class that I unit tested.

## B. Architecture

**5. Which architecture did you use?**
Layered architecture: controller, service, repository, entity, with DTOs between the controller and the outside world. Controllers only handle HTTP, services hold business rules, repositories talk to the database.

**6. Why DTOs and not returning entities?**
Entities contain the password hash and lazy relations. DTOs expose only what the client needs, avoid lazy loading errors and let the API change without changing the database.

**7. Why did you separate frontend and backend?**
The backend is a REST API, so any client (web, mobile) can use it. The frontend is static files that call the API with `fetch`. I can also bundle the frontend into the JAR for a single deployment.

**8. What does `@Transactional` do in your services?**
It keeps one database transaction and session open for the method. I need it because lazy relations (category, user, department) are loaded while mapping to a DTO. `readOnly = true` is used for queries.

**9. What is `open-in-view=false` and why?**
By default Spring keeps the session open during the whole web request, which hides lazy loading and keeps connections longer. I turned it off so all database work happens in the service layer, which is cleaner.

## C. Security

**10. How does authentication work?**
The user logs in with email and password. The service checks the password with BCrypt. If it matches, the server creates a signed JWT containing the email, user id and role, valid for 24 hours. The frontend stores it and sends `Authorization: Bearer <token>` with every request. `JwtAuthFilter` validates the token and sets the logged in user for that request.

**11. What is JWT?**
A signed token with three parts: header, payload (claims) and signature. The server signs it with a secret key. If someone changes the payload, the signature check fails. It lets the server stay stateless because no session is stored.

**12. Authentication vs authorization?**
Authentication is who you are (login). Authorization is what you may do (roles). I check roles with `@PreAuthorize` on endpoints and also inside services, for example staff can open only complaints assigned to them.

**13. How are passwords stored?**
Only as BCrypt hashes. BCrypt adds a random salt and is deliberately slow, so brute force is hard. The hash is never returned by the API.

**14. How does forgot password work securely?**
The server creates a random 256 bit token, stores only its SHA-256 hash with an expiry of 30 minutes, and sends the link. The token works once. The response is the same whether the email exists or not, so nobody can find which emails are registered. After a reset, all other unused links of that user are invalidated.

**15. What is CORS and how did you handle it?**
Browsers block a page on one origin (127.0.0.1:5500) from calling an API on another origin (localhost:8080) unless the API allows it. I configured allowed origins in `SecurityConfig` from `app.cors.allowed-origins`.

**16. Why is CSRF disabled?**
CSRF attacks rely on browser cookies sent automatically. My API uses a bearer token in a header and no cookies, so CSRF does not apply.

**17. Any security weakness you know of?**
The token is kept in localStorage, which can be read if the site has an XSS bug. A safer option is an httpOnly cookie with refresh tokens. I also escape all user text before showing it in the page. Rate limiting on login is not added yet.

## D. Database

**18. Explain your database design.**
11 normalised tables: users, departments, complaint_categories, complaints, complaint_attachments, complaint_assignments, status_history, notifications, feedback, escalations, password_reset_tokens. Complaints link to the submitting user, the assigned staff, the category and the department. `status_history` is an append only log used for the timeline. See the ER diagram in `docs/ARCHITECTURE.md`.

**19. Which indexes did you add and why?**
On `complaints.status`, `priority`, `user_id`, `assigned_staff_id` because list screens filter on them, and on `notifications(user_id, is_read)` for the unread count. Unique indexes on email, tracking ID and token hash.

**20. How do you stop one complaint getting two ratings?**
`feedback.complaint_id` is unique in the database, and the service also checks first and returns HTTP 409.

**21. What is the N+1 problem and how did you avoid it?**
Loading a list of complaints and then one extra query per complaint for its category, user and department. I used `@EntityGraph` on the repository methods so these are fetched in one query.

## E. Business logic

**22. How does the smart priority work?**
Take the higher of the category default and the urgency from the user. If the text has a critical keyword such as fire or short circuit, it becomes Critical. If it has a high keyword such as leak, it becomes at least High. Keywords and deadlines are configurable in `application.properties`. Admin can override.

**23. How does escalation work?**
A scheduled task runs every 15 minutes. It finds complaints past their deadline that are not resolved, closed or rejected, skips those already escalated, saves an escalation record and notifies all admins. Deadlines depend on priority: 24, 72, 120 and 168 hours.

**24. How is the tracking ID generated?**
The complaint is saved first to get its database id, then the ID is formatted as `CMP-<year>-<1000 + id>`, for example CMP-2026-1001.

**25. How do you stop staff or users seeing other people's data?**
The list query is scoped by role: user sees own complaints, staff sees assigned ones, admin sees all. The detail endpoint checks the same rule and returns 403 otherwise. This is covered by my API tests.

**26. How does pagination and search work?**
`GET /api/complaints/search` takes page, size, sort, filters and a text query. I build a JPA `Specification` from the filters and call `findAll(spec, pageable)`, so the database does the paging. Size is limited to 50 and sort fields are whitelisted so a client cannot sort by any column.

**27. How do you handle errors?**
A `@RestControllerAdvice` converts exceptions into JSON with the right status: 400 for validation, 401 not logged in, 403 not allowed, 404 not found, 409 conflict. The frontend shows the message to the user.

## F. Testing

**28. How did you test the project?**
Two levels. JUnit tests for business rules that need no database: priority calculation, status transitions, tracking ID format, analytics maths, escalation reason, reset tokens and paging limits. Then 55 automated API checks (`tests/api-tests.ps1`) against the running backend covering login, roles, complaint flow, feedback, analytics, escalation and pagination.

**29. Why test with a script and not only in the browser?**
A script repeats the same checks in a minute after every change, so I know I did not break an old feature.

## G. Improvements

**30. What would you add next?**
Real email notifications (JavaMailSender), refresh tokens and httpOnly cookies, login rate limiting, Flyway database migrations, Swagger documentation, Docker for one command setup, integration tests with a test database, and image storage in cloud storage.

**31. How would you deploy it?**
Build the JAR with `mvn clean package`, run it with the `prod` profile and environment variables for secrets, put Nginx with HTTPS in front, use a MySQL user that is not root, and keep the upload folder on persistent storage. Details are in `DEPLOYMENT.md`.

## H. Quick basics you may be asked

- **Spring Boot vs Spring:** Boot gives auto configuration, an embedded server and starters, so you run a JAR without XML setup.
- **`@RestController`:** controller whose methods return data (JSON) instead of views.
- **Dependency injection:** Spring creates objects and passes them in through the constructor, so classes stay loosely coupled and easy to test.
- **JPA vs Hibernate:** JPA is the specification, Hibernate is the implementation Spring Data uses.
- **`@Entity`, `@Id`, `@ManyToOne`:** map a class to a table, mark the primary key, and define a many to one foreign key.
- **Lazy vs eager loading:** lazy loads related data only when used, eager loads it immediately.
- **PUT vs PATCH:** PUT replaces the whole resource, PATCH changes only the sent fields. I use PATCH for complaint updates.
- **HTTP status codes:** 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict.
- **Bean Validation:** annotations such as `@NotBlank`, `@Size`, `@Email` checked automatically with `@Valid`.
