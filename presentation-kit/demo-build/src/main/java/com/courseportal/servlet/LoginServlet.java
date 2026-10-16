package com.courseportal.servlet;

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
import java.util.regex.Pattern;

/**
 * LoginServlet.java
 * Handles student authentication:
 * 1. Reads studentId and password from POST request.
 * 2. Uses PreparedStatement to query the 'students' table in MySQL.
 * 3. On success, creates an HttpSession and stores student attributes.
 * 4. Redirects to dashboard.jsp on success, or back to index.html on failure.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Roll-number pattern aligned with StudentManagementServlet.java
    private static final Pattern ROLL_PATTERN = Pattern.compile(
            "^(\\d{2}881A[0-9A-Za-z]{4}|[0-9]{2}[0-9A-Za-z]{8})$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Retrieve form inputs
        String studentId = request.getParameter("studentId");
        String password = request.getParameter("password");

        // Basic validation for non-empty input
        if (studentId == null || password == null || studentId.trim().isEmpty() || password.trim().isEmpty()) {
            response.sendRedirect("index.html?error=invalid_credentials");
            return;
        }

        studentId = studentId.trim();
        password = password.trim();

        // Format validation - aligned with StudentManagementServlet
        if (!ROLL_PATTERN.matcher(studentId).matches()) {
            response.sendRedirect("index.html?error=invalid_format");
            return;
        }

        // 2. Query MySQL database using JDBC
        String sql = "SELECT student_id, name, branch, year, semester, email FROM students WHERE student_id = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Credentials are valid - create new HttpSession
                    HttpSession session = request.getSession();
                    session.setAttribute("studentId", rs.getString("student_id"));
                    session.setAttribute("studentName", rs.getString("name"));
                    session.setAttribute("branch", rs.getString("branch"));
                    session.setAttribute("year", rs.getInt("year"));
                    session.setAttribute("semester", rs.getInt("semester"));
                    session.setAttribute("email", rs.getString("email"));
                    session.setAttribute("role", "student");

                    // Redirect to student dashboard
                    response.sendRedirect("dashboard.jsp");
                } else {
                    // Invalid credentials
                    response.sendRedirect("index.html?error=invalid_credentials");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error during student login: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("index.html?error=db_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Direct GET request to LoginServlet redirects to the login form
        response.sendRedirect("index.html");
    }
}
