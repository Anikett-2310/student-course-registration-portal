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
 * StudentManagementServlet.java
 * Enables Administrator to manage multi-year student accounts:
 * - Create new student accounts with academic year (1-4) and semester (1-2) assignment.
 * - Update existing student profiles and academic standing.
 * - Reset credentials securely.
 */
@WebServlet("/StudentManagementServlet")
public class StudentManagementServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Admin Session Guard
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            response.sendRedirect("admin-login.html?error=session_expired");
            return;
        }

        // 2. CSRF Guard
        if (!CSRFUtil.isValid(request)) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_csrf#manage-students");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "create";

        switch (action) {
            case "create":
                handleCreateStudent(request, response);
                break;
            case "update":
                handleUpdateStudent(request, response);
                break;
            case "reset_password":
                handleResetPassword(request, response);
                break;
            default:
                response.sendRedirect("admin-dashboard.jsp?error=invalid_request#manage-students");
        }
    }

    private void handleCreateStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = request.getParameter("studentId");
        String name = request.getParameter("name");
        String password = request.getParameter("password");
        String branch = request.getParameter("branch");
        String email = request.getParameter("email");
        String yearStr = request.getParameter("year");
        String semStr = request.getParameter("semester");

        if (studentId == null || name == null || password == null || branch == null ||
            yearStr == null || semStr == null || studentId.trim().isEmpty() || name.trim().isEmpty()) {
            response.sendRedirect("admin-dashboard.jsp?error=empty_fields#manage-students");
            return;
        }

        studentId = studentId.trim().toUpperCase();
        name = name.trim();
        password = password.trim();
        branch = branch.trim();
        email = (email != null && !email.trim().isEmpty()) ? email.trim() : (studentId.toLowerCase() + "@student.vardhaman.org");

        // Format validation
        java.util.regex.Pattern rollPattern = java.util.regex.Pattern.compile("^(\\d{2}881A[0-9A-Za-z]{4}|[0-9]{2}[0-9A-Za-z]{8})$", java.util.regex.Pattern.CASE_INSENSITIVE);
        if (!rollPattern.matcher(studentId).matches()) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_roll_format#manage-students");
            return;
        }

        if (password.length() < 6 || password.length() > 100) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_password_length#manage-students");
            return;
        }

        int year;
        int semester;
        try {
            year = Integer.parseInt(yearStr.trim());
            semester = Integer.parseInt(semStr.trim());
            if (year < 1 || year > 4 || semester < 1 || semester > 2) {
                response.sendRedirect("admin-dashboard.jsp?error=invalid_academic_standing#manage-students");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_academic_standing#manage-students");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check duplicate student_id
            String checkSql = "SELECT 1 FROM students WHERE student_id = ?";
            try (PreparedStatement chk = conn.prepareStatement(checkSql)) {
                chk.setString(1, studentId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("admin-dashboard.jsp?error=duplicate_student_id#manage-students");
                        return;
                    }
                }
            }

            // Insert student
            String insertSql = "INSERT INTO students (student_id, name, password, branch, year, semester, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setString(1, studentId);
                ins.setString(2, name);
                ins.setString(3, password);
                ins.setString(4, branch);
                ins.setInt(5, year);
                ins.setInt(6, semester);
                ins.setString(7, email);
                ins.executeUpdate();
            }

            response.sendRedirect("admin-dashboard.jsp?msg=student_created#manage-students");

        } catch (SQLException e) {
            System.err.println("Error creating student: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("admin-dashboard.jsp?error=db_error#manage-students");
        }
    }

    private void handleUpdateStudent(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = request.getParameter("studentId");
        String name = request.getParameter("name");
        String branch = request.getParameter("branch");
        String email = request.getParameter("email");
        String yearStr = request.getParameter("year");
        String semStr = request.getParameter("semester");

        if (studentId == null || name == null || branch == null || yearStr == null || semStr == null) {
            response.sendRedirect("admin-dashboard.jsp?error=empty_fields#manage-students");
            return;
        }

        studentId = studentId.trim();
        name = name.trim();
        branch = branch.trim();
        email = (email != null) ? email.trim() : "";

        int year;
        int semester;
        try {
            year = Integer.parseInt(yearStr.trim());
            semester = Integer.parseInt(semStr.trim());
            if (year < 1 || year > 4 || semester < 1 || semester > 2) {
                response.sendRedirect("admin-dashboard.jsp?error=invalid_academic_standing#manage-students");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_academic_standing#manage-students");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE students SET name = ?, branch = ?, year = ?, semester = ?, email = ? WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, name);
                ps.setString(2, branch);
                ps.setInt(3, year);
                ps.setInt(4, semester);
                ps.setString(5, email);
                ps.setString(6, studentId);
                int rows = ps.executeUpdate();

                if (rows > 0) {
                    response.sendRedirect("admin-dashboard.jsp?msg=student_updated#manage-students");
                } else {
                    response.sendRedirect("admin-dashboard.jsp?error=student_not_found#manage-students");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("admin-dashboard.jsp?error=db_error#manage-students");
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String studentId = request.getParameter("studentId");
        String newPassword = request.getParameter("newPassword");

        if (studentId == null || newPassword == null || newPassword.trim().isEmpty()) {
            response.sendRedirect("admin-dashboard.jsp?error=empty_fields#manage-students");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE students SET password = ? WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newPassword.trim());
                ps.setString(2, studentId.trim());
                ps.executeUpdate();
            }
            response.sendRedirect("admin-dashboard.jsp?msg=password_reset#manage-students");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("admin-dashboard.jsp?error=db_error#manage-students");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("admin-dashboard.jsp#manage-students");
    }
}
