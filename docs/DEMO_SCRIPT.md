# 5 minute demo script

## Before the demo (10 minutes earlier)

- [ ] MySQL running. Backend started with `mvn spring-boot:run` and shows `Started ComplaintManagementApplication`
- [ ] Frontend open with Live Server at `http://127.0.0.1:5500/login.html`
- [ ] Demo accounts work: user@cms.com, admin@cms.com, staff2@cms.com (Arun, IT Support)
- [ ] 12 or more complaints exist so pagination shows (see "Prepare data")
- [ ] One complaint is overdue so the escalation demo has something to show (see "Prepare data")
- [ ] Open the backend terminal in a visible window (to show the password reset link)
- [ ] Three browser windows or profiles: one each for user, admin, staff, all logged out. Browser zoom 110 percent
- [ ] Laptop charged and not set to sleep. Internet on (Bootstrap and Chart.js load from CDN)
- [ ] Backup: screenshots in `docs/screenshots` in case something fails

### Prepare data

Overdue complaint (Workbench, replace the tracking ID of any open complaint):
```sql
UPDATE complaint_db.complaints SET deadline = NOW() - INTERVAL 5 HOUR WHERE tracking_id = 'CMP-2026-1002';
```
Pagination data: run the 12 demo complaints PowerShell command from the Step 8 instructions, or run `tests\api-tests.ps1` once.

## Timeline

| Time | What you do | What you say |
|---|---|---|
| 0:00 | Show login page | "This is the Smart Complaint Management System. It has three roles: user, staff and admin. Backend is Spring Boot with JWT security and MySQL, frontend is Bootstrap." |
| 0:30 | User window: login as the user. **New Complaint**: title `Fire in lab 2`, category Technical Issues, urgency Low, location, description, attach an image. Submit | "The user submits a complaint with an image. I chose urgency Low, but the word fire in the text makes the system raise it to **Critical** automatically. That is the smart priority rule." Point at the tracking ID and the Critical badge |
| 1:15 | **Track Complaint**: paste the ID | "Every complaint gets a unique tracking ID. The timeline is built from the status history table." |
| 1:35 | Admin window: login as admin. **All Complaints**. Type `fire` in search, use the Priority filter, change sort, show page 2 | "The admin list is searched, filtered, sorted and paged on the server, so it stays fast with thousands of complaints." |
| 2:05 | Open the complaint. Department IT Support, staff Arun M, Save | "The admin assigns department and staff. The status moves to Assigned and the staff member gets a notification." |
| 2:40 | Staff window: login as staff2. **Assigned Complaints**, open it. Accept. Add a progress note. Resolve with notes | "Staff sees only their own complaints. They accept, update progress and resolve. Resolution notes are mandatory. A staff member cannot touch other people's complaints, the server enforces that." |
| 3:25 | User window: refresh Track page, show RESOLVED and timeline. Open **Feedback**, give 5 stars | "The user sees the full history and rates the service. One rating per complaint." |
| 3:50 | Admin window: **Analytics** | "Charts show complaints by category, per month, pending versus resolved, department performance, average resolution time and satisfaction." |
| 4:15 | Admin: **Escalations**, click Run escalation check. Show the history and the bell notification | "A scheduler checks every 15 minutes for complaints past their deadline. Deadline depends on priority. Overdue ones are escalated to admins with a record kept." |
| 4:40 | Optional: forgot password, show the link in the backend terminal | "Reset links expire in 30 minutes, work once, and only a hash is stored." |
| 4:50 | Close | "To summarise: layered architecture, JWT with role based access, smart priority, escalation scheduler, analytics. It has 55 automated API tests and unit tests for the business rules. Thank you." |

## If something goes wrong

| Problem | Quick fix |
|---|---|
| "Cannot reach the server" | Backend stopped. Start it again, wait for `Started ComplaintManagementApplication`, refresh |
| Login fails | Check the email and password in the README. Check the backend terminal for errors |
| Page looks unstyled | No internet (CDN). Use the screenshots |
| Chart is empty | Not enough data. Submit and resolve a complaint first |

## Likely questions during the demo

See `docs/INTERVIEW_QA.md`. Keep `docs/ARCHITECTURE.md` open to show the ER diagram and the flow.
