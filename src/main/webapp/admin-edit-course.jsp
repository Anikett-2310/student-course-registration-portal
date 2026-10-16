<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="com.courseportal.util.DBConnection" %>
<%@ page import="com.courseportal.util.CSRFUtil" %>
<%
    // -------------------------------------------------------------------------
    // 1. Admin Session Protection
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("adminUser") == null) {
        response.sendRedirect("admin-login.html?error=session_expired");
        return;
    }

    String csrfToken = CSRFUtil.getToken(session);
    String courseCode = request.getParameter("code");
    if (courseCode == null || courseCode.trim().isEmpty()) {
        response.sendRedirect("admin-dashboard.jsp?error=invalid_request");
        return;
    }

    courseCode = courseCode.trim();
    String courseName = "";
    String faculty = "";
    int credits = 0;
    boolean found = false;

    // 2. Fetch existing course details via JDBC
    String sql = "SELECT course_code, course_name, faculty, credits FROM courses WHERE course_code = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, courseCode);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                courseName = rs.getString("course_name");
                faculty = rs.getString("faculty");
                credits = rs.getInt("credits");
                found = true;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    if (!found) {
        response.sendRedirect("admin-dashboard.jsp?error=course_not_found");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Course | Admin Portal</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <!-- Header -->
    <header class="site-header">
        <div class="header-container">
            <div class="portal-title">
                <h1>Student Course Registration Portal</h1>
                <p>Administrative Control Panel</p>
            </div>
            <nav class="nav-links">
                <a href="admin-dashboard.jsp">Admin Dashboard</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-wrapper">
        <div class="auth-container" style="padding-top: 1rem;">
            <div class="auth-card" style="max-width: 520px;">
                <h2>Edit Course Details</h2>
                <p class="subtitle">Modify course details for <strong><%= courseCode %></strong></p>

                <!-- Update Course Form submitting to UpdateCourseServlet -->
                <form action="UpdateCourseServlet" method="POST">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                    <!-- Hidden input to submit the immutable Course Code PK -->
                    <input type="hidden" name="courseCode" value="<%= courseCode %>">

                    <div class="form-group">
                        <label>Course Code (Primary Key)</label>
                        <input type="text" class="form-control" value="<%= courseCode %>" disabled
                               style="background-color: #f1f5f9; cursor: not-allowed;">
                    </div>

                    <div class="form-group">
                        <label for="courseName">Course Name</label>
                        <input type="text" id="courseName" name="courseName" class="form-control"
                               value="<%= courseName %>" required>
                    </div>

                    <div class="form-group">
                        <label for="faculty">Faculty In-Charge</label>
                        <input type="text" id="faculty" name="faculty" class="form-control"
                               value="<%= faculty %>" required>
                    </div>

                    <div class="form-group">
                        <label for="credits">Credits</label>
                        <input type="number" id="credits" name="credits" class="form-control"
                               value="<%= credits %>" min="1" max="10" required>
                    </div>

                    <button type="submit" class="btn btn-primary">Save Changes</button>
                    <a href="admin-dashboard.jsp" class="btn-secondary"
                       style="display: block; text-align: center; margin-top: 0.75rem;">Cancel</a>
                </form>
            </div>
        </div>
    </main>

    <!-- Footer -->
    <footer class="site-footer">
        <p>&copy; Web Technologies Laboratory Course-End Project — B.Tech IT</p>
    </footer>

</body>
</html>
