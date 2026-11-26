const CONFIG={
  // Backend address. Use '/api' when the frontend is served by the Spring Boot JAR itself.
  API_BASE_URL:'http://localhost:8080/api',
  // Resolution deadlines (hours) by priority. The backend owns the real values (app.sla.* in application.properties).
  SLA_HOURS:{Critical:24,High:72,Medium:120,Low:168},
  CATEGORIES:['Technical Issues','Infrastructure','Water and Electricity','Maintenance','Transportation','Other'],
  STATUSES:['SUBMITTED','UNDER_REVIEW','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','REJECTED'],
  PRIORITIES:['Critical','High','Medium','Low']
};
