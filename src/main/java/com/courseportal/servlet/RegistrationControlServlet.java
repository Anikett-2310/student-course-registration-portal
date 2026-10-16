package com.courseportal.servlet;

import com.courseportal.util.CSRFUtil;
import com.courseportal.util.RegistrationWindowManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * RegistrationControlServlet.java
 * Handles administrator control of course registration windows per Year and Semester.
 */
@WebServlet("/RegistrationControlServlet")
public class RegistrationControlServlet extends HttpServlet {

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
            response.sendRedirect("admin-dashboard.jsp?error=invalid_csrf#registration-control");
            return;
        }

        String yearStr = request.getParameter("year");
        String semStr = request.getParameter("semester");
        String statusStr = request.getParameter("status"); // "OPEN" or "CLOSED"

        if (yearStr == null || semStr == null || statusStr == null) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_request#registration-control");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr.trim());
            int semester = Integer.parseInt(semStr.trim());
            boolean open = "OPEN".equalsIgnoreCase(statusStr.trim());

            if (year < 1 || year > 4 || semester < 1 || semester > 2) {
                response.sendRedirect("admin-dashboard.jsp?error=invalid_academic_standing#registration-control");
                return;
            }

            RegistrationWindowManager.setWindowStatus(year, semester, open);

            response.sendRedirect("admin-dashboard.jsp?msg=window_updated#registration-control");

        } catch (NumberFormatException e) {
            response.sendRedirect("admin-dashboard.jsp?error=invalid_request#registration-control");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("admin-dashboard.jsp#registration-control");
    }
}
