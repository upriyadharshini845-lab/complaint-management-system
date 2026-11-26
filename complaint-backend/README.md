# Smart Complaint Management System: Backend (Step 1-2)

Spring Boot 3.3 + Java 17 + Spring Data JPA + MySQL 8. Done so far: project setup, MySQL config, 10 entities, repositories, schema, seed data, health API.

## Run
1. Start MySQL. Open `src/main/resources/application.properties` and set `spring.datasource.password`.
2. In the project folder: `mvn spring-boot:run`  (or open `ComplaintManagementApplication.java` and click Run in VS Code).
3. Wait for `Started ComplaintManagementApplication`.
4. Postman: `GET http://localhost:8080/api/health` -> `{"status":"UP","users":4,"departments":4,"categories":6}`

The database `complaint_db` and all 10 tables are created automatically on first run, then DataInitializer inserts departments, categories and 4 accounts
(admin@cms.com / Admin@123, staff@cms.com / Staff@123, staff2@cms.com / Staff@123, user@cms.com / User@123).

## Folders
`controller/ service/ repository/ entity/ dto/ config/ security/ exception/ scheduler/ enums/` under `src/main/java/com/cms/complaint`.
`database/schema.sql` and `database/seed.sql` are optional manual alternatives.

Note: `SecurityConfig` is intentionally open for now. Step 3 adds JWT and role-based security.
