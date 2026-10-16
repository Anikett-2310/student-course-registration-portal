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
 * UpdateCourseServlet.java
 * Handles updating course details by administrators:
 * 1. Verifies admin session.
 * 2. Enforces CSRF token validation.
 * 3. Validates inputs.
 * 4. Checks that the target course exists.
 * 5. Executes UPDATE on courses table using PreparedStatement.
 * 6. Redirects to admin-dashboard.jsp with feedback status.
 */
@WebServlet("/UpdateCourseServlet")
public class UpdateCourseServlet extends HttpServlet {

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
            response.sendRedirect("admin-dashboard.jsp?error=invalid_csrf");
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

        courseCode = courseCode.trim();
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

            // Step 3a: Check course exists
            String checkSql = "SELECT 1 FROM courses WHERE course_code = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, courseCode);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        response.sendRedirect("admin-dashboard.jsp?error=course_not_found");
                        return;
                    }
                }
            }

            // Step 3b: Execute UPDATE query
            String updateSql = "UPDATE courses SET course_name = ?, faculty = ?, credits = ? WHERE course_code = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, courseName);
                updateStmt.setString(2, faculty);
                updateStmt.setInt(3, credits);
                updateStmt.setString(4, courseCode);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    response.sendRedirect("admin-dashboard.jsp?msg=course_updated");
                } else {
                    response.sendRedirect("admin-dashboard.jsp?error=course_not_found");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error updating course: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("admin-dashboard.jsp?error=db_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("admin-dashboard.jsp");
    }
}
