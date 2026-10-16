# Student Course Registration Portal
**Web Technologies Laboratory Course-End Project**  
*Department of Information Technology — 3rd Year B.Tech*

---

## 1. Project Title & Objective

**Title:** Student Course Registration Portal  
**Objective:** To design and build a functional, database-driven web application where students can securely log in, view available curriculum courses, register for courses, track accumulated credits, and drop courses. The system also equips administrators/faculty with capabilities to manage course offerings (CRUD operations) and audit active student enrollments.

The portal is implemented strictly adhering to the concepts taught in the **Web Technologies Laboratory** curriculum, balancing clarity, functional rigor, and defendability during laboratory viva voce examinations.

---

## 2. Features

### Student Portal
1. **Authentication & Session Security:**
   - Login with Student ID (`24881A1203`) and Password.
   - Client-side JavaScript validation with Regular Expression checking.
   - Session creation via `HttpSession` with session-timeout and access controls.
   - Logout terminating the active session.
2. **Dynamic Student Dashboard (`dashboard.jsp`):**
   - Personalized welcome greeting with student details (Roll No, Branch, Year, Semester).
   - Summary stat cards showing **Total Registered Courses** and **Total Registered Credits**.
   - Live table of currently enrolled courses.
3. **Course Catalog & Registration (`courses.jsp`):**
   - Displays all curriculum courses pulled from MySQL.
   - Application-level duplicate enrollment detection.
   - Dynamic badges: displays **Enrolled** for active subjects and a **Register** button for available subjects.
4. **Enrolled Courses Management (`mycourses.jsp`):**
   - Displays student's enrolled courses with credit totals.
   - **Drop Course** action with JavaScript confirmation prompt (`confirm()`).
   - Secure deletion: students can only modify their own enrollments.
5. **Student Profile (`profile.jsp`):**
   - Clean, read-only academic card displaying student roll number, full name, branch, year, semester, and college email.

### Administrator Portal
1. **Admin Authentication:**
   - Dedicated admin login (`admin-login.html`, `AdminLoginServlet`) with credentials (`admin` / `admin123`).
   - Session tracking isolating administrative functions from student actions.
2. **Admin Dashboard (`admin-dashboard.jsp`):**
   - Stat overview: Total Courses & Total Registrations.
   - **Add Course:** Form validating positive credits and checking duplicate course codes.
   - **Edit Course (`admin-edit-course.jsp`):** Form pre-populated from MySQL to modify course name, faculty, or credits.
   - **Delete Course:** Safe deletion with cascade support and confirmation alert.
   - **View Registrations:** Complete audit table displaying student roll numbers, names, course codes, course names, credits, and timestamps via a SQL `JOIN` query.

---

## 3. Technology Stack

This project strictly utilizes standard technologies taught in the Web Technologies syllabus without third-party frameworks:

| Component | Technology | Role |
|---|---|---|
| **Frontend Structure** | HTML5 | Semantic structure for forms, tables, and layouts |
| **Frontend Styling** | Vanilla CSS3 | Custom academic theme (Navy/Slate palette); responsive layout |
| **Client Validation** | JavaScript (ES6) | Non-empty checks, Regex pattern matching, confirmation dialogs |
| **Server-Side Controller**| Java Servlets (Jakarta EE 9/10) | Form processing, parameter retrieval, business logic, redirects |
| **Session Tracking** | `HttpSession` | Secure session tracking across requests |
| **Dynamic Views** | JSP (JavaServer Pages) | Server-side presentation rendering data from MySQL |
| **Database Connectivity** | Java JDBC (`mysql-connector-j`) | Connection management, parameterized queries via `PreparedStatement` |
| **Database** | MySQL Server 8.0 | Relational database (`student_course_portal`) |
| **Runtime Container** | Apache Tomcat 10+ | Servlet and JSP container |

*No external frontend frameworks (React, Angular, Bootstrap, Tailwind) or backend frameworks (Spring Boot, Hibernate) are used.*

---

## 4. Database Schema & Relationships

**Database Name:** `student_course_portal`

### Tables
1. **`students`**
   - `student_id` VARCHAR(20) **[PRIMARY KEY]**
   - `name` VARCHAR(100)
   - `password` VARCHAR(100)
   - `branch` VARCHAR(50)
   - `year` INT
   - `semester` INT
   - `email` VARCHAR(100)

2. **`courses`**
   - `course_code` VARCHAR(10) **[PRIMARY KEY]**
   - `course_name` VARCHAR(100)
   - `faculty` VARCHAR(100)
   - `credits` INT

3. **`registrations`**
   - `registration_id` INT AUTO_INCREMENT **[PRIMARY KEY]**
   - `student_id` VARCHAR(20) **[FOREIGN KEY -> students(student_id)]** `ON DELETE CASCADE`
   - `course_code` VARCHAR(10) **[FOREIGN KEY -> courses(course_code)]** `ON DELETE CASCADE`
   - `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - `UNIQUE KEY unique_student_course (student_id, course_code)`

4. **`admin`**
   - `username` VARCHAR(50) **[PRIMARY KEY]**
   - `password` VARCHAR(50)

### Prescribed Curriculum Courses Seeded
1. `A8506` — Web Technologies | Ms. B. Swapna | 3 Credits
2. `A8519` — Computer Networks | Ms. Sumaiya Shaikh | 1 Credit
3. `A8520` — Software Engineering | Dr. Ravi Kumar | 2 Credits
4. `A8704` — Machine Learning | Dr. B. K. Madhavi | 3 Credits
5. `A8851` — Data Science | Dr. Sreenivasulu Gogula | 3 Credits
*(Total available curriculum credits: 12)*

### Seeded Credentials
- **Student 1:** `24881A1203` / `student123` (Rahul Sharma)
- **Student 2:** `24881A1204` / `student123` (Priya Patel)
- **Administrator:** `admin` / `admin123`

---

## 5. Project Structure

```text
course-end/
├── database/
│   └── student_course_portal.sql              # Database creation & initial seed script
├── pom.xml                                    # Maven POM configured for Java 21 & Tomcat 10+
├── src/
│   ├── main/
│   │   ├── java/com/courseportal/
│   │   │   ├── util/
│   │   │   │   └── DBConnection.java          # JDBC DriverManager connection utility
│   │   │   └── servlet/
│   │   │       ├── LoginServlet.java          # Student login verification
│   │   │       ├── LogoutServlet.java         # Invalidate session & redirect
│   │   │       ├── RegisterCourseServlet.java # Course registration & duplicate check
│   │   │       ├── DropCourseServlet.java     # Dropping student enrolled course
│   │   │       ├── AdminLoginServlet.java     # Admin authentication
│   │   │       ├── AddCourseServlet.java      # Admin course creation
│   │   │       ├── UpdateCourseServlet.java   # Admin course modification
│   │   │       └── DeleteCourseServlet.java   # Admin course deletion
│   │   │
│   │   └── webapp/
│   │       ├── index.html                     # Student login landing page
│   │       ├── admin-login.html               # Administrator login page
│   │       ├── dashboard.jsp                  # Student dashboard with live credit counter
│   │       ├── courses.jsp                    # Course catalog with register buttons
│   │       ├── mycourses.jsp                  # Enrolled courses view with drop buttons
│   │       ├── profile.jsp                    # Read-only student profile card
│   │       ├── admin-dashboard.jsp            # Admin CRUD & registrations audit view
│   │       ├── admin-edit-course.jsp          # Prefilled edit course form
│   │       ├── css/
│   │       │   └── style.css                  # Clean academic styling (Navy/Slate)
│   │       ├── js/
│   │       │   └── validation.js              # Regex roll number validation & alerts
│   │       └── WEB-INF/
│   │           ├── web.xml                    # Servlet mappings and session timeout
│   │           └── lib/
│   │               └── mysql-connector-j-8.0.33.jar # MySQL JDBC Driver
│   └── test/java/com/courseportal/test/
│       ├── LoginFlowTest.java                 # Stage 3/4 verification test
│       ├── DashboardStageTest.java            # Stage 5 dashboard test
│       ├── CourseRegistrationStageTest.java   # Stage 6 registration test
│       ├── DropCourseStageTest.java           # Stage 7 drop course test
│       ├── AdminStageTest.java                # Stage 8 admin CRUD test
│       └── FullSystemFlowTest.java            # Master E2E verification test
└── README.md                                  # Complete project documentation & viva guide
```

---

## 6. How to Configure MySQL Database

1. Open MySQL Command Line Client, MySQL Workbench, or phpMyAdmin.
2. Log in as `root`.
3. Execute the database script:
   ```sql
   SOURCE c:/Users/anike/OneDrive/Desktop/clg html/course end/database/student_course_portal.sql;
   ```
4. Verify the database tables and data:
   ```sql
   USE student_course_portal;
   SHOW TABLES;
   SELECT * FROM courses;
   SELECT * FROM students;
   ```
5. Ensure credentials in `DBConnection.java` match your MySQL installation:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/student_course_portal?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
   private static final String USER = "root";
   private static final String PASSWORD = "root";
   ```

---

## 7. How to Run the Project

### Option A: Using Eclipse IDE for Enterprise Java (Recommended for Lab)
1. Open Eclipse &rarr; **File** &rarr; **Import** &rarr; **Existing Maven Projects** (or Existing Projects into Workspace).
2. Right-click project &rarr; **Properties** &rarr; **Targeted Runtimes** &rarr; Select **Apache Tomcat 10.1**.
3. Right-click project &rarr; **Run As** &rarr; **Run on Server**.
4. Access via browser: `http://localhost:8080/student-course-registration/`

### Option B: Using Standalone Apache Tomcat 10
1. Build WAR file using Maven:
   ```bash
   mvn clean package
   ```
2. Copy `target/student-course-registration.war` to your Tomcat `webapps/` folder.
3. Start Tomcat:
   ```bash
   catalina.bat run
   ```
4. Navigate to: `http://localhost:8080/student-course-registration/`

---

## 8. Application Workflow Diagrams

### Student Flow
```text
index.html (Enter 24881A1203 / student123)
      ↓ (JavaScript Regex Validation: /^24881A\d{4}$/i)
LoginServlet.java (PreparedStatement against 'students' table)
      ↓ (Credentials valid -> Create HttpSession)
dashboard.jsp (Calculates enrolled courses and credits dynamically)
      ├── courses.jsp  ──[Register]──> RegisterCourseServlet.java ──> Insert into registrations
      ├── mycourses.jsp ──[Drop]─────> DropCourseServlet.java     ──> Delete from registrations
      ├── profile.jsp
      └── LogoutServlet.java ───────> session.invalidate() ───────> Redirect to index.html
```

### Admin Flow
```text
admin-login.html (Enter admin / admin123)
      ↓
AdminLoginServlet.java (Query 'admin' table -> Create admin HttpSession)
      ↓
admin-dashboard.jsp
      ├── Add Course Form  ──> AddCourseServlet.java    ──> INSERT INTO courses
      ├── Edit Course Form ──> UpdateCourseServlet.java ──> UPDATE courses
      ├── Delete Course    ──> DeleteCourseServlet.java ──> DELETE FROM courses
      └── View Registrations (SQL JOIN query between students, registrations, and courses)
```

---

## 9. Core Concepts Explained for Viva Voce

### 1. What is JDBC?
**Java Database Connectivity (JDBC)** is a Java API that enables Java programs to interact with relational databases.
- `DriverManager`: Loads database driver classes (`com.mysql.cj.jdbc.Driver`) and creates database connections.
- `Connection`: Represents an active connection session to MySQL.
- `Statement` vs `PreparedStatement`: `PreparedStatement` pre-compiles SQL queries on the database server. It uses placeholders (`?`) for user inputs, which prevents **SQL Injection** attacks and optimizes execution.
- `ResultSet`: A tabular data structure returned by `executeQuery()` representing rows matching the query. We iterate using `rs.next()`.

### 2. What is a Java Servlet?
A **Servlet** is a Java class that runs on a web server (servlet container like Apache Tomcat) and handles client HTTP requests:
- Extends `HttpServlet`.
- `doGet(HttpServletRequest req, HttpServletResponse res)`: Handles HTTP GET requests (reading/displaying data).
- `doPost(HttpServletRequest req, HttpServletResponse res)`: Handles HTTP POST requests (submitting form data securely).
- `request.getParameter("fieldName")`: Reads values submitted from HTML form inputs.
- `response.sendRedirect("url")`: Instructs the client browser to navigate to another page.

### 3. What is JSP (JavaServer Pages)?
**JSP** is a server-side technology used to create dynamic web pages.
- JSP files combine HTML markup with Java code expressions.
- During initial request, the servlet container translates the JSP into a Java Servlet and compiles it.
- In our project, JSP is used exclusively for the presentation layer to render dynamic database query results (`ResultSet`).

### 4. What is `HttpSession` and how does session tracking work?
HTTP is a stateless protocol (each request is independent). `HttpSession` allows the server to maintain state between multiple requests from the same client:
- `request.getSession()` creates or retrieves the active session.
- `session.setAttribute("key", value)` stores session variables (e.g., `studentId`).
- `session.getAttribute("key")` retrieves stored variables across different pages.
- `session.invalidate()` destroys the session during logout.

---

## 10. Frequently Asked Viva Questions & Answers

**Q1: How do you prevent duplicate course registration in your project?**  
*Answer:* We prevent duplicates at two levels:
1. **Application level in `RegisterCourseServlet`:** We run a `SELECT 1 FROM registrations WHERE student_id = ? AND course_code = ?` query. If a match is found, we redirect with `error=already_registered` before attempting any insert.
2. **Database level in MySQL:** The `registrations` table has a `UNIQUE KEY (student_id, course_code)` constraint which prevents duplicate entries even under concurrent requests.

**Q2: Why did you use `PreparedStatement` instead of `Statement`?**  
*Answer:* `PreparedStatement` uses parameter placeholders (`?`) where user inputs are bound safely. This completely avoids SQL injection attacks and improves performance because the database compiles the query structure once.

**Q3: How does the dashboard show the live credit count?**  
*Answer:* It executes a SQL `JOIN` query:
```sql
SELECT c.credits FROM courses c
JOIN registrations r ON c.course_code = r.course_code
WHERE r.student_id = ?;
```
In Java, we loop through the `ResultSet` and sum up `totalCredits += rs.getInt("credits")`.

**Q4: How do you protect pages like `dashboard.jsp` or `admin-dashboard.jsp` from unauthorized access?**  
*Answer:* At the very top of each JSP, we inspect the session:
```java
if (session == null || session.getAttribute("studentId") == null) {
    response.sendRedirect("index.html?error=session_expired");
    return;
}
```
If the session attribute is missing, the request terminates immediately and redirects to the login page.

**Q5: What happens when an admin deletes a course that students have registered for?**  
*Answer:* In our database schema, the foreign key constraint on the `registrations` table specifies `ON DELETE CASCADE`. When a course is removed from `courses`, MySQL automatically removes all corresponding rows in `registrations`, preventing orphan records.

**Q6: What regular expression did you use for student roll number validation?**  
*Answer:* We used `/^24881A\d{4}$/i`. It ensures the roll number begins with `24881A` followed by exactly four digits (e.g., `24881A1203`).
