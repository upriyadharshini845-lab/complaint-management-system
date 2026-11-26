-- Optional manual seed (the app's DataInitializer already does this when tables are empty).
-- Run only if tables are empty. Passwords are BCrypt hashes: Admin@123 / Staff@123 / User@123
USE complaint_db;
INSERT INTO departments (name) VALUES ('IT Support'),('Electrical & Water'),('Civil & Maintenance'),('Transport');
INSERT INTO complaint_categories (name, default_priority) VALUES
 ('Technical Issues','MEDIUM'),('Infrastructure','MEDIUM'),('Water and Electricity','HIGH'),
 ('Maintenance','LOW'),('Transportation','MEDIUM'),('Other','LOW');
INSERT INTO users (name,email,password,role,active,department_id,created_at,updated_at) VALUES
 ('Admin','admin@cms.com','$2a$10$REt1VlLp1pdFuyYT0bBQCOovqcsQYXlRsKt1iqrKfz32c9FmrK0JO','ADMIN',1,NULL,NOW(),NOW()),
 ('Priya S','staff@cms.com','$2a$10$uNnHZuB3L15vp1M.fYc9.uLNC1qAYnRlLf1SySGVhdtbUSkOoRk.m','STAFF',1,2,NOW(),NOW()),
 ('Arun M','staff2@cms.com','$2a$10$uNnHZuB3L15vp1M.fYc9.uLNC1qAYnRlLf1SySGVhdtbUSkOoRk.m','STAFF',1,1,NOW(),NOW()),
 ('Ravi Kumar','user@cms.com','$2a$10$4r4r3hIiCdDJ7q5HAm.zaO80I/krGJGtn6X8ALRSe0y8Yjez/Ku2G','USER',1,NULL,NOW(),NOW());
