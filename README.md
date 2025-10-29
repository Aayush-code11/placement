# Training and Placement Management System (Minimal)

This is a minimal, simple implementation of the Training & Placement Management System requested. It uses:

- Spring Boot (backend)
- Spring Data JPA (persistence)
- MySQL (database)
- Static frontend files (HTML/CSS/JS) stored under `src/main/resources/static` as requested.

What you get
- Simple entities: Student, Company, JobApplication
- Basic form handlers to register students/companies and to apply for jobs
- Static pages: `index.html`, `student.html`, `company.html`, `admin.html` under `src/main/resources/static`

How to run
1. Install Java 11 (or set `java.version` in `pom.xml` to match your installed JDK).
2. Install MySQL and create a database (example):

   CREATE DATABASE placement_db;

3. Update DB credentials in `src/main/resources/application.properties` (username/password).
4. From project root run:

   mvn spring-boot:run

   or build jar then run:

   mvn package
   java -jar target/placement-system-0.0.1-SNAPSHOT.jar

5. Open browser to http://localhost:8080/

Sample accounts created at startup (for demo):

- admin / admin  (ADMIN)
- student / student (STUDENT)
- company / company (COMPANY)

Authentication and security
- This project now integrates Spring Security. Passwords are stored hashed (BCrypt) but you can log in using the plaintext credentials above (they are encoded on startup).
- The login page is at `/login`. Spring Security handles authentication and redirects users by role.

Notes and next steps
- This is intentionally minimal and simple: authentication is simple and for demo only (no password hashing). Use caution.
- The project now includes resume upload (stored to local `uploads/`), CSV export for admin, and simple role-based login.

Admin CSV export: http://localhost:8080/admin/export/students.csv (must be logged in as ADMIN)

Resume upload: Student profile form allows uploading a PDF resume; uploads are saved to `uploads/` and can be downloaded via admin/company after login.

If you want, I can:
- Improve authentication (add Spring Security and hashed passwords)
- Add resume file validation and virus scanning (demo only)
- Add better UI and pagination for lists
