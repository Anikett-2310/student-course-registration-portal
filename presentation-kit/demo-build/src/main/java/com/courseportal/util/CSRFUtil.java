package com.courseportal.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRFUtil.java
 * Provides lightweight, secure CSRF token protection for state-changing forms.
 */
public class CSRFUtil {

    private static final String CSRF_SESSION_KEY = "CSRF_TOKEN";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Retrieves or generates a unique CSRF token for the current session.
     */
    public static String getToken(HttpSession session) {
        if (session == null) return "";
        synchronized (session) {
            String token = (String) session.getAttribute(CSRF_SESSION_KEY);
            if (token == null || token.trim().isEmpty()) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(CSRF_SESSION_KEY, token);
            }
            return token;
        }
    }

    /**
     * Validates that the request parameter 'csrfToken' matches the session token.
     */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_KEY);
        String requestToken = request.getParameter("csrfToken");

        if (sessionToken == null || requestToken == null) return false;
        return sessionToken.equals(requestToken);
    }
}
