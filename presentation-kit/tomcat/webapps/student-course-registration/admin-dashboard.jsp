<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.courseportal.util.DBConnection" %>
<%@ page import="com.courseportal.util.CSRFUtil" %>
<%@ page import="com.courseportal.util.RegistrationWindowManager" %>
<%
    // -------------------------------------------------------------------------
    // 1. Admin Session Protection
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("adminUser") == null) {
        response.sendRedirect("admin-login.html?error=session_expired");
        return;
    }

    String adminUser = (String) session.getAttribute("adminUser");
    String csrfToken = CSRFUtil.getToken(session);

    // Course data structure
    class CourseRow {
        String code;
        String name;
        String faculty;
        int credits;

        CourseRow(String c, String n, String f, int cr) {
            this.code = c;
            this.name = n;
            this.faculty = f;
            this.credits = cr;
        }
    }

    // Student data structure
    class StudentRow {
        String studentId;
        String name;
        String branch;
        int year;
        int semester;
        int globalSem;
        String email;

        StudentRow(String sid, String n, String b, int y, int s, String e) {
            this.studentId = sid;
            this.name = n;
            this.branch = b;
            this.year = y;
            this.semester = s;
            this.globalSem = (y - 1) * 2 + s;
            this.email = e;
        }
    }

    // Registration JOIN data structure
    class RegistrationRow {
        int id;
        String studentId;
        String studentName;
        String courseCode;
        String courseName;
        int credits;
        String registeredAt;

        RegistrationRow(int id, String sid, String sname, String ccode, String cname, int cr, String date) {
            this.id = id;
            this.studentId = sid;
            this.studentName = sname;
            this.courseCode = ccode;
            this.courseName = cname;
            this.credits = cr;
            this.registeredAt = date;
        }
    }

    List<CourseRow> courseList = new ArrayList<>();
    List<StudentRow> studentList = new ArrayList<>();
    List<RegistrationRow> registrationList = new ArrayList<>();
    String dbError = null;

    // 2. Database operations via JDBC
    try (Connection conn = DBConnection.getConnection()) {

        // Query 1: Fetch all courses
        String coursesSql = "SELECT course_code, course_name, faculty, credits FROM courses ORDER BY course_code ASC";
        try (PreparedStatement cStmt = conn.prepareStatement(coursesSql);
             ResultSet rs = cStmt.executeQuery()) {
            while (rs.next()) {
                courseList.add(new CourseRow(
                    rs.getString("course_code"),
                    rs.getString("course_name"),
                    rs.getString("faculty"),
                    rs.getInt("credits")
                ));
            }
        }

        // Query 2: Fetch all students
        String studentsSql = "SELECT student_id, name, branch, year, semester, email FROM students ORDER BY student_id ASC";
        try (PreparedStatement sStmt = conn.prepareStatement(studentsSql);
             ResultSet sRs = sStmt.executeQuery()) {
            while (sRs.next()) {
                studentList.add(new StudentRow(
                    sRs.getString("student_id"),
                    sRs.getString("name"),
                    sRs.getString("branch"),
                    sRs.getInt("year"),
                    sRs.getInt("semester"),
                    sRs.getString("email")
                ));
            }
        }

        // Query 3: Fetch student registrations via JOIN
        String regSql = "SELECT r.registration_id, r.student_id, s.name AS student_name, " +
                        "r.course_code, c.course_name, c.credits, r.registered_at " +
                        "FROM registrations r " +
                        "JOIN students s ON r.student_id = s.student_id " +
                        "JOIN courses c ON r.course_code = c.course_code " +
                        "ORDER BY r.registered_at DESC";
        try (PreparedStatement rStmt = conn.prepareStatement(regSql);
             ResultSet regRs = rStmt.executeQuery()) {
            while (regRs.next()) {
                registrationList.add(new RegistrationRow(
                    regRs.getInt("registration_id"),
                    regRs.getString("student_id"),
                    regRs.getString("student_name"),
                    regRs.getString("course_code"),
                    regRs.getString("course_name"),
                    regRs.getInt("credits"),
                    regRs.getString("registered_at")
                ));
            }
        }

    } catch (SQLException e) {
        dbError = "Database error: " + e.getMessage();
        e.printStackTrace();
    }

    // 3. Status messages from URL parameters
    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administrator Dashboard | Course Registration Portal</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <!-- Header & Navigation Bar -->
    <header class="site-header">
        <div class="header-container">
            <div class="portal-title">
                <h1>Student Course Registration Portal</h1>
                <p>Administrative Control Panel — Welcome, <%= adminUser %></p>
            </div>
            <nav class="nav-links">
                <a href="admin-dashboard.jsp" class="active">Admin Dashboard</a>
                <a href="#registration-control">Reg Windows</a>
                <a href="#manage-students">Students</a>
                <a href="#manage-courses">Courses</a>
                <a href="#student-registrations">Enrollments</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-wrapper">

        <!-- Welcome Banner -->
        <div class="welcome-banner">
            <h2>Course Management & Multi-Year Administration</h2>
            <div class="welcome-meta">
                <span>Manage 4-year curriculum, open/close semester registration windows, and administer student accounts.</span>
            </div>
        </div>

        <!-- Feedback Alert Messages -->
        <% if ("course_added".equals(msg)) { %>
            <div class="alert alert-success">Course was added successfully.</div>
        <% } else if ("course_updated".equals(msg)) { %>
            <div class="alert alert-success">Course was updated successfully.</div>
        <% } else if ("course_deleted".equals(msg)) { %>
            <div class="alert alert-success">Course was deleted successfully.</div>
        <% } else if ("window_updated".equals(msg)) { %>
            <div class="alert alert-success">Registration window status updated successfully.</div>
        <% } else if ("student_created".equals(msg)) { %>
            <div class="alert alert-success">New student account created successfully.</div>
        <% } else if ("student_updated".equals(msg)) { %>
            <div class="alert alert-success">Student profile updated successfully.</div>
        <% } else if ("password_reset".equals(msg)) { %>
            <div class="alert alert-success">Student password reset successfully.</div>
        <% } %>

        <% if ("duplicate_course_code".equals(error)) { %>
            <div class="alert alert-error">Error: A course with this Course Code already exists.</div>
        <% } else if ("duplicate_student_id".equals(error)) { %>
            <div class="alert alert-error">Error: A student account with this Roll Number already exists.</div>
        <% } else if ("course_not_found".equals(error)) { %>
            <div class="alert alert-error">Error: The specified course was not found.</div>
        <% } else if ("student_not_found".equals(error)) { %>
            <div class="alert alert-error">Error: The specified student was not found.</div>
        <% } else if ("empty_fields".equals(error)) { %>
            <div class="alert alert-error">Error: Please fill in all required fields.</div>
        <% } else if ("invalid_academic_standing".equals(error)) { %>
            <div class="alert alert-error">Error: Year must be between 1 and 4, and Semester between 1 and 2.</div>
        <% } else if ("invalid_credits".equals(error)) { %>
            <div class="alert alert-error">Error: Credits must be a positive number between 1 and 10.</div>
        <% } else if ("invalid_csrf".equals(error)) { %>
            <div class="alert alert-error">Security Error: Invalid or missing CSRF token. Please retry.</div>
        <% } else if ("db_error".equals(error) || dbError != null) { %>
            <div class="alert alert-error"><%= (dbError != null) ? dbError : "A database error occurred. Please try again." %></div>
        <% } %>

        <!-- Admin Overview Stat Cards -->
        <section class="stat-grid">
            <div class="stat-card">
                <h3>Total Students</h3>
                <div class="stat-value"><%= studentList.size() %></div>
                <div class="stat-hint">Active student accounts</div>
            </div>

            <div class="stat-card">
                <h3>Total Courses</h3>
                <div class="stat-value"><%= courseList.size() %></div>
                <div class="stat-hint">Active curriculum subjects</div>
            </div>

            <div class="stat-card">
                <h3>Total Registrations</h3>
                <div class="stat-value"><%= registrationList.size() %></div>
                <div class="stat-hint">Total student enrollments</div>
            </div>

            <div class="stat-card">
                <h3>Curriculum</h3>
                <div class="stat-value" style="font-size: 1.4rem; padding-top: 0.35rem;">VCE R22</div>
                <div class="stat-hint">8 Semesters / 160 Credits</div>
            </div>
        </section>

        <!-- SECTION 1: Registration Window Control (Admin Requirement 3) -->
        <section class="content-section" id="registration-control">
            <div class="section-header">
                <div>
                    <h3>Registration Window Control (All 8 Semesters)</h3>
                    <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.2rem;">
                        Control student registration eligibility. When CLOSED, students in that semester cannot register or drop.
                    </p>
                </div>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width: 15%;">Academic Year</th>
                            <th style="width: 15%;">Semester</th>
                            <th style="width: 20%;">Global Semester</th>
                            <th style="width: 25%;">Current Status</th>
                            <th style="width: 25%;">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (int y = 1; y <= 4; y++) {
                            for (int s = 1; s <= 2; s++) {
                                int gSem = (y - 1) * 2 + s;
                                boolean isOpen = RegistrationWindowManager.isWindowOpen(y, s);
                        %>
                            <tr>
                                <td><strong>Year <%= y %></strong></td>
                                <td>Semester <%= s %></td>
                                <td><span class="badge" style="background-color: #f1f5f9;">Semester <%= gSem %></span></td>
                                <td>
                                    <% if (isOpen) { %>
                                        <span class="badge badge-success" style="font-weight: 600;">OPEN</span>
                                    <% } else { %>
                                        <span class="badge" style="background-color: var(--error-bg); color: var(--error-text); font-weight: 600;">CLOSED</span>
                                    <% } %>
                                </td>
                                <td>
                                    <form action="RegistrationControlServlet" method="POST" style="margin: 0; display: inline;">
                                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                        <input type="hidden" name="year" value="<%= y %>">
                                        <input type="hidden" name="semester" value="<%= s %>">
                                        <% if (isOpen) { %>
                                            <input type="hidden" name="status" value="CLOSED">
                                            <button type="submit" class="btn-action btn-drop btn-sm">Close Window</button>
                                        <% } else { %>
                                            <input type="hidden" name="status" value="OPEN">
                                            <button type="submit" class="btn-action btn-register btn-sm">Open Window</button>
                                        <% } %>
                                    </form>
                                </td>
                            </tr>
                        <%  }
                        } %>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- SECTION 2: Student Account Management (Admin Requirement 2) -->
        <section class="content-section" id="manage-students">
            <div class="section-header">
                <div>
                    <h3>Create Student Account</h3>
                    <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.2rem;">
                        Register new student accounts and assign their academic year (1-4) and semester (1-2).
                    </p>
                </div>
            </div>

            <!-- Create Student Form -->
            <form action="StudentManagementServlet" method="POST"
                  style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; align-items: end;">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="create">

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newStudentId">Roll Number</label>
                    <input type="text" id="newStudentId" name="studentId" class="form-control" placeholder="e.g., 26881A1201" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newStudentName">Full Name</label>
                    <input type="text" id="newStudentName" name="name" class="form-control" placeholder="Student Name" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newPassword">Password</label>
                    <input type="password" id="newPassword" name="password" class="form-control" placeholder="Initial Password" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newBranch">Branch</label>
                    <input type="text" id="newBranch" name="branch" class="form-control" value="IT" required>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newYear">Academic Year</label>
                    <select id="newYear" name="year" class="form-control" required>
                        <option value="1">1st Year</option>
                        <option value="2">2nd Year</option>
                        <option value="3" selected>3rd Year</option>
                        <option value="4">4th Year</option>
                    </select>
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newSem">Semester</label>
                    <select id="newSem" name="semester" class="form-control" required>
                        <option value="1" selected>Sem 1 (Odd)</option>
                        <option value="2">Sem 2 (Even)</option>
                    </select>
                </div>

                <div>
                    <button type="submit" class="btn btn-primary" style="height: 42px; width: 100%;">Create Student</button>
                </div>
            </form>

            <!-- Students List Table -->
            <div style="margin-top: 1.5rem;">
                <h4 style="font-size: 1rem; margin-bottom: 0.75rem; color: var(--text-main);">
                    Existing Student Accounts (<%= studentList.size() %> Total)
                </h4>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 15%;">Roll Number</th>
                                <th style="width: 22%;">Student Name</th>
                                <th style="width: 15%;">Branch</th>
                                <th style="width: 16%;">Academic Standing</th>
                                <th style="width: 22%;">Email</th>
                                <th style="width: 10%;">Global Sem</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (StudentRow s : studentList) { %>
                                <tr>
                                    <td><strong><%= s.studentId %></strong></td>
                                    <td><%= s.name %></td>
                                    <td><%= s.branch %></td>
                                    <td>Year <%= s.year %>, Sem <%= s.semester %></td>
                                    <td style="font-size: 0.85rem; color: var(--text-muted);"><%= s.email %></td>
                                    <td><span class="badge" style="background-color: #f1f5f9;">Sem <%= s.globalSem %></span></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- SECTION 3: Course Management (Preserved Baseline) -->
        <section class="content-section" id="manage-courses">
            <div class="section-header">
                <h3>Add New Course</h3>
            </div>
            <form action="AddCourseServlet" method="POST" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; align-items: end;">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="courseCode">Course Code</label>
                    <input type="text" id="courseCode" name="courseCode" class="form-control" placeholder="e.g., A8999" required>
                </div>
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="courseName">Course Name</label>
                    <input type="text" id="courseName" name="courseName" class="form-control" placeholder="e.g., Cloud Computing" required>
                </div>
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="faculty">Faculty In-Charge</label>
                    <input type="text" id="faculty" name="faculty" class="form-control" placeholder="e.g., Dr. A. Sharma" required>
                </div>
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="credits">Credits (1-10)</label>
                    <input type="number" id="credits" name="credits" class="form-control" min="1" max="10" placeholder="3" required>
                </div>
                <div>
                    <button type="submit" class="btn btn-primary" style="height: 42px;">Add Course</button>
                </div>
            </form>

            <div style="margin-top: 2rem;">
                <h4 style="font-size: 1rem; margin-bottom: 0.75rem; color: var(--text-main);">
                    Curriculum Courses List (<%= courseList.size() %> Total)
                </h4>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 15%;">Course Code</th>
                                <th style="width: 32%;">Course Name</th>
                                <th style="width: 25%;">Faculty</th>
                                <th style="width: 12%;">Credits</th>
                                <th style="width: 16%;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (CourseRow course : courseList) { %>
                                <tr>
                                    <td><strong><%= course.code %></strong></td>
                                    <td><%= course.name %></td>
                                    <td><%= course.faculty %></td>
                                    <td><span class="badge badge-credits"><%= course.credits %> Credits</span></td>
                                    <td>
                                        <div style="display: flex; gap: 0.4rem;">
                                            <a href="admin-edit-course.jsp?code=<%= course.code %>" class="btn-action btn-edit btn-sm">Edit</a>
                                            <form action="DeleteCourseServlet" method="POST"
                                                  onsubmit="return confirm('Are you sure you want to delete course <%= course.name %> (<%= course.code %>)?');"
                                                  style="margin: 0; display: inline;">
                                                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                <input type="hidden" name="courseCode" value="<%= course.code %>">
                                                <button type="submit" class="btn-action btn-drop btn-sm">Delete</button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- SECTION 4: Student Registrations Table (SQL JOIN) -->
        <section class="content-section" id="student-registrations">
            <div class="section-header">
                <h3>Student Course Registrations (<%= registrationList.size() %> Total)</h3>
            </div>
            <div class="table-responsive">
                <% if (registrationList.isEmpty()) { %>
                    <div class="empty-message">No student registrations recorded yet.</div>
                <% } else { %>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 15%;">Roll Number</th>
                                <th style="width: 22%;">Student Name</th>
                                <th style="width: 13%;">Course Code</th>
                                <th style="width: 25%;">Course Name</th>
                                <th style="width: 10%;">Credits</th>
                                <th style="width: 15%;">Registered On</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (RegistrationRow reg : registrationList) { %>
                                <tr>
                                    <td><strong><%= reg.studentId %></strong></td>
                                    <td><%= reg.studentName %></td>
                                    <td><span class="badge"><%= reg.courseCode %></span></td>
                                    <td><%= reg.courseName %></td>
                                    <td><span class="badge badge-credits"><%= reg.credits %> Cr</span></td>
                                    <td style="font-size: 0.82rem; color: var(--text-muted);"><%= reg.registeredAt %></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>
        </section>

    </main>

    <!-- Footer -->
    <footer class="site-footer">
        <p>&copy; Web Technologies Laboratory Course-End Project — B.Tech IT (R22 Regulations)</p>
    </footer>

</body>
</html>
