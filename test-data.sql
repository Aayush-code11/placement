-- Test Data for Placement System
-- Run this in MySQL Workbench or command line to create demo data

USE placement_db;

-- Create Admin User (password: admin123)
INSERT INTO app_user (username, password, role) VALUES 
('admin', '$2a$10$XQ5L5YHZ8Y5kKZ5YHZ8Y5O5L5YHZ8Y5kKZ5YHZ8Y5O5L5YHZ8Y5k.', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;

-- Create Student Users (password: student123)
INSERT INTO app_user (username, password, role) VALUES 
('student1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT'),
('student2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT'),
('student3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT')
ON DUPLICATE KEY UPDATE username=username;

-- Get user IDs
SET @student1_user_id = (SELECT id FROM app_user WHERE username = 'student1');
SET @student2_user_id = (SELECT id FROM app_user WHERE username = 'student2');
SET @student3_user_id = (SELECT id FROM app_user WHERE username = 'student3');

-- Create Student Profiles (Delete existing first)
DELETE FROM student WHERE roll IN ('CS001', 'IT002', 'CS003');

INSERT INTO student (name, roll, branch, cgpa, email, user_id) VALUES
('Rahul Kumar', 'CS001', 'Computer Science', 8.5, 'rahul@example.com', @student1_user_id),
('Priya Sharma', 'IT002', 'Information Technology', 9.0, 'priya@example.com', @student2_user_id),
('Amit Patel', 'CS003', 'Computer Science', 7.8, 'amit@example.com', @student3_user_id);

-- Create Company Users (password: company123)
INSERT INTO app_user (username, password, role) VALUES 
('tcs', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COMPANY'),
('infosys', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COMPANY'),
('wipro', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COMPANY')
ON DUPLICATE KEY UPDATE username=username;

-- Get company user IDs
SET @tcs_user_id = (SELECT id FROM app_user WHERE username = 'tcs');
SET @infosys_user_id = (SELECT id FROM app_user WHERE username = 'infosys');
SET @wipro_user_id = (SELECT id FROM app_user WHERE username = 'wipro');

-- Create Company Profiles (Delete existing first)
DELETE FROM company WHERE name IN ('TCS (Tata Consultancy Services)', 'Infosys', 'Wipro', 'Amazon', 'Google');

INSERT INTO company (name, hr_email, role, min_cgpa, branches, ctc, location, job_description, openings, user_id) VALUES
('TCS (Tata Consultancy Services)', 'hr@tcs.com', 'Software Developer', 7.0, 'Computer Science,Information Technology', '3.5 LPA', 'Mumbai, Pune, Bangalore', 'Develop and maintain software applications using Java, Python, and web technologies.', 50, @tcs_user_id),
('Infosys', 'recruitment@infosys.com', 'System Engineer', 7.5, 'Computer Science,Information Technology,Electronics', '4.0 LPA', 'Bangalore, Hyderabad', 'Work on system integration, testing, and deployment of enterprise applications.', 40, @infosys_user_id),
('Wipro', 'careers@wipro.com', 'Full Stack Developer', 8.0, 'Computer Science,Information Technology', '4.5 LPA', 'Bangalore, Chennai', 'Design and develop full-stack web applications using modern frameworks.', 30, @wipro_user_id),
('Amazon', 'jobs@amazon.com', 'SDE-1', 8.5, 'Computer Science', '12 LPA', 'Bangalore, Hyderabad', 'Build scalable distributed systems and work on core Amazon products.', 10, NULL),
('Google', 'hiring@google.com', 'Software Engineer', 9.0, 'Computer Science', '18 LPA', 'Bangalore', 'Work on innovative projects that impact billions of users worldwide.', 5, NULL);

-- Create Sample Applications
DELETE FROM job_application WHERE id > 0;

SET @student1_id = (SELECT id FROM student WHERE roll = 'CS001' LIMIT 1);
SET @student2_id = (SELECT id FROM student WHERE roll = 'IT002' LIMIT 1);
SET @student3_id = (SELECT id FROM student WHERE roll = 'CS003' LIMIT 1);

SET @tcs_id = (SELECT id FROM company WHERE name = 'TCS (Tata Consultancy Services)' LIMIT 1);
SET @infosys_id = (SELECT id FROM company WHERE name = 'Infosys' LIMIT 1);

INSERT INTO job_application (student_id, company_id, status) VALUES
(@student1_id, @tcs_id, 'Applied'),
(@student1_id, @infosys_id, 'Shortlisted'),
(@student2_id, @tcs_id, 'Selected'),
(@student3_id, @infosys_id, 'Applied');

SELECT 'Test data created successfully!' as message;
SELECT 'Login credentials:' as info;
SELECT 'Student 1 - Username: student1, Password: student123' as credentials;
SELECT 'Student 2 - Username: student2, Password: student123' as credentials;
SELECT 'Student 3 - Username: student3, Password: student123' as credentials;
SELECT 'Admin - Username: admin, Password: admin123' as credentials;
SELECT 'Company TCS - Username: tcs, Password: company123' as credentials;
