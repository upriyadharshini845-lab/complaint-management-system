# CMRS Frontend

Plain HTML5, CSS3, JavaScript, Bootstrap 5 and Chart.js (loaded from CDN). No build step.

Run: open this folder in VS Code and click "Go Live" (Live Server) on index.html. The backend must be running.
Backend address: js/config.js (API_BASE_URL).

Structure: user/ admin/ staff/ (role dashboards), css/style.css, js/ (api, auth, layout, utils, pages), images/
Each HTML page only calls one function from js/pages.js.
