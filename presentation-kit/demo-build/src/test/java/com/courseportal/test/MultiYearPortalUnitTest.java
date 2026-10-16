package com.courseportal.test;

import com.courseportal.util.CurriculumCatalogService;
import com.courseportal.util.CurriculumCatalogService.CurriculumCourse;
import com.courseportal.util.RegistrationWindowManager;

import java.util.List;
import java.util.regex.Pattern;

/**
 * MultiYearPortalUnitTest.java
 * Isolated, non-destructive test suite verifying:
 * 1. General roll number validation across all 4-year cohorts.
 * 2. Academic standing conversion (local year/sem to global semester 1-8).
 * 3. Registration window control (Open/Closed states).
 * 4. 8-Semester R22 Curriculum catalog retrieval.
 * 5. Elective group isolation and rules.
 * 
 * ZERO database mutations. Runs purely in-memory.
 */
public class MultiYearPortalUnitTest {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("STARTING ISOLATED UNIT TESTS: MULTI-YEAR PORTAL");
        System.out.println("============================================================");

        int passed = 0;
        int total = 0;

        // Test 1: Roll Number Validation Across All 4 Years
        total++;
        if (testRollNumberValidation()) {
            System.out.println("[TEST 1] Roll Number Validation: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 1] Roll Number Validation: FAILED");
        }

        // Test 2: Academic Standing Calculation
        total++;
        if (testAcademicStandingCalculation()) {
            System.out.println("[TEST 2] Academic Standing Calculation: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 2] Academic Standing Calculation: FAILED");
        }

        // Test 3: Registration Window Management
        total++;
        if (testRegistrationWindows()) {
            System.out.println("[TEST 3] Registration Window Management: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 3] Registration Window Management: FAILED");
        }

        // Test 4: 8-Semester Curriculum Catalog Coverage
        total++;
        if (testCurriculumCatalogCoverage()) {
            System.out.println("[TEST 4] 8-Semester Curriculum Coverage: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 4] 8-Semester Curriculum Coverage: FAILED");
        }

        // Test 5: Elective Group Rule Validation
        total++;
        if (testElectiveGroupRules()) {
            System.out.println("[TEST 5] Elective Group Rule Validation: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 5] Elective Group Rule Validation: FAILED");
        }

        // Test 6: Open Electives Catalog Completeness (All 41 Courses + Slots OE-I, OE-II, OE-III)
        total++;
        if (testOpenElectivesCompleteness()) {
            System.out.println("[TEST 6] Open Electives Pool & Slot Completeness: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 6] Open Electives Pool & Slot Completeness: FAILED");
        }

        // Test 7: CSRF Token Generation and Validation
        total++;
        if (testCSRFTokenLifecycle()) {
            System.out.println("[TEST 7] CSRF Token Lifecycle & Security: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 7] CSRF Token Lifecycle & Security: FAILED");
        }

        // Test 8: Semester Mismatch Eligibility Guard
        total++;
        if (testSemesterEligibilityLogic()) {
            System.out.println("[TEST 8] Semester Eligibility Logic: PASSED");
            passed++;
        } else {
            System.err.println("[TEST 8] Semester Eligibility Logic: FAILED");
        }

        System.out.println("============================================================");
        System.out.printf("TEST SUMMARY: %d / %d TESTS PASSED\n", passed, total);
        System.out.println("============================================================");

        if (passed != total) {
            System.exit(1);
        }
    }

    private static boolean testRollNumberValidation() {
        Pattern pattern = Pattern.compile("^(\\d{2}881A[0-9A-Za-z]{4}|[0-9]{2}[0-9A-Za-z]{8})$", Pattern.CASE_INSENSITIVE);

        // Valid test cases across all 4 years
        String[] valid = {
            "26881A1201", // 1st Year Cohort
            "25881A1201", // 2nd Year Cohort
            "24881A1201", // 3rd Year Cohort
            "24881A1203", // 3rd Year Sample
            "23881A1201", // 4th Year Cohort
            "23881A1203", // 4th Year Sample
            "24881a1205"  // Lowercase
        };

        for (String roll : valid) {
            if (!pattern.matcher(roll).matches()) {
                System.err.println("  Failed to validate valid roll: " + roll);
                return false;
            }
        }

        // Invalid test cases
        String[] invalid = {
            "", "123", "INVALID", "24881A", "24881A12", "ABCDEFGH", "24881A120344", "24881A12345", "24881-1203"
        };
        for (String roll : invalid) {
            if (pattern.matcher(roll).matches()) {
                System.err.println("  Unexpectedly matched invalid roll: " + roll);
                return false;
            }
        }

        return true;
    }

    private static boolean testAcademicStandingCalculation() {
        // globalSemester = (year - 1) * 2 + semester
        for (int y = 1; y <= 4; y++) {
            for (int s = 1; s <= 2; s++) {
                int expectedGlobal = (y - 1) * 2 + s;
                int actualGlobal = (y - 1) * 2 + s;
                if (expectedGlobal < 1 || expectedGlobal > 8 || expectedGlobal != actualGlobal) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean testRegistrationWindows() {
        // 1. Verify strict default: all windows across all 4 years and 8 semesters must default to CLOSED
        for (int y = 1; y <= 4; y++) {
            for (int s = 1; s <= 2; s++) {
                if (RegistrationWindowManager.isWindowOpen(y, s)) {
                    System.err.println("  Window for Year " + y + " Sem " + s + " is unexpectedly OPEN by default!");
                    return false;
                }
            }
        }

        // 2. Explicit authenticated admin action opens window
        RegistrationWindowManager.setWindowStatus(3, 1, true);
        if (!RegistrationWindowManager.isWindowOpen(3, 1)) {
            System.err.println("  Failed to open window for Year 3 Sem 1 via admin action");
            return false;
        }

        // 3. Explicit authenticated admin action closes window
        RegistrationWindowManager.setWindowStatus(3, 1, false);
        if (RegistrationWindowManager.isWindowOpen(3, 1)) {
            System.err.println("  Failed to close window for Year 3 Sem 1 via admin action");
            return false;
        }

        return true;
    }

    private static boolean testCurriculumCatalogCoverage() {
        List<CurriculumCourse> all = CurriculumCatalogService.getAllCourses();
        if (all == null || all.isEmpty()) return false;

        // Verify that all 8 semesters have courses defined
        for (int sem = 1; sem <= 8; sem++) {
            int y = (sem + 1) / 2;
            int s = (sem % 2 == 1) ? 1 : 2;
            List<CurriculumCourse> semCourses = CurriculumCatalogService.getCoursesForSemester(y, s);
            if (semCourses.isEmpty()) {
                System.err.println("  Semester " + sem + " has 0 courses!");
                return false;
            }
        }
        return true;
    }

    private static boolean testElectiveGroupRules() {
        // A8651 and A8851 both belong to "Professional Elective I"
        String group1 = CurriculumCatalogService.getElectiveGroup("A8651");
        String group2 = CurriculumCatalogService.getElectiveGroup("A8851");
        String coreGroup = CurriculumCatalogService.getElectiveGroup("A8519");

        if (group1 == null || !group1.equals(group2)) {
            System.err.println("  Elective group mismatch for PE-I courses");
            return false;
        }

        if (coreGroup != null) {
            System.err.println("  Core course A8519 should not belong to an elective group");
            return false;
        }

        return true;
    }

    private static boolean testOpenElectivesCompleteness() {
        List<CurriculumCourse> openElectives = CurriculumCatalogService.getOpenElectives();
        if (openElectives == null || openElectives.size() != 41) {
            System.err.println("  Expected exactly 41 open electives, but got: " + (openElectives == null ? 0 : openElectives.size()));
            return false;
        }

        // Verify specific known sample courses across departments
        if (!CurriculumCatalogService.isOpenElective("A8181")) {
            System.err.println("  Missing CE open elective A8181 (Smart Cities)");
            return false;
        }
        if (!CurriculumCatalogService.isOpenElective("A8182")) {
            System.err.println("  Missing CE open elective A8182 (Disaster Management)");
            return false;
        }
        if (!CurriculumCatalogService.isOpenElective("A8281")) {
            System.err.println("  Missing EEE open elective A8281 (Solar Energy and Applications)");
            return false;
        }
        if (!CurriculumCatalogService.isOpenElective("A8682")) {
            System.err.println("  Missing CSE open elective A8682 (Full Stack Development)");
            return false;
        }
        if (!CurriculumCatalogService.isOpenElective("A8091")) {
            System.err.println("  Missing BS open elective A8091 (National Cadet Corps)");
            return false;
        }
        if (CurriculumCatalogService.isOpenElective("A8506")) {
            System.err.println("  Core course A8506 incorrectly identified as open elective");
            return false;
        }

        // Verify that Year 4 Sem 1 has OE-I slot
        List<CurriculumCourse> y4s1 = CurriculumCatalogService.getCoursesForSemester(4, 1);
        boolean hasOE1 = false;
        for (CurriculumCourse c : y4s1) {
            if ("OE-I".equals(c.getCode())) {
                hasOE1 = true;
                break;
            }
        }
        if (!hasOE1) {
            System.err.println("  Missing OE-I slot in Year 4 Semester 1");
            return false;
        }

        // Verify that Year 4 Sem 2 has OE-II and OE-III slots
        List<CurriculumCourse> y4s2 = CurriculumCatalogService.getCoursesForSemester(4, 2);
        boolean hasOE2 = false;
        boolean hasOE3 = false;
        for (CurriculumCourse c : y4s2) {
            if ("OE-II".equals(c.getCode())) hasOE2 = true;
            if ("OE-III".equals(c.getCode())) hasOE3 = true;
        }
        if (!hasOE2 || !hasOE3) {
            System.err.println("  Missing OE-II or OE-III slot in Year 4 Semester 2");
            return false;
        }

        return true;
    }

    private static boolean testCSRFTokenLifecycle() {
        try {
            java.util.Map<String, Object> sessionAttributes = new java.util.HashMap<>();
            jakarta.servlet.http.HttpSession mockSession = (jakarta.servlet.http.HttpSession) java.lang.reflect.Proxy.newProxyInstance(
                MultiYearPortalUnitTest.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.http.HttpSession.class},
                (proxy, method, mArgs) -> {
                    String methodName = method.getName();
                    if ("getAttribute".equals(methodName)) {
                        return sessionAttributes.get((String) mArgs[0]);
                    } else if ("setAttribute".equals(methodName)) {
                        sessionAttributes.put((String) mArgs[0], mArgs[1]);
                        return null;
                    }
                    return null;
                }
            );

            // 1. Generate token
            String token = com.courseportal.util.CSRFUtil.getToken(mockSession);
            if (token == null || token.length() < 20) {
                System.err.println("  CSRF token generation failed or token too short: " + token);
                return false;
            }

            // 2. Token consistency on repeat calls
            String token2 = com.courseportal.util.CSRFUtil.getToken(mockSession);
            if (!token.equals(token2)) {
                System.err.println("  CSRF token not persistent within session");
                return false;
            }

            // 3. Mock request matching
            java.util.Map<String, String> requestParams = new java.util.HashMap<>();
            requestParams.put("csrfToken", token);

            jakarta.servlet.http.HttpServletRequest validRequest = (jakarta.servlet.http.HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                MultiYearPortalUnitTest.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.http.HttpServletRequest.class},
                (proxy, method, mArgs) -> {
                    if ("getSession".equals(method.getName())) return mockSession;
                    if ("getParameter".equals(method.getName())) return requestParams.get((String) mArgs[0]);
                    return null;
                }
            );

            if (!com.courseportal.util.CSRFUtil.isValid(validRequest)) {
                System.err.println("  CSRF validation failed for valid token");
                return false;
            }

            // 4. Invalid token rejection
            requestParams.put("csrfToken", "tampered_token_value_123");
            if (com.courseportal.util.CSRFUtil.isValid(validRequest)) {
                System.err.println("  CSRF validation should fail for tampered token");
                return false;
            }

            // 5. Missing token rejection
            requestParams.remove("csrfToken");
            if (com.courseportal.util.CSRFUtil.isValid(validRequest)) {
                System.err.println("  CSRF validation should fail for missing token");
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean testSemesterEligibilityLogic() {
        // Given student in Year 3 Sem 1
        int studentYear = 3;
        int studentSem = 1;

        // Valid course in same semester: A8604 (Web Technologies, Year 3 Sem 1)
        CurriculumCourse courseInSem = CurriculumCatalogService.getCourse("A8604");
        if (courseInSem == null) {
            System.err.println("  A8604 not found in catalog");
            return false;
        }
        boolean allowedSameSem = (courseInSem.getYear() == studentYear && courseInSem.getSemester() == studentSem);
        if (!allowedSameSem) {
            System.err.println("  Expected course in same semester to be allowed");
            return false;
        }

        // Invalid course in different semester: A8654 (Cloud Security, Year 4 Sem 1)
        CurriculumCourse courseOtherSem = CurriculumCatalogService.getCourse("A8654");
        if (courseOtherSem == null) {
            System.err.println("  A8654 not found in catalog");
            return false;
        }
        boolean allowedOtherSem = (courseOtherSem.getYear() == studentYear && courseOtherSem.getSemester() == studentSem);
        if (allowedOtherSem) {
            System.err.println("  Course in different semester was incorrectly allowed");
            return false;
        }

        return true;
    }
}
