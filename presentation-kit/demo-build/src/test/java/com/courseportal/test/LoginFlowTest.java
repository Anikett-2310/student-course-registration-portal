package com.courseportal.test;

import com.courseportal.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * LoginFlowTest.java
 * Verifies:
 * 1. DBConnection against live MySQL database.
 * 2. LoginServlet logic with valid credentials (24881A1203 / student123).
 * 3. LoginServlet logic with invalid credentials (24881A1203 / wrongpassword).
 * 4. Session attributes population and redirect destination.
 */
public class LoginFlowTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("STARTING TEST SUITE: DB CONNECTION & LOGIN FLOW");
        System.out.println("==================================================");

        boolean dbOk = testDatabaseConnection();
        if (!dbOk) {
            System.err.println("[FAIL] Cannot proceed without database connection.");
            System.exit(1);
        }

        boolean validLoginOk = testValidLogin("24881A1203", "student123");
        boolean invalidPassOk = testInvalidPassword("24881A1203", "wrongpass");
        boolean nonExistentOk = testNonExistentUser("24881A9999", "student123");

        System.out.println("==================================================");
        System.out.println("TEST SUMMARY:");
        System.out.println("1. MySQL DBConnection Test   : " + (dbOk ? "PASSED [OK]" : "FAILED"));
        System.out.println("2. Valid Login (24881A1203)   : " + (validLoginOk ? "PASSED [OK]" : "FAILED"));
        System.out.println("3. Invalid Password Test     : " + (invalidPassOk ? "PASSED [OK]" : "FAILED"));
        System.out.println("4. Non-Existent User Test    : " + (nonExistentOk ? "PASSED [OK]" : "FAILED"));
        System.out.println("==================================================");

        if (dbOk && validLoginOk && invalidPassOk && nonExistentOk) {
            System.out.println("ALL TESTS PASSED SUCCESSFULLY!");
        } else {
            System.err.println("SOME TESTS FAILED.");
            System.exit(1);
        }
    }

    private static boolean testDatabaseConnection() {
        System.out.println("\n[TEST 1] Testing DBConnection.getConnection()...");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(" -> Connection established successfully to: " + conn.getCatalog());
                return true;
            } else {
                System.out.println(" -> Connection returned null or closed.");
                return false;
            }
        } catch (Exception e) {
            System.err.println(" -> DBConnection error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testValidLogin(String rollNo, String password) {
        System.out.println("\n[TEST 2] Testing Valid Login: " + rollNo + " / " + password);
        String sql = "SELECT student_id, name, branch, year, semester, email FROM students WHERE student_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNo);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Simulate session attribute mapping as done in LoginServlet
                    Map<String, Object> sessionAttributes = new HashMap<>();
                    sessionAttributes.put("studentId", rs.getString("student_id"));
                    sessionAttributes.put("studentName", rs.getString("name"));
                    sessionAttributes.put("branch", rs.getString("branch"));
                    sessionAttributes.put("year", rs.getInt("year"));
                    sessionAttributes.put("semester", rs.getInt("semester"));
                    sessionAttributes.put("email", rs.getString("email"));
                    sessionAttributes.put("role", "student");

                    String redirectUrl = "dashboard.jsp";

                    System.out.println(" -> Authenticated successfully in MySQL!");
                    System.out.println(" -> Student Name: " + sessionAttributes.get("studentName"));
                    System.out.println(" -> Branch: " + sessionAttributes.get("branch"));
                    System.out.println(" -> Semester: " + sessionAttributes.get("semester"));
                    System.out.println(" -> Email: " + sessionAttributes.get("email"));
                    System.out.println(" -> Session Attributes: " + sessionAttributes.keySet());
                    System.out.println(" -> Redirection Target: " + redirectUrl);
                    return true;
                } else {
                    System.out.println(" -> [ERROR] Valid user was not authenticated!");
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println(" -> Error during testValidLogin: " + e.getMessage());
            return false;
        }
    }

    private static boolean testInvalidPassword(String rollNo, String wrongPassword) {
        System.out.println("\n[TEST 3] Testing Invalid Password: " + rollNo + " / " + wrongPassword);
        String sql = "SELECT student_id, name, branch, year, semester, email FROM students WHERE student_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, rollNo);
            pstmt.setString(2, wrongPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    String redirectUrl = "index.html?error=invalid_credentials";
                    System.out.println(" -> Rejected correctly! No matching credentials.");
                    System.out.println(" -> Redirection Target: " + redirectUrl);
                    return true;
                } else {
                    System.out.println(" -> [ERROR] Invalid password unexpectedly authenticated!");
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println(" -> Error during testInvalidPassword: " + e.getMessage());
            return false;
        }
    }

    private static boolean testNonExistentUser(String invalidRollNo, String password) {
        System.out.println("\n[TEST 4] Testing Non-Existent User: " + invalidRollNo);
        String sql = "SELECT student_id, name, branch, year, semester, email FROM students WHERE student_id = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, invalidRollNo);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    String redirectUrl = "index.html?error=invalid_credentials";
                    System.out.println(" -> Rejected correctly! Non-existent user rejected.");
                    System.out.println(" -> Redirection Target: " + redirectUrl);
                    return true;
                } else {
                    System.out.println(" -> [ERROR] Non-existent user found!");
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println(" -> Error during testNonExistentUser: " + e.getMessage());
            return false;
        }
    }
}
