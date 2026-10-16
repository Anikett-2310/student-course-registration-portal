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
    // 1. Session Protection
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("studentId") == null) {
        response.sendRedirect("index.html?error=session_expired");
        return;
    }

    String studentId = (String) session.getAttribute("studentId");
    String studentName = (String) session.getAttribute("studentName");
    Integer year = (Integer) session.getAttribute("year");
    Integer semester = (Integer) session.getAttribute("semester");
    if (year == null) year = 3;
    if (semester == null) semester = 1;

    boolean isRegOpen = RegistrationWindowManager.isWindowOpen(year, semester);
    String csrfToken = CSRFUtil.getToken(session);

    // Course holder class
    class RegisteredCourse {
        String code;
        String name;
        String faculty;
        int credits;

        RegisteredCourse(String c, String n, String f, int cr) {
            this.code = c;
            this.name = n;
            this.faculty = f;
            this.credits = cr;
        }
    }

    List<RegisteredCourse> registeredList = new ArrayList<>();
    int totalCredits = 0;
    String dbError = null;

    // 2. Fetch student's registered courses using SQL JOIN
    String sql = "SELECT c.course_code, c.course_name, c.faculty, c.credits " +
                 "FROM courses c " +
                 "INNER JOIN registrations r ON c.course_code = r.course_code " +
                 "WHERE r.student_id = ? " +
                 "ORDER BY c.course_code ASC";

    try (Connection conn = DBConnection.getConnection()) {

        // Query 1: Fetch from curriculum_registrations if table exists
        boolean useCurriculumReg = false;
        try {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "curriculum_registrations", new String[]{"TABLE"})) {
                useCurriculumReg = rs.next();
            }
        } catch (Exception ignored) {}

        if (useCurriculumReg) {
            String curSql = "SELECT course_code, course_name, credits, elective_slot FROM curriculum_registrations WHERE student_id = ? ORDER BY registered_at ASC";
            try (PreparedStatement curStmt = conn.prepareStatement(curSql)) {
                curStmt.setString(1, studentId);
                try (ResultSet curRs = curStmt.executeQuery()) {
                    while (curRs.next()) {
                        String code = curRs.getString("course_code");
                        String name = curRs.getString("course_name");
                        String slot = curRs.getString("elective_slot");
                        String facultyDisplay = (slot != null) ? "[" + slot + "] Assigned Faculty" : "Department Faculty";
                        int credits = curRs.getInt("credits");
                        totalCredits += credits;
                        registeredList.add(new RegisteredCourse(code, name, facultyDisplay, credits));
                    }
                }
            }
        }

        // Query 2: Fetch legacy registrations
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("course_code");
                    String name = rs.getString("course_name");
                    String faculty = rs.getString("faculty");
                    int credits = rs.getInt("credits");

                    totalCredits += credits;
                    registeredList.add(new RegisteredCourse(code, name, faculty, credits));
                }
            }
        }
    } catch (SQLException e) {
        dbError = "Database error while fetching registered courses: " + e.getMessage();
        e.printStackTrace();
    }

    // 3. Status message handling from URL query parameters
    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Courses | Course Registration Portal</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <!-- Header & Student Navigation Bar -->
    <header class="site-header">
        <div class="header-container">
            <div class="portal-title">
                <h1>Student Course Registration Portal</h1>
                <p>Web Technologies Laboratory — Department of Information Technology</p>
            </div>
            <nav class="nav-links">
                <a href="dashboard.jsp">Dashboard</a>
                <a href="courses.jsp">Courses</a>
                <a href="mycourses.jsp" class="active">My Courses</a>
                <a href="profile.jsp">Profile</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-wrapper">

        <div class="welcome-banner">
            <h2>My Enrolled Courses</h2>
            <div class="welcome-meta">
                <span>View and manage your registered courses for Year <%= year %>, Semester <%= semester %>.</span>
                <span><strong>Window:</strong>
                    <% if (isRegOpen) { %>
                        <span class="badge badge-success">OPEN</span>
                    <% } else { %>
                        <span class="badge" style="background-color: var(--error-bg); color: var(--error-text);">CLOSED</span>
                    <% } %>
                </span>
            </div>
        </div>

        <!-- Feedback Alert Messages -->
        <% if ("dropped_successfully".equals(msg)) { %>
            <div class="alert alert-success">Course was dropped successfully.</div>
        <% } %>

        <% if ("window_closed".equals(error)) { %>
            <div class="alert alert-error">Registration window is CLOSED for your semester. Dropping courses is currently disabled.</div>
        <% } else if ("invalid_csrf".equals(error)) { %>
            <div class="alert alert-error">Security Error: Invalid or expired CSRF token. Please retry.</div>
        <% } else if ("not_registered".equals(error)) { %>
            <div class="alert alert-error">You are not registered for that course.</div>
        <% } else if ("invalid_request".equals(error)) { %>
            <div class="alert alert-error">Invalid course drop request.</div>
        <% } else if ("db_error".equals(error) || dbError != null) { %>
            <div class="alert alert-error"><%= (dbError != null) ? dbError : "A database error occurred. Please try again." %></div>
        <% } %>

        <!-- Summary Stat Cards -->
        <section class="stat-grid">
            <div class="stat-card">
                <h3>Total Registered Courses</h3>
                <div class="stat-value"><%= registeredList.size() %></div>
                <div class="stat-hint">Active subjects</div>
            </div>

            <div class="stat-card">
                <h3>Total Credits</h3>
                <div class="stat-value"><%= totalCredits %></div>
                <div class="stat-hint">Earned / In-progress credits</div>
            </div>
        </section>

        <!-- Registered Courses Table -->
        <section class="content-section">
            <div class="section-header">
                <h3>Registered Courses List</h3>
            </div>

            <div class="table-responsive">
                <% if (registeredList.isEmpty()) { %>
                    <div class="empty-message">
                        You have not registered for any courses yet.
                        <br>
                        <a href="courses.jsp" style="color: var(--accent-blue); text-decoration: underline; margin-top: 0.5rem; display: inline-block;">Browse Available Courses &rarr;</a>
                    </div>
                <% } else { %>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 15%;">Course Code</th>
                                <th style="width: 32%;">Course Name</th>
                                <th style="width: 25%;">Faculty</th>
                                <th style="width: 13%;">Credits</th>
                                <th style="width: 15%;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (RegisteredCourse course : registeredList) { %>
                                <tr>
                                    <td><strong><%= course.code %></strong></td>
                                    <td><%= course.name %></td>
                                    <td><%= course.faculty %></td>
                                    <td><span class="badge badge-credits"><%= course.credits %> Credits</span></td>
                                    <td>
                                        <% if (!isRegOpen) { %>
                                            <button type="button" class="btn-action" disabled
                                                    style="background-color: #cbd5e1; color: #64748b; cursor: not-allowed;"
                                                    title="Registration window is closed">Window Closed</button>
                                        <% } else { %>
                                            <!-- Drop Course Form with JavaScript Confirmation Dialog and CSRF Token -->
                                            <form action="DropCourseServlet" method="POST"
                                                  onsubmit="return confirm('Are you sure you want to drop <%= course.name %> (<%= course.code %>)?');"
                                                  style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                <input type="hidden" name="courseCode" value="<%= course.code %>">
                                                <button type="submit" class="btn-action btn-drop">Drop Course</button>
                                            </form>
                                        <% } %>
                                    </td>
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
        <p>&copy; Web Technologies Laboratory Course-End Project — B.Tech IT</p>
    </footer>

</body>
</html>
