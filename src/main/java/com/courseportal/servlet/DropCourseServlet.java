package com.courseportal.servlet;

import com.courseportal.util.CSRFUtil;
import com.courseportal.util.DBConnection;
import com.courseportal.util.RegistrationWindowManager;
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
 * DropCourseServlet.java
 * Handles dropping an enrolled course for the logged-in student:
 * 1. Verifies student session.
 * 2. Enforces CSRF token validation.
 * 3. Enforces registration window state (cannot drop courses when window is closed).
 * 4. Checks that the registration actually belongs to this student.
 * 5. Deletes the record from registrations table using PreparedStatement.
 * 6. Redirects back to mycourses.jsp with appropriate feedback message.
 */
@WebServlet("/DropCourseServlet")
public class DropCourseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Session verification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html?error=session_expired");
            return;
        }

        // 2. CSRF Guard
        if (!CSRFUtil.isValid(request)) {
            response.sendRedirect("mycourses.jsp?error=invalid_csrf");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");
        Integer studentYear = (Integer) session.getAttribute("year");
        Integer studentSem = (Integer) session.getAttribute("semester");

        // 3. Registration Window Guard
        if (studentYear != null && studentSem != null && !RegistrationWindowManager.isWindowOpen(studentYear, studentSem)) {
            response.sendRedirect("mycourses.jsp?error=window_closed");
            return;
        }

        // 4. Read courseCode parameter
        String courseCode = request.getParameter("courseCode");
        if (courseCode == null || courseCode.trim().isEmpty()) {
            response.sendRedirect("mycourses.jsp?error=invalid_request");
            return;
        }

        courseCode = courseCode.trim();

        // 5. Database operations
        try (Connection conn = DBConnection.getConnection()) {

            boolean useCurriculumReg = RegisterCourseServlet.hasCurriculumRegistrationsTable(conn);

            if (useCurriculumReg) {
                String delCurSql = "DELETE FROM curriculum_registrations WHERE student_id = ? AND course_code = ?";
                try (PreparedStatement delPs = conn.prepareStatement(delCurSql)) {
                    delPs.setString(1, studentId);
                    delPs.setString(2, courseCode);
                    int curRows = delPs.executeUpdate();
                    if (curRows > 0) {
                        response.sendRedirect("mycourses.jsp?msg=dropped_successfully");
                        return;
                    }
                }
            }

            // Fallback: Check and delete from legacy registrations table
            String verifyRegSql = "SELECT 1 FROM registrations WHERE student_id = ? AND course_code = ?";
            try (PreparedStatement verifyStmt = conn.prepareStatement(verifyRegSql)) {
                verifyStmt.setString(1, studentId);
                verifyStmt.setString(2, courseCode);

                try (ResultSet rs = verifyStmt.executeQuery()) {
                    if (!rs.next()) {
                        response.sendRedirect("mycourses.jsp?error=not_registered");
                        return;
                    }
                }
            }

            String deleteSql = "DELETE FROM registrations WHERE student_id = ? AND course_code = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, studentId);
                deleteStmt.setString(2, courseCode);

                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    response.sendRedirect("mycourses.jsp?msg=dropped_successfully");
                } else {
                    response.sendRedirect("mycourses.jsp?error=not_registered");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error during drop course: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("mycourses.jsp?error=db_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("mycourses.jsp");
    }
}
