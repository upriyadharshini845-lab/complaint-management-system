# Deployment guide

## 1. Build the backend JAR

```
cd complaint-backend
mvn clean package
```

The runnable file is `target/complaint-management-0.0.1-SNAPSHOT.jar`. Add `-DskipTests` to skip tests.

## 2. Prepare MySQL

Create a dedicated database user instead of using root:

```sql
CREATE DATABASE complaint_db;
CREATE USER 'cms_user'@'%' IDENTIFIED BY 'a-strong-password';
GRANT ALL PRIVILEGES ON complaint_db.* TO 'cms_user'@'%';
FLUSH PRIVILEGES;
```

Tables are created on the first start. To create them by hand, run `database/schema.sql`.

## 3. Run with the production profile

All secrets are read from environment variables (`application-prod.properties`).

Windows PowerShell:
```
$env:DB_PASSWORD="a-strong-password"
$env:DB_USERNAME="cms_user"
$env:JWT_SECRET="a-long-random-text-of-at-least-40-characters"
$env:ADMIN_PASSWORD="a-strong-admin-password"
$env:CORS_ORIGINS="https://your-frontend-address"
java -jar target/complaint-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Linux:
```
export DB_PASSWORD=... DB_USERNAME=cms_user JWT_SECRET=... ADMIN_PASSWORD=... CORS_ORIGINS=https://your-frontend-address
java -jar target/complaint-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

| Variable | Required | Meaning |
|---|---|---|
| `DB_PASSWORD` | yes | Database password |
| `JWT_SECRET` | yes | Random text, 32 characters or more |
| `ADMIN_PASSWORD` | yes | Password of the first admin account |
| `DB_URL`, `DB_USERNAME` | no | Defaults to local MySQL and root |
| `ADMIN_EMAIL` | no | Defaults to admin@cms.com |
| `CORS_ORIGINS` | no | Comma separated frontend addresses |
| `FRONTEND_URL` | no | Used in password reset links |
| `UPLOAD_DIR` | no | Folder for complaint images. Keep it on persistent storage |

The server listens on port 8080 (`server.port`). Check `http://localhost:8080/api/health`.

The admin account is created once, when the users table is empty. Demo accounts are not created in the prod profile.

## 4. Deploy the frontend (choose one)

### Option A: one JAR for everything

1. Copy the contents of `complaint-frontend` into `complaint-backend/src/main/resources/static/`.
2. In `static/js/config.js` change `API_BASE_URL` to `'/api'`.
3. Run `mvn clean package` again and start the JAR. Open `http://localhost:8080/login.html`.

The frontend and the API share one address, so no CORS setup is needed.

### Option B: separate static hosting

Upload `complaint-frontend` to any static host (Netlify, GitHub Pages, Nginx, Apache). In `js/config.js` set `API_BASE_URL` to the public backend address, for example `https://api.example.com/api`, and add the frontend address to `CORS_ORIGINS`.

## 5. Production checklist

- [ ] Strong values for `JWT_SECRET`, `DB_PASSWORD`, `ADMIN_PASSWORD`
- [ ] Demo accounts are off (prod profile)
- [ ] HTTPS in front of the backend (reverse proxy such as Nginx)
- [ ] MySQL user is not root, database backups are scheduled
- [ ] `UPLOAD_DIR` is on persistent storage and backed up
- [ ] Reset links are written to the log. To send real emails, replace the log line in `PasswordResetService` with a `JavaMailSender` call
- [ ] `tests/api-tests.ps1` passes against the deployed backend (needs the demo accounts, so run it on a test copy)

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| `Port 8080 was already in use` | Stop the old process: `netstat -ano \| findstr LISTENING \| findstr :8080`, then `taskkill /PID <pid> /F`, or set `server.port` |
| `Access denied for user` | Wrong `DB_PASSWORD` or user without rights |
| `Could not resolve placeholder 'JWT_SECRET'` | A required environment variable is missing |
| Browser shows "Failed to fetch" | Backend is not running, or the frontend address is missing in `CORS_ORIGINS` |
