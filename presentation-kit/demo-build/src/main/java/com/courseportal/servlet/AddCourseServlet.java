package com.courseportal.servlet;

import com.courseportal.util.CSRFUtil;
import com.courseportal.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AddCourseServlet.java
 * Handles adding new courses by administrators:
 * 1. Verifies admin session.
 * 2. Enforces CSRF token validation.
 * 3. Validates inputs: courseCode, courseName, faculty, credits.
 * 4. Checks for duplicate courseCode in courses table.
 * 5. Inserts new record into courses table using PreparedStatement.
 * 6. Redirects to admin-dashboard.jsp with appropriate status message.
 */
@WebServlet("/AddCourseServlet")
public class AddCourseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Admin session guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            response.sendRedirect("admin-login.html?error=session_expired");
            return;
        }

        // 2. CSRF Guard
        if (!CSRFUtil.isValid(request)) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_csrf#manage-courses");
            return;
        }

        // 2. Retrieve form parameters
        String courseCode = request.getParameter("courseCode");
        String courseName = request.getParameter("courseName");
        String faculty = request.getParameter("faculty");
        String creditsStr = request.getParameter("credits");

        if (courseCode == null || courseName == null || faculty == null || creditsStr == null ||
            courseCode.trim().isEmpty() || courseName.trim().isEmpty() || faculty.trim().isEmpty() || creditsStr.trim().isEmpty()) {
            response.sendRedirect("admin-dashboard.jsp?error=empty_fields");
            return;
        }

        courseCode = courseCode.trim().toUpperCase();
        courseName = courseName.trim();
        faculty = faculty.trim();

        int credits;
        try {
            credits = Integer.parseInt(creditsStr.trim());
            if (credits <= 0 || credits > 10) {
                response.sendRedirect("admin-dashboard.jsp?error=invalid_credits");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_credits");
            return;
        }

        // 3. Database operations
        try (Connection conn = DBConnection.getConnection()) {

            // Step 3a: Duplicate check
            String checkSql = "SELECT 1 FROM courses WHERE course_code = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, courseCode);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("admin-dashboard.jsp?error=duplicate_course_code");
                        return;
                    }
                }
            }

            // Step 3b: Insert course
            String insertSql = "INSERT INTO courses (course_code, course_name, faculty, credits) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, courseCode);
                insertStmt.setString(2, courseName);
                insertStmt.setString(3, faculty);
                insertStmt.setInt(4, credits);
                insertStmt.executeUpdate();
            }

            response.sendRedirect("admin-dashboard.jsp?msg=course_added");

        } catch (SQLException e) {
            System.err.println("Database error adding course: " + e.getMessage());
            e.printStackTrace();
            if (e.getErrorCode() == 1062) {
                response.sendRedirect("admin-dashboard.jsp?error=duplicate_course_code");
            } else {
                response.sendRedirect("admin-dashboard.jsp?error=db_error");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("admin-dashboard.jsp");
    }
}
