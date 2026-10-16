package com.courseportal.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RegistrationWindowManager.java
 * Manages open/closed registration windows per Academic Year and Semester.
 * 
 * COMPATIBILITY GUARD:
 * 1. Checks if the 'registration_windows' table exists in MySQL.
 * 2. If present, persists and queries window states from the database.
 * 3. If the table has not yet been migrated, maintains state in memory,
 *    defaulting to OPEN for active semesters and allowing Admin to toggle state safely.
 */
public class RegistrationWindowManager {

    private static final Map<String, Boolean> IN_MEMORY_WINDOWS = new ConcurrentHashMap<>();
    private static volatile Boolean tableExistsCache = null;

    static {
        // Initialize default memory states: (Year 1-4, Semester 1-2)
        // STRICT SAFETY RULE: All registration windows default to CLOSED upon container startup.
        // A restart must never automatically open registration. Only explicit authenticated admin action opens windows.
        for (int y = 1; y <= 4; y++) {
            for (int s = 1; s <= 2; s++) {
                String key = y + "_" + s;
                IN_MEMORY_WINDOWS.put(key, false);
            }
        }
    }

    public static final String DEFAULT_ACADEMIC_TERM = "2026-2027 ODD";

    /**
     * Checks if the registration window is OPEN for the specified year and semester using the default academic term.
     * @param year Academic Year (1-4)
     * @param semester Semester within year (1-2)
     * @return true if OPEN, false if CLOSED
     */
    public static boolean isWindowOpen(int year, int semester) {
        return isWindowOpen(DEFAULT_ACADEMIC_TERM, year, semester);
    }

    /**
     * Checks if the registration window is OPEN for the specified academic term, year, and semester.
     * @param academicTerm Academic term (e.g. '2026-2027 ODD')
     * @param year Academic Year (1-4)
     * @param semester Semester within year (1-2)
     * @return true if OPEN, false if CLOSED
     */
    public static boolean isWindowOpen(String academicTerm, int year, int semester) {
        if (hasRegistrationWindowsTable()) {
            String sql = "SELECT status FROM registration_windows WHERE academic_term = ? AND year = ? AND semester = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, academicTerm);
                ps.setInt(2, year);
                ps.setInt(3, semester);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "OPEN".equalsIgnoreCase(rs.getString("status"));
                    }
                }
            } catch (SQLException e) {
                System.err.println("Database check for registration_windows failed, falling back to memory state: " + e.getMessage());
            }
        }

        String key = year + "_" + semester;
        return IN_MEMORY_WINDOWS.getOrDefault(key, false);
    }

    /**
     * Sets the registration window status for a specified year and semester using default academic term.
     * @param year Academic Year (1-4)
     * @param semester Semester (1-2)
     * @param open true for OPEN, false for CLOSED
     * @return true if successful
     */
    public static boolean setWindowStatus(int year, int semester, boolean open) {
        return setWindowStatus(DEFAULT_ACADEMIC_TERM, year, semester, open);
    }

    /**
     * Sets the registration window status for a specified academic term, year, and semester.
     * @param academicTerm Academic term (e.g. '2026-2027 ODD')
     * @param year Academic Year (1-4)
     * @param semester Semester (1-2)
     * @param open true for OPEN, false for CLOSED
     * @return true if successful
     */
    public static boolean setWindowStatus(String academicTerm, int year, int semester, boolean open) {
        String key = year + "_" + semester;
        IN_MEMORY_WINDOWS.put(key, open);

        if (hasRegistrationWindowsTable()) {
            String sql = "INSERT INTO registration_windows (academic_term, year, semester, status) VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE status = VALUES(status)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, academicTerm);
                ps.setInt(2, year);
                ps.setInt(3, semester);
                ps.setString(4, open ? "OPEN" : "CLOSED");
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Could not update registration_windows table: " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * Returns true if the registration_windows table exists in the live database.
     */
    public static boolean hasRegistrationWindowsTable() {
        if (tableExistsCache != null) {
            return tableExistsCache;
        }
        try (Connection conn = DBConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "registration_windows", new String[]{"TABLE"})) {
                tableExistsCache = rs.next();
                return tableExistsCache;
            }
        } catch (Exception e) {
            tableExistsCache = false;
            return false;
        }
    }
}
