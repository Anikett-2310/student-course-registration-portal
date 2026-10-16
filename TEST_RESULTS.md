# TEST RESULTS REPORT
## Multi-Year / 8-Semester Student Course Registration Portal

**Date:** 2026-10-13  
**Environment:** JDK 21 (`C:\Program Files\Java\jdk-21.0.12.1`), Maven 3.9, MySQL 8.0.46, Tomcat 10.1.60  
**Test Suite:** Isolated Non-Destructive Unit Tests & Read-Only Audits  

---

## 1. Test Summary

| Test Suite / Step | Category | Target Component | Execution Environment | Status | Data Mutation |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Roll Number Validation** | Unit Test | `MultiYearPortalUnitTest` (Test 1) | In-Memory Regex (`Pattern`) | **PASSED** | ZERO |
| **Academic Standing Calculation** | Unit Test | `MultiYearPortalUnitTest` (Test 2) | In-Memory `(year - 1) * 2 + sem` | **PASSED** | ZERO |
| **Registration Window State Control** | Unit Test | `MultiYearPortalUnitTest` (Test 3) | `RegistrationWindowManager` (Default CLOSED) | **PASSED** | ZERO |
| **8-Semester Curriculum Coverage** | Unit Test | `MultiYearPortalUnitTest` (Test 4) | `CurriculumCatalogService` | **PASSED** | ZERO |
| **Elective Group Rule Validation** | Unit Test | `MultiYearPortalUnitTest` (Test 5) | `CurriculumCatalogService` PE grouping | **PASSED** | ZERO |
| **Open Electives Catalog Completeness** | Unit Test | `MultiYearPortalUnitTest` (Test 6) | 41 Electives Pool + Slots OE-I, II, III | **PASSED** | ZERO |
| **CSRF Token Lifecycle & Security** | Unit Test | `MultiYearPortalUnitTest` (Test 7) | `CSRFUtil` Token Generation & Rejection | **PASSED** | ZERO |
| **Semester Eligibility Logic** | Unit Test | `MultiYearPortalUnitTest` (Test 8) | Semester Mismatch Guard Simulation | **PASSED** | ZERO |
| **Maven Full Compilation** | Build Test | `mvn test-compile` | JDK 21 Compiler (14 source + 8 test files) | **PASSED** | ZERO |
| **Maven Package Build** | Build Test | `mvn package -DskipTests` | Packaged `target/student-course-registration.war` | **PASSED** | ZERO (Tomcat webapps untouched) |
| **Live Database Read-Only Audit** | DB Audit | `ReadOnlyDatabaseAudit.java` | Live MySQL (`student_course_portal`) | **PASSED** | ZERO (Read-only `SELECT` & metadata) |
| **Disposable DB Integration Suite** | Integration | `IsolatedTestDatabaseRunner.java` | Disposable MySQL (`student_course_portal_test`) | **PASSED (18/18)** | ZERO to live DB (isolated test DB only) |
| **Live Database Backup Restore Test** | Disaster Recovery | `student_course_portal_restore_test` | Disposable MySQL (`student_course_portal_restore_test`) | **PASSED** | ZERO to live DB (isolated restore DB only) |
| **Legacy Destructive Integration Tests** | Integration | `CourseRegistrationStageTest`, etc. | Live MySQL | **BLOCKED** | Prevented unisolated `DELETE` calls |

---

## 2. Detailed Test Verification Results

### 2.1 Live Database Read-Only Audit (`ReadOnlyDatabaseAudit`)
- Verified that connection to `jdbc:mysql://localhost:3306/student_course_portal` succeeds.
- Verified exact counts:
  - `students`: 5 rows (`24881A1201` - `24881A1205`, all IT, Year 3, Sem 1)
  - `courses`: 5 rows (`A8506`, `A8519`, `A8520`, `A8704`, `A8851`)
  - `registrations`: 8 rows (IDs 4, 5, 6, 7, 8, 9, 10, 11 with exact 2026-10-12 timestamps)
  - `admin`: 1 row (`username=admin`, password field preserved, masked in report as `[REDACTED]`)
- Output confirmed zero modifications were made to any tables.

### 2.2 Roll Number Multi-Year Cohort Validation
- **Regex:** `^(\d{2}881A[0-9A-Za-z]{4}|[0-9]{2}[0-9A-Za-z]{8})$` (Case Insensitive)
- Verified valid admission cohorts:
  - 1st Year: `26881A1201` -> MATCH
  - 2nd Year: `25881A1201` -> MATCH
  - 3rd Year: `24881A1201`, `24881A1203` -> MATCH
  - 4th Year: `23881A1203` -> MATCH
  - Lowercase entry: `24881a1205` -> MATCH
- Verified invalid formats rejected:
  - `""` (Empty), `"123"`, `"ABCDEFGH"`, `"24881A12"`, `"24881-1203"` -> REJECTED

### 2.3 Academic Standing Mapping (Local vs. Global Semester)
- Formula tested: `globalSemester = (year - 1) * 2 + semester`
- Year 1, Sem 1 -> Sem 1 (Odd)
- Year 1, Sem 2 -> Sem 2 (Even)
- Year 2, Sem 1 -> Sem 3 (Odd)
- Year 2, Sem 2 -> Sem 4 (Even)
- Year 3, Sem 1 -> Sem 5 (Odd)
- Year 3, Sem 2 -> Sem 6 (Even)
- Year 4, Sem 1 -> Sem 7 (Odd)
- Year 4, Sem 2 -> Sem 8 (Even)
- All 8 semester mappings verified.

### 2.4 Registration Window Control
- Verified that `RegistrationWindowManager` accurately reports OPEN/CLOSED states.
- Verified that toggling Year 3 Sem 1 status to CLOSED immediately blocks enrollment checks in `RegisterCourseServlet`.
- Verified that toggling status to OPEN permits valid enrollments.

### 2.5 Curriculum Catalog & Elective Grouping
- Verified all 8 semesters have course definitions populated in `CurriculumCatalogService`.
- Verified that elective courses `A8651` (Ethical Hacking) and `A8851` (Data Science for Engineers) both share elective group `"Professional Elective I"`.
- Verified that core course `A8519` (Computer Networks) has `null` elective group.

### 2.6 Disposable Database Integration Test Suite (`IsolatedTestDatabaseRunner`)
- Executed against separate disposable MySQL database `student_course_portal_test`.
- **Check 1 (Isolation Guard):** Verified connection catalog isolation; production database strictly safeguarded. (PASSED)
- **Check 2 (Disposable DB Creation):** Clean `student_course_portal_test` created with baseline prototype tables and synthetic test accounts. (PASSED)
- **Check 3 (Schema Migration):** All 4 proposed tables (`registration_windows`, `r22_curriculum_catalog`, `r22_open_electives`, `curriculum_registrations`) created cleanly. (PASSED)
- **Check 4 (Seed Script Execution):** `PROPOSED_R22_CURRICULUM_SEED.sql` executed with zero syntax or constraint errors. (PASSED)
- **Check 5 (Catalog Verification):** Exactly 41 open electives and 92 catalog courses across all 8 semesters verified. Slots `OE-I`, `OE-II`, and `OE-III` verified. (PASSED)
- **Check 6 (Seed Idempotency & Safe Re-run):** Re-executed seed script a second time against populated database with simulated pre-existing catalog updates; verified 0 errors, 0 duplicate rows, and legitimate catalog modifications preserved. (PASSED)
- **Check 7 (Default Window State):** All 8 semester registration windows verified defaulting strictly to `CLOSED`. (PASSED)
- **Check 8 (Admin Window Toggle):** Authenticated admin OPEN and CLOSE state transitions verified per academic term, year, and semester. (PASSED)
- **Check 9 (Admin Login Authentication):** Admin credentials verified against test database; invalid password blocked; non-existent username blocked. (PASSED)
- **Check 10 (Student Account Creation):** Admin student creation verified; duplicate student ID rejection (`1062`) verified. (PASSED)
- **Check 11 (Student Year/Semester Assignment):** Assigning Year and Semester verified; automatic academic standing calculation (`global_semester = (year - 1) * 2 + sem`) verified. (PASSED)
- **Check 12 (Student Login Authentication):** Synthetic student login verified against test database; invalid password blocked; non-existent roll number blocked. (PASSED)
- **Check 13 (Course Registration & Window Guard):** Core course `A8604` registered; closed registration window blocks registration attempt. (PASSED)
- **Check 14 (Duplicate Course Registration Prevention):** Duplicate course registration attempt strictly rejected by unique constraint `uk_student_course_term`. (PASSED)
- **Check 15 (Elective Slot Constraints):** Elective collision within slot `PE-I` rejected by `uk_student_slot_term`; distinct `OE-II` (`A8181`) and `OE-III` (`A8281`) in Semester 8 allowed and tracked. (PASSED)
- **Check 16 (Course Drop Functionality):** Student course drop executed and confirmed row removal. (PASSED)
- **Check 17 (Restart Persistence):** Registration window states and curriculum registrations persist across connection close and re-open. (PASSED)
- **Check 18 (Production Database Audit):** Live database `student_course_portal` re-audited and confirmed 100% untouched (5 students, 5 courses, 8 registrations, 1 admin, 0 test tables). (PASSED)

---

## 3. Tests NOT Executed (Guaranteed Safety Boundary)
1. **Live Database Mutations:** Zero `INSERT`, `UPDATE`, or `DELETE` tests were executed against the live MySQL database.
2. **Pre-Existing Destructive Integration Tests:** Legacy test files `CourseRegistrationStageTest` and `DropCourseStageTest` were NOT executed against live data because they contain unisolated `DELETE FROM registrations` calls.
3. **Tomcat Live Deployment:** No new WAR was overwritten onto Tomcat `webapps/`, preserving the currently running baseline application until user approval.
