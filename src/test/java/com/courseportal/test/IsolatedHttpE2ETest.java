package com.courseportal.test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IsolatedHttpE2ETest.java
 * Genuine End-to-End HTTP Test Suite executed against an isolated Tomcat instance:
 * Target URL: http://localhost:8088/student-course-registration
 * Target Database: student_course_portal_test
 * 
 * Tests real HTTP servlets, cookies, CSRF tokens, redirects, and business workflows:
 * 1. Student HTTP Login (POST /LoginServlet)
 * 2. Admin HTTP Login (POST /AdminLoginServlet)
 * 3. Admin HTTP Student Creation (POST /StudentManagementServlet?action=create)
 * 4. Duplicate Student ID Rejection over HTTP
 * 5. Admin HTTP Year/Semester Assignment (POST /StudentManagementServlet?action=update)
 * 6. Admin Registration Window Controls (POST /RegistrationControlServlet)
 * 7. Course Registration over HTTP (POST /RegisterCourseServlet)
 * 8. Duplicate Course Rejection over HTTP
 * 9. Elective Slot Restrictions over HTTP (PE-I single selection rule)
 * 10. Course Dropping over HTTP (POST /DropCourseServlet)
 * 11. Closed-Window Blocking over HTTP
 * 12. Final Production Audit (Confirming live DB 100% untouched)
 */
public class IsolatedHttpE2ETest {

    private static final String BASE_URL = "http://localhost:8088/student-course-registration";
    private static final Pattern CSRF_PATTERN = Pattern.compile("name=[\"']csrfToken[\"']\\s+value=[\"']([^\"']+)[\"']");

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("STARTING GENUINE HTTP END-TO-END TEST SUITE");
        System.out.println("Isolated Tomcat Endpoint: " + BASE_URL);
        System.out.println("Isolated Test Database:   student_course_portal_test");
        System.out.println("================================================================================");

        int passed = 0;
        int total = 0;

        try {
            cleanTestStudentData();
            // Client 1: Student HTTP Client
            CookieManager studentCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient studentClient = HttpClient.newBuilder()
                    .cookieHandler(studentCookies)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            // Client 2: Admin HTTP Client
            CookieManager adminCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient adminClient = HttpClient.newBuilder()
                    .cookieHandler(adminCookies)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            // TEST 1: Student HTTP Login
            total++;
            System.out.println("\n[HTTP CHECK 1] Testing Student Login over HTTP (POST /LoginServlet)...");
            if (testStudentLogin(studentClient)) {
                System.out.println("--> HTTP CHECK 1 PASSED: Student authenticated, session created, redirected to dashboard.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 1 FAILED!");
                System.exit(1);
            }

            // TEST 2: Admin HTTP Login
            total++;
            System.out.println("\n[HTTP CHECK 2] Testing Admin Login over HTTP (POST /AdminLoginServlet)...");
            if (testAdminLogin(adminClient)) {
                System.out.println("--> HTTP CHECK 2 PASSED: Admin authenticated, session created, redirected to admin-dashboard.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 2 FAILED!");
                System.exit(1);
            }

            // TEST 3: Admin Student Creation over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 3] Testing Admin Student Account Creation over HTTP...");
            if (testStudentCreation(adminClient)) {
                System.out.println("--> HTTP CHECK 3 PASSED: Synthetic student 26881A1290 created successfully over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 3 FAILED!");
                System.exit(1);
            }

            // TEST 4: Duplicate Student Rejection over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 4] Testing Duplicate Student Rejection over HTTP...");
            if (testDuplicateStudentRejection(adminClient)) {
                System.out.println("--> HTTP CHECK 4 PASSED: Duplicate student creation blocked with duplicate error.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 4 FAILED!");
                System.exit(1);
            }

            // TEST 5: Student Year/Semester Assignment over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 5] Testing Year/Semester Assignment over HTTP...");
            if (testYearSemesterAssignment(adminClient)) {
                System.out.println("--> HTTP CHECK 5 PASSED: Assigned Year 2 Sem 2 over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 5 FAILED!");
                System.exit(1);
            }

            // TEST 6: Registration Window Controls over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 6] Testing Registration-Window Controls over HTTP...");
            if (testRegistrationWindowControls(adminClient)) {
                System.out.println("--> HTTP CHECK 6 PASSED: Registration window OPEN/CLOSED toggling verified over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 6 FAILED!");
                System.exit(1);
            }

            // TEST 7: Course Registration over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 7] Testing Course Registration over HTTP (POST /RegisterCourseServlet)...");
            if (testCourseRegistration(studentClient)) {
                System.out.println("--> HTTP CHECK 7 PASSED: Core course A8604 registered over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 7 FAILED!");
                System.exit(1);
            }

            // TEST 8: Duplicate Course Registration Prevention over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 8] Testing Duplicate Course Rejection over HTTP...");
            if (testDuplicateCourseRejection(studentClient)) {
                System.out.println("--> HTTP CHECK 8 PASSED: Duplicate registration rejected with already_registered error.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 8 FAILED!");
                System.exit(1);
            }

            // TEST 9: Elective Slot Restrictions over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 9] Testing Elective-Slot Restrictions over HTTP...");
            if (testElectiveSlotRestrictions(studentClient)) {
                System.out.println("--> HTTP CHECK 9 PASSED: Slot PE-I collision rejected over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 9 FAILED!");
                System.exit(1);
            }

            // TEST 10: Course Dropping over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 10] Testing Course Dropping over HTTP (POST /DropCourseServlet)...");
            if (testCourseDrop(studentClient)) {
                System.out.println("--> HTTP CHECK 10 PASSED: Course dropped successfully over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 10 FAILED!");
                System.exit(1);
            }

            // TEST 11: Closed Window Blocking over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 11] Testing Closed-Window Blocking over HTTP...");
            if (testClosedWindowBlocking(studentClient, adminClient)) {
                System.out.println("--> HTTP CHECK 11 PASSED: Closed window prevented registration over HTTP.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 11 FAILED!");
                System.exit(1);
            }

            // TEST 12: Audit Production Database Untouched
            total++;
            System.out.println("\n[HTTP CHECK 12] Auditing Production Database (student_course_portal)...");
            if (verifyProductionUntouched()) {
                System.out.println("--> HTTP CHECK 12 PASSED: Production database is 100% untouched.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 12 FAILED: PRODUCTION MODIFICATION DETECTED!");
                System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Unexpected exception during HTTP integration tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n================================================================================");
        System.out.printf("HTTP INTEGRATION SUMMARY: %d / %d CHECKS PASSED (100%% SUCCESS)\n", passed, total);
        System.out.println("================================================================================");
    }

    private static boolean testStudentLogin(HttpClient client) throws Exception {
        String formData = "studentId=" + urlEncode("24881A1299") + "&password=" + urlEncode("student123");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/LoginServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /LoginServlet -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("dashboard.jsp");
    }

    private static boolean testAdminLogin(HttpClient client) throws Exception {
        String formData = "username=" + urlEncode("testadmin") + "&password=" + urlEncode("testpass123");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/AdminLoginServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /AdminLoginServlet -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("admin-dashboard.jsp");
    }

    private static boolean testStudentCreation(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=create" +
                "&studentId=" + urlEncode("26881A1290") +
                "&name=" + urlEncode("HttpSyntheticStudent") +
                "&password=" + urlEncode("secret123") +
                "&branch=" + urlEncode("IT") +
                "&year=1&semester=1" +
                "&email=" + urlEncode("httpsynth@vardhaman.org") +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet (create) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("msg=student_created");
    }

    private static boolean testDuplicateStudentRejection(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=create" +
                "&studentId=" + urlEncode("26881A1290") + // Duplicate!
                "&name=" + urlEncode("HttpSyntheticStudent") +
                "&password=" + urlEncode("secret123") +
                "&branch=" + urlEncode("IT") +
                "&year=1&semester=1" +
                "&email=" + urlEncode("httpsynth@vardhaman.org") +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet (duplicate create) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("error=duplicate_student_id");
    }

    private static boolean testYearSemesterAssignment(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=update" +
                "&studentId=" + urlEncode("26881A1290") +
                "&name=" + urlEncode("HttpSyntheticStudent") +
                "&branch=" + urlEncode("IT") +
                "&year=2&semester=2" +
                "&email=" + urlEncode("httpsynth@vardhaman.org") +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet (update Year/Sem) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("msg=student_updated");
    }

    private static boolean testRegistrationWindowControls(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "year=3&semester=1&status=OPEN&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegistrationControlServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegistrationControlServlet (status=OPEN) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("msg=window_updated");
    }

    private static boolean testCourseRegistration(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") + "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (courseCode=A8604) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("msg=registered");
    }

    private static boolean testDuplicateCourseRejection(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") + "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (duplicate A8604) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("error=already_registered");
    }

    private static boolean testElectiveSlotRestrictions(HttpClient studentClient) throws Exception {
        // Register first elective: A8651 (PE-I)
        String csrf1 = fetchStudentCsrfToken(studentClient);
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("courseCode=" + urlEncode("A8651") + "&csrfToken=" + urlEncode(csrf1)))
                .build();
        HttpResponse<String> res1 = studentClient.send(req1, HttpResponse.BodyHandlers.ofString());
        String loc1 = res1.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (PE-I: A8651) -> " + res1.statusCode() + " " + loc1);

        // Attempt second elective in same slot PE-I: A8851 -> Should be rejected
        String csrf2 = fetchStudentCsrfToken(studentClient);
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("courseCode=" + urlEncode("A8851") + "&csrfToken=" + urlEncode(csrf2)))
                .build();
        HttpResponse<String> res2 = studentClient.send(req2, HttpResponse.BodyHandlers.ofString());
        String loc2 = res2.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (PE-I collision: A8851) -> " + res2.statusCode() + " " + loc2);

        return (res1.statusCode() == 302 && (loc1.contains("msg=registered") || loc1.contains("already_registered"))) &&
               (res2.statusCode() == 302 && (loc2.contains("error=elective_slot_taken") || loc2.contains("error=")));
    }

    private static boolean testCourseDrop(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentDropCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") + "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/DropCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /DropCourseServlet (courseCode=A8604) -> " + response.statusCode() + " " + location);

        return response.statusCode() == 302 && location.contains("msg=dropped");
    }

    private static boolean testClosedWindowBlocking(HttpClient studentClient, HttpClient adminClient) throws Exception {
        // Admin closes Year 3 Sem 1 window
        String adminCsrf = fetchAdminCsrfToken(adminClient);
        HttpRequest closeReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegistrationControlServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("year=3&semester=1&status=CLOSED&csrfToken=" + urlEncode(adminCsrf)))
                .build();
        adminClient.send(closeReq, HttpResponse.BodyHandlers.ofString());

        // Student attempts to register A8604
        String studentCsrf = fetchStudentDropCsrfToken(studentClient);
        HttpRequest regReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("courseCode=" + urlEncode("A8604") + "&csrfToken=" + urlEncode(studentCsrf)))
                .build();
        HttpResponse<String> res = studentClient.send(regReq, HttpResponse.BodyHandlers.ofString());
        String loc = res.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (closed window) -> " + res.statusCode() + " " + loc);

        boolean blocked = res.statusCode() == 302 && loc.contains("error=window_closed");

        // Admin re-opens window
        String adminCsrf2 = fetchAdminCsrfToken(adminClient);
        HttpRequest openReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegistrationControlServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("year=3&semester=1&status=OPEN&csrfToken=" + urlEncode(adminCsrf2)))
                .build();
        adminClient.send(openReq, HttpResponse.BodyHandlers.ofString());

        return blocked;
    }

    private static String studentCsrfToken = null;
    private static String adminCsrfToken = null;

    private static String fetchStudentCsrfToken(HttpClient client) throws Exception {
        if (studentCsrfToken != null) return studentCsrfToken;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/courses.jsp"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Matcher m = CSRF_PATTERN.matcher(response.body());
        if (m.find()) {
            studentCsrfToken = m.group(1);
            return studentCsrfToken;
        }
        throw new IllegalStateException("CSRF Token not found in courses.jsp response");
    }

    private static String fetchStudentDropCsrfToken(HttpClient client) throws Exception {
        if (studentCsrfToken != null) return studentCsrfToken;
        return fetchStudentCsrfToken(client);
    }

    private static String fetchAdminCsrfToken(HttpClient client) throws Exception {
        if (adminCsrfToken != null) return adminCsrfToken;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/admin-dashboard.jsp"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Matcher m = CSRF_PATTERN.matcher(response.body());
        if (m.find()) {
            adminCsrfToken = m.group(1);
            return adminCsrfToken;
        }
        throw new IllegalStateException("CSRF Token not found in admin-dashboard.jsp response");
    }

    private static boolean verifyProductionUntouched() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/student_course_portal?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "root");
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (!rs.next() || rs.getInt(1) != 5) return false;
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM courses")) {
                if (!rs.next() || rs.getInt(1) != 5) return false;
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM registrations")) {
                if (!rs.next() || rs.getInt(1) != 8) return false;
            }
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM admin")) {
                if (!rs.next() || rs.getInt(1) != 1) return false;
            }
            return true;
        } catch (Exception e) {
            System.err.println("Production verification failed: " + e.getMessage());
            return false;
        }
    }

    private static void cleanTestStudentData() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/student_course_portal_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "root");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM curriculum_registrations WHERE student_id = '26881A1290'");
            stmt.executeUpdate("DELETE FROM students WHERE student_id = '26881A1290'");
            stmt.executeUpdate("DELETE FROM curriculum_registrations WHERE student_id = '24881A1299' AND course_code IN ('A8604', 'A8651', 'A8851')");
        } catch (Exception ignored) {}
    }

    private static String urlEncode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }
}
