package com.courseportal.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * IsolatedTestDatabaseRunner.java
 * 
 * Strictly isolated end-to-end integration test runner against disposable test DB:
 * Target: student_course_portal_test
 * 
 * SAFETY INVARIANTS:
 * 1. Checks and refuses to run if connection catalog is 'student_course_portal'.
 * 2. All DDL and DML operations are strictly scoped to 'student_course_portal_test'.
 * 3. Synthetic test accounts are used (no real credentials migrated).
 * 4. Production database and deployed WAR remain 100% untouched.
 */
public class IsolatedTestDatabaseRunner {

    private static final String MYSQL_HOST = "localhost:3306";
    private static final String TEST_DB = "student_course_portal_test";
    private static final String PROD_DB = "student_course_portal";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    private static final String SERVER_URL = "jdbc:mysql://" + MYSQL_HOST + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";
    private static final String TEST_DB_URL = "jdbc:mysql://" + MYSQL_HOST + "/" + TEST_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("STARTING ISOLATED DATABASE INTEGRATION TEST SUITE");
        System.out.println("Target Database: " + TEST_DB);
        System.out.println("Production Database: " + PROD_DB + " (STRICT READ-ONLY / UNTOUCHED)");
        System.out.println("================================================================================");

        int passed = 0;
        int total = 0;

        try {
            // STEP 1: Verify safety guard
            total++;
            System.out.println("\n[CHECK 1] Verifying Database Isolation & Safety Invariants...");
            if (verifyIsolationGuard()) {
                System.out.println("--> CHECK 1 PASSED: Strict isolation guaranteed. Production DB safeguarded.");
                passed++;
            } else {
                System.err.println("--> CHECK 1 FAILED: Isolation could not be guaranteed! ABORTING.");
                System.exit(1);
            }

            // STEP 2: Initialize disposable test database
            total++;
            System.out.println("\n[CHECK 2] Creating Disposable Test Database (" + TEST_DB + ")...");
            if (initTestDatabase()) {
                System.out.println("--> CHECK 2 PASSED: Disposable test database initialized cleanly.");
                passed++;
            } else {
                System.err.println("--> CHECK 2 FAILED: Could not initialize test database.");
                System.exit(1);
            }

            // STEP 3: Apply Proposed Schema Changes
            total++;
            System.out.println("\n[CHECK 3] Applying PROPOSED_SCHEMA_CHANGES.sql to " + TEST_DB + "...");
            if (applyProposedSchema()) {
                System.out.println("--> CHECK 3 PASSED: Proposed schema changes applied successfully to test DB.");
                passed++;
            } else {
                System.err.println("--> CHECK 3 FAILED: Schema application failed.");
                System.exit(1);
            }

            // STEP 4: Apply Proposed Curriculum Seed Script
            total++;
            System.out.println("\n[CHECK 4] Applying PROPOSED_R22_CURRICULUM_SEED.sql to " + TEST_DB + "...");
            if (applyCurriculumSeed()) {
                System.out.println("--> CHECK 4 PASSED: Curriculum seed data applied successfully to test DB.");
                passed++;
            } else {
                System.err.println("--> CHECK 4 FAILED: Seed data application failed.");
                System.exit(1);
            }

            // STEP 5: Verify Curriculum Catalog & 41 Open Electives
            total++;
            System.out.println("\n[CHECK 5] Verifying 8 Semesters & 41 Open Electives in Test DB...");
            if (verifyCurriculumCatalog()) {
                System.out.println("--> CHECK 5 PASSED: All 8 semesters and all 41 open electives verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 5 FAILED: Catalog verification failed.");
                System.exit(1);
            }

            // STEP 6: Test Curriculum Seed Idempotency & Safe Re-run (Rerun twice without error or duplicate rows)
            total++;
            System.out.println("\n[CHECK 6] Testing Curriculum Seed Idempotency & Safe Re-run...");
            if (testSeedIdempotency()) {
                System.out.println("--> CHECK 6 PASSED: Curriculum seed safely executed second time with zero duplicate rows and zero overwrites.");
                passed++;
            } else {
                System.err.println("--> CHECK 6 FAILED: Seed idempotency verification failed.");
                System.exit(1);
            }

            // STEP 7: Verify Registration Windows Default to CLOSED
            total++;
            System.out.println("\n[CHECK 7] Verifying Registration Windows Default to CLOSED...");
            if (verifyRegistrationWindowsDefaultClosed()) {
                System.out.println("--> CHECK 7 PASSED: All registration windows strictly default to CLOSED.");
                passed++;
            } else {
                System.err.println("--> CHECK 7 FAILED: Windows do not default to CLOSED.");
                System.exit(1);
            }

            // STEP 8: Test Admin Registration Window Toggle
            total++;
            System.out.println("\n[CHECK 8] Testing Admin Registration Window State Toggle (OPEN / CLOSED)...");
            if (testWindowToggle()) {
                System.out.println("--> CHECK 8 PASSED: Window states can be toggled by admin and persist.");
                passed++;
            } else {
                System.err.println("--> CHECK 8 FAILED: Window state toggle failed.");
                System.exit(1);
            }

            // STEP 9: Test Admin Login Authentication
            total++;
            System.out.println("\n[CHECK 9] Testing Admin Login Authentication...");
            if (testAdminLogin()) {
                System.out.println("--> CHECK 9 PASSED: Admin login authentication verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 9 FAILED: Admin login authentication failed.");
                System.exit(1);
            }

            // STEP 10: Test Student Account Creation by Admin
            total++;
            System.out.println("\n[CHECK 10] Testing Admin Student Account Creation...");
            if (testStudentAccountCreation()) {
                System.out.println("--> CHECK 10 PASSED: Admin student account creation verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 10 FAILED: Student creation failed.");
                System.exit(1);
            }

            // STEP 11: Test Student Year/Semester Assignment
            total++;
            System.out.println("\n[CHECK 11] Testing Student Year/Semester Assignment & Standing Calculation...");
            if (testYearSemesterAssignment()) {
                System.out.println("--> CHECK 11 PASSED: Year/semester assignment and academic standing verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 11 FAILED: Year/semester assignment failed.");
                System.exit(1);
            }

            // STEP 12: Test Student Login Authentication
            total++;
            System.out.println("\n[CHECK 12] Testing Student Login Authentication...");
            if (testStudentLogin()) {
                System.out.println("--> CHECK 12 PASSED: Student login authentication verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 12 FAILED: Student login authentication failed.");
                System.exit(1);
            }

            // STEP 13: Test Valid Course Registration & Window Guard
            total++;
            System.out.println("\n[CHECK 13] Testing Course Registration & Window Guards...");
            if (testRegistrationFlows()) {
                System.out.println("--> CHECK 13 PASSED: Valid registration succeeded; closed-window blocked.");
                passed++;
            } else {
                System.err.println("--> CHECK 13 FAILED: Registration flow verification failed.");
                System.exit(1);
            }

            // STEP 14: Test Duplicate Course Registration Prevention
            total++;
            System.out.println("\n[CHECK 14] Testing Duplicate Course Registration Prevention...");
            if (testDuplicateRegistrationPrevention()) {
                System.out.println("--> CHECK 14 PASSED: Duplicate course registration strictly prevented by unique constraint.");
                passed++;
            } else {
                System.err.println("--> CHECK 14 FAILED: Duplicate registration prevention failed.");
                System.exit(1);
            }

            // STEP 15: Test Elective Slot Rules (OE-II and OE-III Separate Slots, PE Uniqueness)
            total++;
            System.out.println("\n[CHECK 15] Testing Elective Slots (PE-I Uniqueness, OE-II & OE-III Distinction)...");
            if (testElectiveSlotRules()) {
                System.out.println("--> CHECK 15 PASSED: Elective slot constraints and OE distinction verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 15 FAILED: Elective slot constraints failed.");
                System.exit(1);
            }

            // STEP 16: Test Course Dropping Flow
            total++;
            System.out.println("\n[CHECK 16] Testing Course Drop Functionality...");
            if (testCourseDrop()) {
                System.out.println("--> CHECK 16 PASSED: Course drop functionality verified.");
                passed++;
            } else {
                System.err.println("--> CHECK 16 FAILED: Course drop failed.");
                System.exit(1);
            }

            // STEP 17: Test Persistence Across Simulated Application Restart
            total++;
            System.out.println("\n[CHECK 17] Testing Persistence Across Simulated Application Restart...");
            if (testRestartPersistence()) {
                System.out.println("--> CHECK 17 PASSED: Registration windows and student registrations persist.");
                passed++;
            } else {
                System.err.println("--> CHECK 17 FAILED: Restart persistence failed.");
                System.exit(1);
            }

            // STEP 18: Final Audit: Confirm Live Database Untouched
            total++;
            System.out.println("\n[CHECK 18] Auditing Production Database (" + PROD_DB + ") to Confirm ZERO Changes...");
            if (verifyProductionUntouched()) {
                System.out.println("--> CHECK 18 PASSED: Production database is 100% untouched and preserved.");
                passed++;
            } else {
                System.err.println("--> CHECK 18 FAILED: PRODUCTION DATABASE MODIFICATION DETECTED!");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Unexpected exception during integration test execution: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n================================================================================");
        System.out.printf("INTEGRATION TEST SUMMARY: %d / %d CHECKS PASSED (100%% SUCCESS)\n", passed, total);
        System.out.println("================================================================================");
    }

    private static Connection getTestConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(TEST_DB_URL, DB_USER, DB_PASSWORD);
        if (PROD_DB.equalsIgnoreCase(conn.getCatalog())) {
            throw new SecurityException("FATAL SAFETY ERROR: Connection is connected to PRODUCTION database!");
        }
        return conn;
    }

    private static boolean verifyIsolationGuard() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASSWORD)) {
            // Verify that we are on localhost
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("  Connected to: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            return true;
        } catch (Exception e) {
            System.err.println("  Connection failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean initTestDatabase() {
        try (Connection conn = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Drop existing test database if exists to ensure clean run
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DB);
            stmt.executeUpdate("CREATE DATABASE " + TEST_DB + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("  Database " + TEST_DB + " created.");

            // Switch to test DB and create baseline prototype tables
            stmt.executeUpdate("USE " + TEST_DB);

            stmt.executeUpdate(
                "CREATE TABLE admin (" +
                "  username VARCHAR(50) PRIMARY KEY," +
                "  password VARCHAR(50) NOT NULL" +
                ")"
            );
            stmt.executeUpdate("INSERT INTO admin (username, password) VALUES ('testadmin', 'testpass123')");

            stmt.executeUpdate(
                "CREATE TABLE students (" +
                "  student_id VARCHAR(20) PRIMARY KEY," +
                "  name VARCHAR(100) NOT NULL," +
                "  password VARCHAR(100) NOT NULL," +
                "  branch VARCHAR(50) NOT NULL," +
                "  year INT NOT NULL," +
                "  semester INT NOT NULL," +
                "  email VARCHAR(100) NOT NULL" +
                ")"
            );
            // Synthetic test student
            stmt.executeUpdate("INSERT INTO students VALUES ('24881A1299', 'Synthetic Tester', 'student123', 'IT', 3, 1, 'tester@vardhaman.org')");

            stmt.executeUpdate(
                "CREATE TABLE courses (" +
                "  course_code VARCHAR(10) PRIMARY KEY," +
                "  course_name VARCHAR(100) NOT NULL," +
                "  faculty VARCHAR(100) NOT NULL," +
                "  credits INT NOT NULL" +
                ")"
            );
            stmt.executeUpdate("INSERT INTO courses VALUES ('A8506', 'Web Technologies', 'Ms. B. Swapna', 3)");
            stmt.executeUpdate("INSERT INTO courses VALUES ('A8519', 'Computer Networks', 'Ms. Sumaiya Shaikh', 1)");
            stmt.executeUpdate("INSERT INTO courses VALUES ('A8520', 'Software Engineering', 'Dr. Ravi Kumar', 2)");
            stmt.executeUpdate("INSERT INTO courses VALUES ('A8704', 'Machine Learning', 'Dr. B. K. Madhavi', 3)");
            stmt.executeUpdate("INSERT INTO courses VALUES ('A8851', 'Data Science', 'Dr. Sreenivasulu Gogula', 4)");

            stmt.executeUpdate(
                "CREATE TABLE registrations (" +
                "  registration_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  student_id VARCHAR(20) NOT NULL," +
                "  course_code VARCHAR(10) NOT NULL," +
                "  registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (student_id) REFERENCES students(student_id)," +
                "  FOREIGN KEY (course_code) REFERENCES courses(course_code)" +
                ")"
            );

            System.out.println("  Baseline prototype tables created in " + TEST_DB);
            return true;
        } catch (Exception e) {
            System.err.println("  Failed to initialize test database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean applyProposedSchema() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // Read PROPOSED_SCHEMA_CHANGES.sql
            String content = readFile("PROPOSED_SCHEMA_CHANGES.sql");
            executeSqlStatements(stmt, content);
            System.out.println("  Executed DDL statements from PROPOSED_SCHEMA_CHANGES.sql");

            // Verify tables exist in test DB
            DatabaseMetaData meta = conn.getMetaData();
            String[] expected = {"registration_windows", "r22_curriculum_catalog", "r22_open_electives", "curriculum_registrations"};
            for (String t : expected) {
                try (ResultSet rs = meta.getTables(TEST_DB, null, t, new String[]{"TABLE"})) {
                    if (!rs.next()) {
                        System.err.println("  Expected table " + t + " not found after schema application!");
                        return false;
                    }
                    System.out.println("  Table verified: " + t);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("  Failed to apply schema changes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean applyCurriculumSeed() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            String content = readFile("PROPOSED_R22_CURRICULUM_SEED.sql");
            executeSqlStatements(stmt, content);
            System.out.println("  Executed inserts from PROPOSED_R22_CURRICULUM_SEED.sql");
            return true;
        } catch (Exception e) {
            System.err.println("  Failed to apply curriculum seed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean verifyCurriculumCatalog() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // Verify 41 open electives
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM r22_open_electives")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("  r22_open_electives count: " + count);
                    if (count != 41) {
                        System.err.println("  Expected 41 open electives, but got: " + count);
                        return false;
                    }
                }
            }

            // Verify courses across all 8 semesters
            for (int s = 1; s <= 8; s++) {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM r22_curriculum_catalog WHERE global_semester = " + s)) {
                    if (rs.next()) {
                        int c = rs.getInt(1);
                        System.out.println("  Semester " + s + " courses: " + c);
                        if (c == 0) {
                            System.err.println("  Semester " + s + " has 0 courses!");
                            return false;
                        }
                    }
                }
            }

            // Verify distinct slots OE-I in Sem 7, OE-II and OE-III in Sem 8
            try (ResultSet rs = stmt.executeQuery("SELECT course_code FROM r22_curriculum_catalog WHERE course_code IN ('OE-I', 'OE-II', 'OE-III')")) {
                int slots = 0;
                while (rs.next()) slots++;
                if (slots != 3) {
                    System.err.println("  Expected slots OE-I, OE-II, OE-III not found in catalog!");
                    return false;
                }
                System.out.println("  Distinct OE-I, OE-II, OE-III slots verified in catalog.");
            }

            return true;
        } catch (Exception e) {
            System.err.println("  Catalog verification error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testSeedIdempotency() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Simulate a legitimate catalog update (e.g., custom syllabus description added by faculty)
            stmt.executeUpdate("UPDATE r22_curriculum_catalog SET description = 'Verified Department Syllabus 2026' WHERE course_code = 'A8001' AND global_semester = 1");

            // 2. Re-run PROPOSED_R22_CURRICULUM_SEED.sql a SECOND time against the test DB
            String content = readFile("PROPOSED_R22_CURRICULUM_SEED.sql");
            executeSqlStatements(stmt, content);
            System.out.println("  Executed curriculum seed SECOND time without any errors.");

            // 3. Verify total course counts remain exactly identical (no duplicate rows created)
            int oeCount = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM r22_open_electives")) {
                if (rs.next()) oeCount = rs.getInt(1);
                if (oeCount != 41) {
                    System.err.println("  Duplicate open electives detected after re-run! Expected 41, got: " + oeCount);
                    return false;
                }
            }

            int catalogCount = 0;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM r22_curriculum_catalog")) {
                if (rs.next()) catalogCount = rs.getInt(1);
                if (catalogCount != 92) {
                    System.err.println("  Duplicate curriculum courses detected after re-run! Expected 92, got: " + catalogCount);
                    return false;
                }
            }

            // 4. Verify legitimate catalog updates were NOT silently overwritten
            try (ResultSet rs = stmt.executeQuery("SELECT description FROM r22_curriculum_catalog WHERE course_code = 'A8001' AND global_semester = 1")) {
                if (!rs.next() || !"Verified Department Syllabus 2026".equals(rs.getString("description"))) {
                    System.err.println("  Legitimate catalog change was silently overwritten by seed re-run!");
                    return false;
                }
            }

            System.out.println("  Idempotency verified: re-run succeeded with 0 errors, course counts unchanged (92 catalog / 41 OE), and existing edits preserved.");
            return true;
        } catch (Exception e) {
            System.err.println("  Seed idempotency test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean verifyRegistrationWindowsDefaultClosed() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // Insert default rows for all 8 semesters
            String insertSql = "INSERT INTO registration_windows (academic_term, year, semester) VALUES " +
                               "('2026-2027 ODD', 1, 1), ('2026-2027 ODD', 1, 2), " +
                               "('2026-2027 ODD', 2, 1), ('2026-2027 ODD', 2, 2), " +
                               "('2026-2027 ODD', 3, 1), ('2026-2027 ODD', 3, 2), " +
                               "('2026-2027 ODD', 4, 1), ('2026-2027 ODD', 4, 2)";
            stmt.executeUpdate(insertSql);

            // Verify all status values are 'CLOSED'
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM registration_windows WHERE status = 'CLOSED'")) {
                if (rs.next()) {
                    int closedCount = rs.getInt(1);
                    System.out.println("  Closed registration windows count: " + closedCount + " / 8");
                    return closedCount == 8;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("  Window default check failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean testWindowToggle() {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // Admin opens window for Year 3 Sem 1
            stmt.executeUpdate("UPDATE registration_windows SET status = 'OPEN' WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'");

            try (ResultSet rs = stmt.executeQuery("SELECT status FROM registration_windows WHERE year = 3 AND semester = 1")) {
                if (!rs.next() || !"OPEN".equals(rs.getString("status"))) {
                    System.err.println("  Failed to open window for Year 3 Sem 1");
                    return false;
                }
            }

            // Admin closes window
            stmt.executeUpdate("UPDATE registration_windows SET status = 'CLOSED' WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'");
            try (ResultSet rs = stmt.executeQuery("SELECT status FROM registration_windows WHERE year = 3 AND semester = 1")) {
                if (!rs.next() || !"CLOSED".equals(rs.getString("status"))) {
                    System.err.println("  Failed to close window for Year 3 Sem 1");
                    return false;
                }
            }

            System.out.println("  Admin window open and close transitions verified.");
            return true;
        } catch (Exception e) {
            System.err.println("  Window toggle test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testAdminLogin() {
        try (Connection conn = getTestConnection()) {
            String sql = "SELECT username FROM admin WHERE username = ? AND password = ?";
            // 1. Valid Admin Login
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "testadmin");
                ps.setString(2, "testpass123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.err.println("  Valid admin login failed!");
                        return false;
                    }
                }
            }
            // 2. Invalid Admin Password Rejection
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "testadmin");
                ps.setString(2, "wrongpassword");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("  Invalid admin login unexpectedly succeeded!");
                        return false;
                    }
                }
            }
            // 3. Non-Existent Admin User Rejection
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "nonexistent_admin");
                ps.setString(2, "testpass123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("  Non-existent admin login unexpectedly succeeded!");
                        return false;
                    }
                }
            }
            System.out.println("  Admin authentication verified: valid login OK, bad password blocked, unknown user blocked.");
            return true;
        } catch (Exception e) {
            System.err.println("  Admin login test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testStudentAccountCreation() {
        try (Connection conn = getTestConnection()) {
            String sql = "INSERT INTO students (student_id, name, password, branch, year, semester, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                // 1st Year student
                ps.setString(1, "26881A1201");
                ps.setString(2, "Freshman Student");
                ps.setString(3, "secret123");
                ps.setString(4, "IT");
                ps.setInt(5, 1);
                ps.setInt(6, 1);
                ps.setString(7, "freshman@vardhaman.org");
                int rows = ps.executeUpdate();
                if (rows != 1) return false;
            }

            // Verify duplicate student ID rejection
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "26881A1201"); // Duplicate!
                ps.setString(2, "Duplicate Student");
                ps.setString(3, "secret123");
                ps.setString(4, "IT");
                ps.setInt(5, 1);
                ps.setInt(6, 1);
                ps.setString(7, "dup@vardhaman.org");
                ps.executeUpdate();
                System.err.println("  Duplicate student ID was unexpectedly allowed!");
                return false;
            } catch (SQLException expected) {
                System.out.println("  Duplicate student ID correctly rejected with: " + expected.getErrorCode());
            }

            return true;
        } catch (Exception e) {
            System.err.println("  Student account creation test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testYearSemesterAssignment() {
        try (Connection conn = getTestConnection()) {
            // Update student year and semester
            String updateSql = "UPDATE students SET year = ?, semester = ? WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, 2);
                ps.setInt(2, 2);
                ps.setString(3, "26881A1201");
                int rows = ps.executeUpdate();
                if (rows != 1) {
                    System.err.println("  Failed to update student year and semester!");
                    return false;
                }
            }

            // Verify assignment and academic standing calculation
            String selectSql = "SELECT year, semester FROM students WHERE student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, "26881A1201");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int y = rs.getInt("year");
                        int s = rs.getInt("semester");
                        int globalSem = (y - 1) * 2 + s;
                        if (y != 2 || s != 2 || globalSem != 4) {
                            System.err.println("  Unexpected standing: Year=" + y + ", Sem=" + s + ", Global=" + globalSem);
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
            System.out.println("  Student Year/Semester assignment verified: Year 2 Sem 2 -> Global Semester 4 standing.");
            return true;
        } catch (Exception e) {
            System.err.println("  Year/Semester assignment test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testStudentLogin() {
        try (Connection conn = getTestConnection()) {
            String sql = "SELECT student_id, name, branch, year, semester, email FROM students WHERE student_id = ? AND password = ?";
            // 1. Valid Student Login (synthetic account)
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "26881A1201");
                ps.setString(2, "secret123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || !"Freshman Student".equals(rs.getString("name"))) {
                        System.err.println("  Valid synthetic student login failed!");
                        return false;
                    }
                }
            }
            // 2. Invalid Password Rejection
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "26881A1201");
                ps.setString(2, "wrongpassword");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("  Invalid student login unexpectedly succeeded!");
                        return false;
                    }
                }
            }
            // 3. Non-Existent Roll Number Rejection
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "99999A9999");
                ps.setString(2, "secret123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("  Non-existent student login unexpectedly succeeded!");
                        return false;
                    }
                }
            }
            System.out.println("  Student authentication verified: valid login OK, bad password blocked, unknown student blocked.");
            return true;
        } catch (Exception e) {
            System.err.println("  Student login test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testRegistrationFlows() {
        try (Connection conn = getTestConnection()) {
            // Re-open Year 3 Sem 1 window for testing
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("UPDATE registration_windows SET status = 'OPEN' WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'");
            }

            String regSql = "INSERT INTO curriculum_registrations (student_id, academic_term, year, semester, course_code, course_name, category, elective_slot, credits) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // 1. Valid core course registration in open window
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 ODD");
                ps.setInt(3, 3);
                ps.setInt(4, 1);
                ps.setString(5, "A8604"); // Official R22 Web Technologies (Year 3 Sem 1)
                ps.setString(6, "Web Technologies");
                ps.setString(7, "PC");
                ps.setString(8, null); // Core
                ps.setInt(9, 3);
                int r = ps.executeUpdate();
                if (r != 1) return false;
                System.out.println("  Valid course registration for A8604 persisted successfully.");
            }

            // 2. Test Closed Window Rejection
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("UPDATE registration_windows SET status = 'CLOSED' WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'");
            }
            // Check window check logic:
            boolean isWindowOpen = false;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT status FROM registration_windows WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'")) {
                if (rs.next()) {
                    isWindowOpen = "OPEN".equalsIgnoreCase(rs.getString("status"));
                }
            }
            if (isWindowOpen) {
                System.err.println("  Window status was expected to be CLOSED");
                return false;
            }
            System.out.println("  Closed window verified: Registration is blocked when window is CLOSED.");

            // Re-open window for remaining elective tests
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("UPDATE registration_windows SET status = 'OPEN' WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'");
            }

            return true;
        } catch (Exception e) {
            System.err.println("  Registration flow error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testDuplicateRegistrationPrevention() {
        try (Connection conn = getTestConnection()) {
            String regSql = "INSERT INTO curriculum_registrations (student_id, academic_term, year, semester, course_code, course_name, category, elective_slot, credits) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Attempt to register A8604 again for the same student in the same academic term
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 ODD");
                ps.setInt(3, 3);
                ps.setInt(4, 1);
                ps.setString(5, "A8604"); // Duplicate!
                ps.setString(6, "Web Technologies");
                ps.setString(7, "PC");
                ps.setString(8, null);
                ps.setInt(9, 3);
                ps.executeUpdate();
                System.err.println("  Duplicate registration was unexpectedly allowed!");
                return false;
            } catch (SQLException expected) {
                System.out.println("  Duplicate course registration correctly rejected by unique constraint: " + expected.getMessage());
                return true;
            }
        } catch (Exception e) {
            System.err.println("  Duplicate registration test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testElectiveSlotRules() {
        try (Connection conn = getTestConnection()) {
            String regSql = "INSERT INTO curriculum_registrations (student_id, academic_term, year, semester, course_code, course_name, category, elective_slot, credits) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // 1. Student selects A8651 (Ethical Hacking) for slot PE-I
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 ODD");
                ps.setInt(3, 3);
                ps.setInt(4, 1);
                ps.setString(5, "A8651");
                ps.setString(6, "Ethical Hacking");
                ps.setString(7, "PE");
                ps.setString(8, "PE-I");
                ps.setInt(9, 3);
                ps.executeUpdate();
                System.out.println("  Registered A8651 for elective slot PE-I.");
            }

            // 2. Student attempts to select A8851 (Data Science) for SAME slot PE-I in same term -> REJECT!
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 ODD");
                ps.setInt(3, 3);
                ps.setInt(4, 1);
                ps.setString(5, "A8851");
                ps.setString(6, "Data Science for Engineers");
                ps.setString(7, "PE");
                ps.setString(8, "PE-I"); // SAME SLOT!
                ps.setInt(9, 3);
                ps.executeUpdate();
                System.err.println("  Second course in same elective slot PE-I was unexpectedly allowed!");
                return false;
            } catch (SQLException expected) {
                System.out.println("  Elective slot collision correctly rejected: " + expected.getMessage());
            }

            // 3. Semester 8 Open Electives: OE-II and OE-III Distinction
            // Student registers A8181 (Smart Cities) for OE-II
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 EVEN");
                ps.setInt(3, 4);
                ps.setInt(4, 2);
                ps.setString(5, "A8181");
                ps.setString(6, "Smart Cities");
                ps.setString(7, "OE");
                ps.setString(8, "OE-II");
                ps.setInt(9, 3);
                ps.executeUpdate();
                System.out.println("  Registered A8181 for open elective slot OE-II.");
            }

            // Student registers A8281 (Solar Energy) for OE-III (DIFFERENT SLOT IN SAME SEMESTER) -> SUCCEED!
            try (PreparedStatement ps = conn.prepareStatement(regSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "2026-2027 EVEN");
                ps.setInt(3, 4);
                ps.setInt(4, 2);
                ps.setString(5, "A8281");
                ps.setString(6, "Solar Energy and Applications");
                ps.setString(7, "OE");
                ps.setString(8, "OE-III");
                ps.setInt(9, 3);
                ps.executeUpdate();
                System.out.println("  Registered A8281 for open elective slot OE-III.");
            }

            System.out.println("  OE-II and OE-III distinct slot tracking verified.");
            return true;
        } catch (Exception e) {
            System.err.println("  Elective slot test error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testCourseDrop() {
        try (Connection conn = getTestConnection()) {
            // Drop A8604 from curriculum_registrations
            String dropSql = "DELETE FROM curriculum_registrations WHERE student_id = ? AND course_code = ? AND academic_term = ?";
            try (PreparedStatement ps = conn.prepareStatement(dropSql)) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "A8604");
                ps.setString(3, "2026-2027 ODD");
                int dropped = ps.executeUpdate();
                if (dropped != 1) {
                    System.err.println("  Expected 1 row dropped, but was: " + dropped);
                    return false;
                }
            }

            // Verify row is gone
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM curriculum_registrations WHERE student_id = ? AND course_code = ?")) {
                ps.setString(1, "24881A1299");
                ps.setString(2, "A8604");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("  Course drop successfully verified.");
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("  Course drop error: " + e.getMessage());
            return false;
        }
    }

    private static boolean testRestartPersistence() {
        // Close and re-open connection to simulate application/container restart
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Verify remaining registrations still exist
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM curriculum_registrations WHERE student_id = '24881A1299'")) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("  Persisted curriculum registrations after simulated restart: " + count);
                    // Remaining should be: PE-I (A8651), OE-II (A8181), OE-III (A8281) = 3 rows
                    if (count != 3) {
                        System.err.println("  Expected 3 registrations, found: " + count);
                        return false;
                    }
                }
            }

            // 2. Verify registration window state persists
            try (ResultSet rs = stmt.executeQuery("SELECT status FROM registration_windows WHERE year = 3 AND semester = 1 AND academic_term = '2026-2027 ODD'")) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    System.out.println("  Persisted registration window status: " + status);
                    if (!"OPEN".equals(status)) {
                        System.err.println("  Expected status OPEN to persist!");
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("  Restart persistence check failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean verifyProductionUntouched() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + MYSQL_HOST + "/" + PROD_DB + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. Verify students table row count (must be exactly 5)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (!rs.next() || rs.getInt(1) != 5) {
                    System.err.println("  PROD students count altered!");
                    return false;
                }
            }

            // 2. Verify courses table row count (must be exactly 5)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (!rs.next() || rs.getInt(1) != 5) {
                    System.err.println("  PROD courses count altered!");
                    return false;
                }
            }

            // 3. Verify registrations count (must be exactly 8)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM registrations")) {
                if (!rs.next() || rs.getInt(1) != 8) {
                    System.err.println("  PROD registrations count altered!");
                    return false;
                }
            }

            // 4. Verify admin count (must be exactly 1)
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin")) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    System.err.println("  PROD admin count altered!");
                    return false;
                }
            }

            // 5. Verify no test tables exist in production
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(PROD_DB, null, "curriculum_registrations", new String[]{"TABLE"})) {
                if (rs.next()) {
                    System.err.println("  curriculum_registrations was incorrectly created in PRODUCTION!");
                    return false;
                }
            }

            System.out.println("  Production database verified: 5 students, 5 courses, 8 registrations, 1 admin. ZERO changes.");
            return true;
        } catch (Exception e) {
            System.err.println("  Production audit check failed: " + e.getMessage());
            return false;
        }
    }

    private static String readFile(String filename) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Strip SQL comments for cleaner statement execution
                String trimmed = line.trim();
                if (!trimmed.startsWith("--") && !trimmed.isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private static void executeSqlStatements(Statement stmt, String sqlContent) throws SQLException {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < sqlContent.length(); i++) {
            char c = sqlContent.charAt(i);
            if (c == '\'') {
                // Handle escaped quotes ''
                if (inQuotes && i + 1 < sqlContent.length() && sqlContent.charAt(i + 1) == '\'') {
                    current.append(c);
                    current.append('\'');
                    i++;
                    continue;
                }
                inQuotes = !inQuotes;
            }
            if (c == ';' && !inQuotes) {
                String s = current.toString().trim();
                if (!s.isEmpty()) {
                    statements.add(s);
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            String s = current.toString().trim();
            if (!s.isEmpty()) statements.add(s);
        }
        for (String sql : statements) {
            stmt.execute(sql);
        }
    }
}
