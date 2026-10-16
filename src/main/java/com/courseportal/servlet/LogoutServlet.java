package com.courseportal.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * LogoutServlet.java
 * Terminates the user's active session:
 * 1. Retrieves existing HttpSession if present.
 * 2. Calls session.invalidate() to clear all session attributes.
 * 3. Redirects to index.html with logged_out message.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    private void processLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Retrieve existing session, do not create a new one
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        // Redirect to login page with feedback flag
        response.sendRedirect("index.html?msg=logged_out");
    }
}
