# Screenshots checklist

Take screenshots at browser zoom 100 percent, full window, with real looking data. Save them in `docs/screenshots/` with the names below (PNG).

| # | File name | Page and what must be visible |
|---|---|---|
| 1 | `01-login.png` | Login page |
| 2 | `02-register.png` | Register page with validation message (for example short password) |
| 3 | `03-user-dashboard.png` | User dashboard: 4 cards, recent complaints, status chart, notifications |
| 4 | `04-submit-complaint.png` | New Complaint form filled with an image preview |
| 5 | `05-complaint-submitted.png` | Success screen with tracking ID and Critical priority badge |
| 6 | `06-tracking-timeline.png` | Track page: progress steps and status timeline |
| 7 | `07-my-complaints.png` | My Complaints with search, filters and pagination |
| 8 | `08-admin-dashboard.png` | Admin dashboard: cards and overdue alerts |
| 9 | `09-admin-complaints.png` | All Complaints with filters, sort and overdue badge, page numbers visible |
| 10 | `10-admin-assign.png` | Complaint detail with department, staff, priority, status controls |
| 11 | `11-admin-users.png` | Users page with the add user form |
| 12 | `12-analytics.png` | Analytics page: all five charts |
| 13 | `13-escalations.png` | Escalations page with history rows |
| 14 | `14-staff-dashboard.png` | Staff dashboard |
| 15 | `15-staff-resolve.png` | Staff complaint detail with accept, progress and resolve boxes |
| 16 | `16-feedback.png` | Feedback page with stars and history |
| 17 | `17-forgot-password.png` | Forgot password and reset password pages |
| 18 | `18-mobile-view.png` | Any page in mobile width (Chrome F12, device toolbar, 375 px) with the sidebar open |
| 19 | `19-api-tests.png` | Terminal showing `Passed: 55  Failed: 0` |
| 20 | `20-unit-tests.png` | Terminal showing `mvn test` with BUILD SUCCESS |
| 21 | `21-database-tables.png` | MySQL Workbench with the 11 tables of complaint_db |

Tips:
- Use realistic titles (Projector not working in Lab 3, Water leakage in corridor), not "test test".
- Hide your email password or anything private. Demo accounts are fine.
- Windows shortcut: Win + Shift + S, select the area, then paste into Paint and save.

## Add them to the README

After saving the images in `docs/screenshots/`, replace the Screenshots section of `README.md` with this:

```markdown
## Screenshots

| | |
|---|---|
| ![Login](docs/screenshots/01-login.png) | ![User dashboard](docs/screenshots/03-user-dashboard.png) |
| ![Submit complaint](docs/screenshots/04-submit-complaint.png) | ![Tracking timeline](docs/screenshots/06-tracking-timeline.png) |
| ![Admin complaints](docs/screenshots/09-admin-complaints.png) | ![Analytics](docs/screenshots/12-analytics.png) |
| ![Escalations](docs/screenshots/13-escalations.png) | ![Staff resolve](docs/screenshots/15-staff-resolve.png) |
```
