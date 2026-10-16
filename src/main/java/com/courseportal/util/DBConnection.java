package com.courseportal.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * Utility class to establish connection with MySQL database using JDBC.
 * Suitable for 3rd Year Web Technologies Lab viva explanation:
 * 1. Loads MySQL JDBC Driver class.
 * 2. Uses DriverManager to obtain a Connection object.
 */
public class DBConnection {

    // Database connection parameters
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/student_course_portal?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static volatile String overrideUrl = null;

    // Static block to load the MySQL JDBC driver once when class is loaded
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found! Ensure mysql-connector-j jar is present in classpath/WEB-INF/lib.");
            e.printStackTrace();
        }
    }

    /**
     * Allows test runners to direct connections to an isolated disposable test database.
     * When null, defaults strictly to production configuration.
     */
    public static synchronized void setOverrideUrl(String url) {
        overrideUrl = url;
    }

    /**
     * Obtains a new database Connection.
     * @return Connection object to active database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        String sysPropUrl = System.getProperty("courseportal.db.url");
        String envUrl = System.getenv("PORTAL_DB_URL");
        String activeUrl = (overrideUrl != null) ? overrideUrl :
                           (sysPropUrl != null && !sysPropUrl.trim().isEmpty() ? sysPropUrl.trim() :
                           (envUrl != null && !envUrl.trim().isEmpty() ? envUrl.trim() : DEFAULT_URL));
        return DriverManager.getConnection(activeUrl, USER, PASSWORD);
    }
}
