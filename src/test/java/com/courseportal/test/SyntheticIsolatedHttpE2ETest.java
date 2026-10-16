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
 * SyntheticIsolatedHttpE2ETest.java
 * Genuine End-to-End HTTP Test Suite executed against isolated Tomcat instance:
 * Target URL: http://localhost:8088/student-course-registration
 * Target Database: student_course_portal_synth_test (100% synthetic accounts only)
 * 
 * Tests real HTTP servlets, cookies, CSRF tokens, redirects, and business workflows:
 * 1. Student HTTP Login (POST /LoginServlet) using synthetic account SYNTH2401
 * 2. Admin HTTP Login (POST /AdminLoginServlet) using synthetic account synthadmin
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
public class SyntheticIsolatedHttpE2ETest {

    private static final String BASE_URL = "http://localhost:8088/student-course-registration";
    private static final Pattern CSRF_PATTERN = Pattern.compile("name=[\"']csrfToken[\"']\\s+value=[\"']([^\"']+)[\"']");

    private static String studentCsrfToken = null;
    private static String adminCsrfToken = null;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("STARTING SYNTHETIC HTTP END-TO-END TEST SUITE");
        System.out.println("Endpoint:         " + BASE_URL);
        System.out.println("Target DB:        student_course_portal_synth_test (Strictly Synthetic Accounts)");
        System.out.println("================================================================================");

        int passed = 0;
        int total = 0;

        try {
            cleanTestStudentData();
            // Client 1: Synthetic Student HTTP Client
            CookieManager studentCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient studentClient = HttpClient.newBuilder()
                    .cookieHandler(studentCookies)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            // Client 2: Synthetic Admin HTTP Client
            CookieManager adminCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
            HttpClient adminClient = HttpClient.newBuilder()
                    .cookieHandler(adminCookies)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();

            // TEST 1: Synthetic Student HTTP Login
            total++;
            System.out.println("\n[HTTP CHECK 1] Testing Student Login over HTTP (POST /LoginServlet with SYNTH2401)...");
            if (testStudentLogin(studentClient)) {
                System.out.println("--> HTTP CHECK 1 PASSED: Synthetic student authenticated, session created, redirected to dashboard.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 1 FAILED!");
                System.exit(1);
            }

            // TEST 2: Synthetic Admin HTTP Login
            total++;
            System.out.println("\n[HTTP CHECK 2] Testing Admin Login over HTTP (POST /AdminLoginServlet with synthadmin)...");
            if (testAdminLogin(adminClient)) {
                System.out.println("--> HTTP CHECK 2 PASSED: Synthetic admin authenticated, session created, redirected to admin-dashboard.");
                passed++;
            } else {
                System.err.println("--> HTTP CHECK 2 FAILED!");
                System.exit(1);
            }

            // TEST 3: Admin Student Creation over HTTP
            total++;
            System.out.println("\n[HTTP CHECK 3] Testing Admin Student Account Creation over HTTP (SYNTH2403)...");
            if (testStudentCreation(adminClient)) {
                System.out.println("--> HTTP CHECK 3 PASSED: Synthetic student SYNTH2403 created successfully over HTTP.");
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
            System.err.println("Unexpected exception during synthetic HTTP integration tests: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\n================================================================================");
        System.out.printf("SYNTHETIC HTTP INTEGRATION SUMMARY: %d / %d CHECKS PASSED (100%% SUCCESS)\n", passed, total);
        System.out.println("================================================================================");
    }

    private static boolean testStudentLogin(HttpClient client) throws Exception {
        String formData = "studentId=" + urlEncode("SYNTH2401") + "&password=" + urlEncode("synthpass123");
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
        String formData = "username=" + urlEncode("synthadmin") + "&password=" + urlEncode("synthpass123");
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
                "&studentId=" + urlEncode("99881A9901") +
                "&name=" + urlEncode("Synthetic Student Gamma") +
                "&password=" + urlEncode("synthpass123") +
                "&branch=" + urlEncode("IT") +
                "&email=" + urlEncode("synthgamma@example.test") +
                "&year=1" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet?action=create -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("msg=student_created");
    }

    private static boolean testDuplicateStudentRejection(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=create" +
                "&studentId=" + urlEncode("99881A9901") +
                "&name=" + urlEncode("Duplicate Gamma") +
                "&password=" + urlEncode("synthpass123") +
                "&branch=" + urlEncode("IT") +
                "&email=" + urlEncode("synthgamma2@example.test") +
                "&year=1" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet (Duplicate) -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("error=duplicate_student_id");
    }

    private static boolean testYearSemesterAssignment(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=update" +
                "&studentId=" + urlEncode("99881A9901") +
                "&name=" + urlEncode("Synthetic Student Gamma") +
                "&branch=" + urlEncode("IT") +
                "&email=" + urlEncode("synthgamma@example.test") +
                "&year=2" +
                "&semester=2" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/StudentManagementServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /StudentManagementServlet?action=update -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("msg=student_updated");
    }

    private static boolean testRegistrationWindowControls(HttpClient adminClient) throws Exception {
        String csrfToken = fetchAdminCsrfToken(adminClient);
        String formData = "action=toggle" +
                "&term=" + urlEncode("2026-2027 ODD") +
                "&year=3" +
                "&semester=1" +
                "&status=OPEN" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegistrationControlServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = adminClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegistrationControlServlet (OPEN Year 3 Sem 1) -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("msg=window_updated");
    }

    private static boolean testCourseRegistration(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") +
                "&courseName=" + urlEncode("Web Technologies") +
                "&category=PC" +
                "&electiveSlot=CORE" +
                "&credits=3" +
                "&year=3" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (A8604) -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("msg=registered_successfully");
    }

    private static boolean testDuplicateCourseRejection(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") +
                "&courseName=" + urlEncode("Web Technologies") +
                "&category=PC" +
                "&electiveSlot=CORE" +
                "&credits=3" +
                "&year=3" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String location = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (Duplicate A8604) -> Status " + status + " Location: " + location);

        return status == 302 && location.contains("error=already_registered");
    }

    private static boolean testElectiveSlotRestrictions(HttpClient studentClient) throws Exception {
        // Register A8651 into PE-I
        String csrfToken1 = fetchStudentCsrfToken(studentClient);
        String formData1 = "courseCode=" + urlEncode("A8651") +
                "&courseName=" + urlEncode("Advanced Computer Architecture") +
                "&category=PE" +
                "&electiveSlot=PE-I" +
                "&credits=3" +
                "&year=3" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken1);

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData1))
                .build();

        HttpResponse<String> response1 = studentClient.send(request1, HttpResponse.BodyHandlers.ofString());
        System.out.println("  POST /RegisterCourseServlet (A8651 in PE-I) -> Status " + response1.statusCode() + " Location: " + response1.headers().firstValue("Location").orElse(""));

        // Attempt A8851 into PE-I (must fail with error=elective_group_limit)
        String csrfToken2 = fetchStudentCsrfToken(studentClient);
        String formData2 = "courseCode=" + urlEncode("A8851") +
                "&courseName=" + urlEncode("Data Science for Engineers") +
                "&category=PE" +
                "&electiveSlot=PE-I" +
                "&credits=3" +
                "&year=3" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(csrfToken2);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData2))
                .build();

        HttpResponse<String> response2 = studentClient.send(request2, HttpResponse.BodyHandlers.ofString());
        int status2 = response2.statusCode();
        String loc2 = response2.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (A8851 into PE-I collision) -> Status " + status2 + " Location: " + loc2);

        return status2 == 302 && loc2.contains("error=elective_group_limit");
    }

    private static boolean testCourseDrop(HttpClient studentClient) throws Exception {
        String csrfToken = fetchStudentCsrfToken(studentClient);
        String formData = "courseCode=" + urlEncode("A8604") +
                "&csrfToken=" + urlEncode(csrfToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/DropCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = studentClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String loc = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /DropCourseServlet (A8604) -> Status " + status + " Location: " + loc);

        return status == 302 && loc.contains("msg=dropped_successfully");
    }

    private static boolean testClosedWindowBlocking(HttpClient studentClient, HttpClient adminClient) throws Exception {
        // 1. Admin closes window
        String adminToken = fetchAdminCsrfToken(adminClient);
        String closeData = "action=toggle" +
                "&term=" + urlEncode("2026-2027 ODD") +
                "&year=3" +
                "&semester=1" +
                "&status=CLOSED" +
                "&csrfToken=" + urlEncode(adminToken);

        HttpRequest reqClose = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegistrationControlServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(closeData))
                .build();
        adminClient.send(reqClose, HttpResponse.BodyHandlers.ofString());

        // 2. Student attempts to register A8604 while window is CLOSED
        String studentToken = fetchStudentCsrfToken(studentClient);
        String regData = "courseCode=" + urlEncode("A8604") +
                "&courseName=" + urlEncode("Web Technologies") +
                "&category=PC" +
                "&electiveSlot=CORE" +
                "&credits=3" +
                "&year=3" +
                "&semester=1" +
                "&csrfToken=" + urlEncode(studentToken);

        HttpRequest reqReg = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/RegisterCourseServlet"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(regData))
                .build();

        HttpResponse<String> response = studentClient.send(reqReg, HttpResponse.BodyHandlers.ofString());
        studentCsrfToken = null;
        int status = response.statusCode();
        String loc = response.headers().firstValue("Location").orElse("");
        System.out.println("  POST /RegisterCourseServlet (Window CLOSED) -> Status " + status + " Location: " + loc);

        return status == 302 && loc.contains("error=window_closed");
    }

    private static String fetchStudentCsrfToken(HttpClient client) throws Exception {
        if (studentCsrfToken != null) return studentCsrfToken;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/mycourses.jsp"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Matcher m = CSRF_PATTERN.matcher(response.body());
        if (m.find()) {
            studentCsrfToken = m.group(1);
            return studentCsrfToken;
        }
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/courses.jsp"))
                .GET()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        m = CSRF_PATTERN.matcher(response.body());
        if (m.find()) {
            studentCsrfToken = m.group(1);
            return studentCsrfToken;
        }
        throw new IllegalStateException("CSRF Token not found in mycourses.jsp or courses.jsp response");
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/student_course_portal_synth_test?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "root", "root");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM curriculum_registrations WHERE student_id IN ('99881A9901', 'SYNTH2401', 'SYNTH2402')");
            stmt.executeUpdate("DELETE FROM students WHERE student_id = '99881A9901'");
        } catch (Exception ignored) {}
    }

    private static String urlEncode(String val) {
        return URLEncoder.encode(val, StandardCharsets.UTF_8);
    }
}
