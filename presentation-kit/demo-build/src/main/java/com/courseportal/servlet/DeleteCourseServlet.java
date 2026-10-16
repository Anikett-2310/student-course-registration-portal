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
 * DeleteCourseServlet.java
 * Handles deleting course records by administrators:
 * 1. Verifies admin session.
 * 2. Enforces CSRF token validation.
 * 3. Enforces HTTP POST only (rejects GET mutations).
 * 4. Reads courseCode parameter and verifies course exists.
 * 5. Deletes from courses table.
 * 6. Redirects to admin-dashboard.jsp with feedback message.
 */
@WebServlet("/DeleteCourseServlet")
public class DeleteCourseServlet extends HttpServlet {

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

        // 3. Read courseCode
        String courseCode = request.getParameter("courseCode");
        if (courseCode == null || courseCode.trim().isEmpty()) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_request#manage-courses");
            return;
        }

        courseCode = courseCode.trim();

        // 4. Database operations
        try (Connection conn = DBConnection.getConnection()) {

            // Step 4a: Check existence
            String checkSql = "SELECT 1 FROM courses WHERE course_code = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, courseCode);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        response.sendRedirect("admin-dashboard.jsp?error=course_not_found#manage-courses");
                        return;
                    }
                }
            }

            // Step 4b: Delete course
            String deleteSql = "DELETE FROM courses WHERE course_code = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, courseCode);
                int rows = deleteStmt.executeUpdate();

                if (rows > 0) {
                    response.sendRedirect("admin-dashboard.jsp?msg=course_deleted#manage-courses");
                } else {
                    response.sendRedirect("admin-dashboard.jsp?error=course_not_found#manage-courses");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error deleting course: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("admin-dashboard.jsp?error=db_error#manage-courses");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Direct GET requests are rejected for state-changing deletion
        response.sendRedirect("admin-dashboard.jsp#manage-courses");
    }
}
