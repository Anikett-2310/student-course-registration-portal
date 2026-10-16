<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.courseportal.util.DBConnection" %>
<%
    // -------------------------------------------------------------------------
    // 1. Session Protection
    // Redirect unauthenticated requests to index.html
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("studentId") == null) {
        response.sendRedirect("index.html?error=session_expired");
        return;
    }

    String studentId = (String) session.getAttribute("studentId");
    String studentName = (String) session.getAttribute("studentName");
    String branch = (String) session.getAttribute("branch");
    Integer year = (Integer) session.getAttribute("year");
    Integer semester = (Integer) session.getAttribute("semester");
    String email = (String) session.getAttribute("email");

    // Fresh fallback query from database if email attribute was not in session
    if (email == null) {
        String sql = "SELECT email FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    email = rs.getString("email");
                    session.setAttribute("email", email);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Student Profile | Course Registration Portal</title>
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
                <a href="mycourses.jsp">My Courses</a>
                <a href="profile.jsp" class="active">Profile</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-wrapper">
        <div class="profile-card">
            <h2>Student Profile Information</h2>

            <div class="profile-grid">
                <div class="profile-label">Roll Number:</div>
                <div class="profile-value"><strong><%= studentId %></strong></div>

                <div class="profile-label">Full Name:</div>
                <div class="profile-value"><%= studentName %></div>

                <div class="profile-label">Department:</div>
                <div class="profile-value"><%= branch %></div>

                <div class="profile-label">Academic Year:</div>
                <div class="profile-value">Year <%= year %></div>

                <div class="profile-label">Current Semester:</div>
                <div class="profile-value">Semester <%= semester %></div>

                <div class="profile-label">College Email:</div>
                <div class="profile-value"><%= (email != null) ? email : "Not Provided" %></div>
            </div>

            <div class="profile-actions">
                <a href="dashboard.jsp" class="btn-secondary">&larr; Back to Dashboard</a>
            </div>
        </div>
    </main>

    <!-- Footer -->
    <footer class="site-footer">
        <p>&copy; Web Technologies Laboratory Course-End Project — B.Tech IT</p>
    </footer>

</body>
</html>
