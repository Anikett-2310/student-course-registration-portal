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

/**
 * AdminLoginServlet.java
 * Authenticates faculty/administrator users:
 * 1. Reads username and password from POST request.
 * 2. Uses PreparedStatement to query the 'admin' table in MySQL.
 * 3. On success, creates an HttpSession with adminUser attribute.
 * 4. Redirects to admin-dashboard.jsp on success, or back to admin-login.html on failure.
 */
@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.sendRedirect("admin-login.html?error=invalid_credentials");
            return;
        }

        username = username.trim();
        password = password.trim();

        String sql = "SELECT username FROM admin WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Valid admin credentials - create session
                    HttpSession session = request.getSession();
                    session.setAttribute("adminUser", rs.getString("username"));
                    session.setAttribute("role", "admin");

                    response.sendRedirect("admin-dashboard.jsp");
                } else {
                    response.sendRedirect("admin-login.html?error=invalid_credentials");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error during admin login: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("admin-login.html?error=db_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("admin-login.html");
    }
}
