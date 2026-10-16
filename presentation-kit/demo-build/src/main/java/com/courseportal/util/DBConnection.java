package com.courseportal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java (Presentation Demo Build)
 *
 * Configurable JDBC connection utility for the portable presentation kit.
 * STRICT SAFETY RULES:
 * 1. Zero hardcoded database credentials or passwords.
 * 2. Zero silent fallback to 'root' / 'root'.
 * 3. Enforces PORTAL_DB_URL, PORTAL_DB_USER, and PORTAL_DB_PASSWORD
 *    (or system properties: courseportal.db.url, courseportal.db.user, courseportal.db.password).
 * 4. Fails fast with clear descriptive error if demo configuration is missing.
 */
public class DBConnection {

    private static volatile String overrideUrl = null;

    // Static block to load MySQL JDBC Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver not found in classpath/WEB-INF/lib.");
            e.printStackTrace();
        }
    }

    /**
     * Allows test runners to direct connections to an isolated disposable test database.
     */
    public static synchronized void setOverrideUrl(String url) {
        overrideUrl = url;
    }

    /**
     * Obtains a database Connection using environment variables or JVM system properties.
     * @return Connection object to configured demo database
     * @throws SQLException if configuration is missing or database access fails
     */
    public static Connection getConnection() throws SQLException {
        // Resolve URL
        String sysPropUrl = System.getProperty("courseportal.db.url");
        String envUrl = System.getenv("PORTAL_DB_URL");
        String activeUrl = (overrideUrl != null) ? overrideUrl :
                           (sysPropUrl != null && !sysPropUrl.trim().isEmpty() ? sysPropUrl.trim() :
                           (envUrl != null && !envUrl.trim().isEmpty() ? envUrl.trim() : null));

        // Resolve User
        String sysPropUser = System.getProperty("courseportal.db.user");
        String envUser = System.getenv("PORTAL_DB_USER");
        String activeUser = (sysPropUser != null && !sysPropUser.trim().isEmpty()) ? sysPropUser.trim() :
                            (envUser != null && !envUser.trim().isEmpty() ? envUser.trim() : null);

        // Resolve Password
        String sysPropPass = System.getProperty("courseportal.db.password");
        String envPass = System.getenv("PORTAL_DB_PASSWORD");
        String activePassword = (sysPropPass != null) ? sysPropPass : envPass;

        // Fail fast if any parameter is missing - NO silent fallback to root/root
        if (activeUrl == null || activeUser == null || activePassword == null) {
            StringBuilder missing = new StringBuilder();
            if (activeUrl == null) missing.append("PORTAL_DB_URL (-Dcourseportal.db.url) ");
            if (activeUser == null) missing.append("PORTAL_DB_USER (-Dcourseportal.db.user) ");
            if (activePassword == null) missing.append("PORTAL_DB_PASSWORD (-Dcourseportal.db.password) ");

            String errorMsg = "CRITICAL CONFIGURATION ERROR: Demo database configuration is missing required parameters: [" +
                    missing.toString().trim() + "]. " +
                    "To fix this, edit 'config/portal-env.bat' and specify your demo MySQL parameters, " +
                    "then launch via 'Start-Portal.bat'. " +
                    "This presentation build contains zero hardcoded fallback credentials.";
            System.err.println(errorMsg);
            throw new SQLException(errorMsg);
        }

        return DriverManager.getConnection(activeUrl, activeUser, activePassword);
    }
}
