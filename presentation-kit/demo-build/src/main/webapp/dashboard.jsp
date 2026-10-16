<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.courseportal.util.DBConnection" %>
<%@ page import="com.courseportal.util.RegistrationWindowManager" %>
<%
    // -------------------------------------------------------------------------
    // 1. Session Protection
    // Ensure user is logged in as a student; otherwise redirect to login page
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("studentId") == null) {
        response.sendRedirect("index.html?error=session_expired");
        return;
    }

    // 2. Retrieve student attributes stored in session by LoginServlet
    String studentId = (String) session.getAttribute("studentId");
    String studentName = (String) session.getAttribute("studentName");
    String branch = (String) session.getAttribute("branch");
    Integer year = (Integer) session.getAttribute("year");
    Integer semester = (Integer) session.getAttribute("semester");
    if (year == null) year = 3;
    if (semester == null) semester = 1;

    boolean isRegOpen = RegistrationWindowManager.isWindowOpen(year, semester);

    // 3. Simple Course helper class for holding table data
    class EnrolledCourse {
        String code;
        String name;
        String faculty;
        int credits;

        EnrolledCourse(String c, String n, String f, int cr) {
            this.code = c;
            this.name = n;
            this.faculty = f;
            this.credits = cr;
        }
    }

    List<EnrolledCourse> courseList = new ArrayList<>();
    int totalCredits = 0;
    String dbError = null;

    // 4. JDBC Query to retrieve registered courses and compute total credits
    String query = "SELECT c.course_code, c.course_name, c.faculty, c.credits " +
                   "FROM courses c " +
                   "INNER JOIN registrations r ON c.course_code = r.course_code " +
                   "WHERE r.student_id = ? " +
                   "ORDER BY c.course_code ASC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, studentId);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString("course_code");
                String name = rs.getString("course_name");
                String faculty = rs.getString("faculty");
                int credits = rs.getInt("credits");

                totalCredits += credits;
                courseList.add(new EnrolledCourse(code, name, faculty, credits));
            }
        }
    } catch (SQLException e) {
        dbError = "Error loading registered courses: " + e.getMessage();
        e.printStackTrace();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Dashboard | Course Registration Portal</title>
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
                <a href="dashboard.jsp" class="active">Dashboard</a>
                <a href="courses.jsp">Courses</a>
                <a href="mycourses.jsp">My Courses</a>
                <a href="profile.jsp">Profile</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Dashboard Content -->
    <main class="main-wrapper">

        <!-- Welcome Banner -->
        <section class="welcome-banner">
            <h2>Welcome back, <%= studentName %>!</h2>
            <div class="welcome-meta">
                <span><strong>Roll No:</strong> <%= studentId %></span>
                <span><strong>Branch:</strong> <%= branch %></span>
                <span><strong>Academic Standing:</strong> Year <%= year %>, Semester <%= semester %></span>
                <span><strong>Registration Window:</strong>
                    <% if (isRegOpen) { %>
                        <span class="badge badge-success" style="font-weight: 600;">OPEN</span>
                    <% } else { %>
                        <span class="badge" style="background-color: var(--error-bg); color: var(--error-text); font-weight: 600;">CLOSED</span>
                    <% } %>
                </span>
            </div>
        </section>

        <!-- Database Error Alert (if any) -->
        <% if (dbError != null) { %>
            <div class="alert alert-error"><%= dbError %></div>
        <% } %>

        <!-- Registration Summary Stat Cards -->
        <section class="stat-grid">
            <div class="stat-card">
                <h3>Registered Courses</h3>
                <div class="stat-value"><%= courseList.size() %></div>
                <div class="stat-hint">Active enrollments</div>
            </div>

            <div class="stat-card">
                <h3>Total Credits</h3>
                <div class="stat-value"><%= totalCredits %></div>
                <div class="stat-hint">Total registered course credits</div>
            </div>

            <div class="stat-card">
                <h3>Registration Status</h3>
                <div class="stat-value" style="font-size: 1.4rem; padding-top: 0.35rem; color: <%= isRegOpen ? "var(--success-text)" : "var(--error-text)" %>;">
                    <%= isRegOpen ? "WINDOW OPEN" : "WINDOW CLOSED" %>
                </div>
                <div class="stat-hint">Year <%= year %> — Sem <%= semester %></div>
            </div>
        </section>

        <!-- Current Registered Courses Table -->
        <section class="content-section">
            <div class="section-header">
                <h3>Current Registered Courses</h3>
            </div>

            <div class="table-responsive">
                <% if (courseList.isEmpty()) { %>
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
                                <th style="width: 35%;">Course Name</th>
                                <th style="width: 35%;">Faculty</th>
                                <th style="width: 15%;">Credits</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (EnrolledCourse course : courseList) { %>
                                <tr>
                                    <td><strong><%= course.code %></strong></td>
                                    <td><%= course.name %></td>
                                    <td><%= course.faculty %></td>
                                    <td><span class="badge badge-credits"><%= course.credits %> Credits</span></td>
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
