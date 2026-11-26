-- Smart Complaint Management System: MySQL 8 schema (11 tables)
-- Optional: the app creates these automatically (ddl-auto=update). Use this for review or manual setup.
CREATE DATABASE IF NOT EXISTS complaint_db;
USE complaint_db;

CREATE TABLE departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE complaint_categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  default_priority VARCHAR(20) NOT NULL
);

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  phone VARCHAR(15),
  role VARCHAR(20) NOT NULL,
  active BIT NOT NULL DEFAULT 1,
  department_id BIGINT NULL,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id),
  INDEX idx_users_role (role)
);

CREATE TABLE complaints (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  tracking_id VARCHAR(30) NOT NULL UNIQUE,
  title VARCHAR(150) NOT NULL,
  description TEXT NOT NULL,
  location VARCHAR(150),
  urgency VARCHAR(20) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  priority_overridden BIT NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  category_id BIGINT NOT NULL,
  department_id BIGINT NULL,
  user_id BIGINT NOT NULL,
  assigned_staff_id BIGINT NULL,
  resolution_notes TEXT,
  deadline DATETIME(6),
  created_at DATETIME(6),
  updated_at DATETIME(6),
  resolved_at DATETIME(6),
  CONSTRAINT fk_c_category FOREIGN KEY (category_id) REFERENCES complaint_categories(id),
  CONSTRAINT fk_c_department FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT fk_c_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_c_staff FOREIGN KEY (assigned_staff_id) REFERENCES users(id),
  INDEX idx_complaints_status (status),
  INDEX idx_complaints_priority (priority),
  INDEX idx_complaints_user (user_id),
  INDEX idx_complaints_staff (assigned_staff_id)
);

CREATE TABLE complaint_attachments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  content_type VARCHAR(100),
  uploaded_at DATETIME(6),
  CONSTRAINT fk_att_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
);

CREATE TABLE complaint_assignments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  department_id BIGINT NULL,
  staff_id BIGINT NULL,
  assigned_by_id BIGINT NULL,
  assigned_at DATETIME(6),
  accepted_at DATETIME(6),
  CONSTRAINT fk_asg_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  CONSTRAINT fk_asg_department FOREIGN KEY (department_id) REFERENCES departments(id),
  CONSTRAINT fk_asg_staff FOREIGN KEY (staff_id) REFERENCES users(id),
  CONSTRAINT fk_asg_by FOREIGN KEY (assigned_by_id) REFERENCES users(id)
);

CREATE TABLE status_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  note VARCHAR(500),
  changed_by_id BIGINT NULL,
  changed_at DATETIME(6),
  CONSTRAINT fk_sh_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  CONSTRAINT fk_sh_user FOREIGN KEY (changed_by_id) REFERENCES users(id)
);

CREATE TABLE notifications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  message VARCHAR(300) NOT NULL,
  is_read BIT NOT NULL DEFAULT 0,
  created_at DATETIME(6),
  CONSTRAINT fk_n_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_notifications_user_read (user_id, is_read)
);

CREATE TABLE feedback (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL,
  comment VARCHAR(500),
  created_at DATETIME(6),
  CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
  CONSTRAINT fk_f_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
  CONSTRAINT fk_f_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE escalations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  complaint_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  escalated_at DATETIME(6),
  CONSTRAINT fk_e_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at DATETIME(6) NOT NULL,
  used BIT NOT NULL DEFAULT 0,
  created_at DATETIME(6),
  CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_prt_user (user_id)
);
