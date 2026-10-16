# IMPLEMENTATION AUDIT REPORT
## Student Course Registration Portal | Full 4-Year / 8-Semester System

**Audit Date:** 2026-10-13  
**Project Directory:** `C:\Users\anike\OneDrive\Desktop\clg html\course end`  
**Application Stack:** Java Servlets (Jakarta EE 10 / Servlet 6.0), JSP 3.1, Apache Tomcat 10.1.60, MySQL 8.0.46, Maven 3.9, Vanilla HTML/CSS/JS  
**Curriculum Authority:** `VCE_BTech_IT_R22_Curriculum.pdf` (Vardhaman College of Engineering, Autonomous R22)

---

## 1. Executive Summary

This audit establishes the authenticated, empirical baseline of the existing Student Course Registration Portal prior to applying any multi-year / 8-semester features. In accordance with the non-negotiable safety rules:
1. **Zero live database modifications** have been performed.
2. The live MySQL database was inspected strictly using read-only metadata and `SELECT` queries.
3. The live database contains **5 student accounts, 5 prototype course records, 8 registration records, and 1 admin account**.
4. The application currently functions on the baseline natural-key schema, but has specific limitations:
   - Roll number validation regex in JavaScript is hardcoded to `/^24881A\d{4}$/i` (rejecting any student from other admission years such as 26, 25, or 23).
   - The UI does not support filtering by semester or academic year.
   - Admin cannot create or edit student accounts, assign years/semesters, or open/close registration windows.
   - Significant naming and credit conflicts exist between the 5 prototype courses and the official R22 curriculum.

---

## 2. Live Database Read-Only Inspection

Executed via `ReadOnlyDatabaseAudit.java` connecting to `jdbc:mysql://localhost:3306/student_course_portal`:

### 2.1 Table: `students` (Count: 5)
| Roll Number | Name | Branch | Year | Semester | Email | Password |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `24881A1201` | Abhignaya | IT | 3 | 1 | `24881a1201@student.vardhaman.org` | `student123` |
| `24881A1202` | Lokesh | IT | 3 | 1 | `24881a1202@student.vardhaman.org` | `student123` |
| `24881A1203` | Aniket | IT | 3 | 1 | `24881a1203@student.vardhaman.org` | `student123` |
| `24881A1204` | Ansar | IT | 3 | 1 | `24881a1204@student.vardhaman.org` | `student123` |
| `24881A1205` | Anushka | IT | 3 | 1 | `24881a1205@student.vardhaman.org` | `student123` |

*Finding:* All 5 students in the live database belong to the IT department with Year 3, Semester 1. The seed script `database/student_course_portal.sql` contained placeholder names ("Rahul Sharma", "Priya Patel"), which are **NOT** present in the live database. The live data represents real student accounts and must be preserved.

### 2.2 Table: `courses` (Count: 5)
| Course Code (PK) | Course Name | Faculty | Credits |
| :--- | :--- | :--- | :--- |
| `A8506` | Web Technologies | Ms. B. Swapna | 3 |
| `A8519` | Computer Networks | Ms. Sumaiya Shaikh | 1 |
| `A8520` | Software Engineering | Dr. Ravi Kumar | 2 |
| `A8704` | Machine Learning | Dr. B. K. Madhavi | 3 |
| `A8851` | Data Science | Dr. Sreenivasulu Gogula | 4 |

### 2.3 Table: `registrations` (Count: 8)
| ID (PK) | Student ID | Course Code | Registered At (Timestamp) |
| :--- | :--- | :--- | :--- |
| 4 | `24881A1203` | `A8519` | 2026-10-12 10:48:54 |
| 5 | `24881A1201` | `A8506` | 2026-10-12 11:26:43 |
| 6 | `24881A1201` | `A8519` | 2026-10-12 11:26:45 |
| 7 | `24881A1201` | `A8520` | 2026-10-12 11:26:46 |
| 8 | `24881A1201` | `A8704` | 2026-10-12 11:26:47 |
| 9 | `24881A1201` | `A8851` | 2026-10-12 11:26:49 |
| 10 | `24881A1203` | `A8520` | 2026-10-12 12:23:07 |
| 11 | `24881A1203` | `A8704` | 2026-10-12 12:23:08 |

### 2.4 Table: `admin` (Count: 1)
| Username (PK) | Password |
| :--- | :--- |
| `admin` | `admin123` |

---

## 3. Discrepancy & Conflict Analysis

### 3.1 Course Code Conflicts with Official R22 Curriculum
Comparing the 5 live prototype records with `VCE_BTech_IT_R22_Curriculum.pdf`:

| Code | Prototype Identity (Live DB) | Official R22 Identity (PDF) | Conflict Nature |
| :--- | :--- | :--- | :--- |
| **A8506** | Web Technologies (3 cr, Year 3 Sem 1) | Computer Organization (2 cr, Year 1 Sem 2) | **Severe identity clash**: Prototype used `A8506` for Web Tech, but official Web Tech is `A8604` (Sem 5, 3 cr). In R22, `A8506` is a 1st year computer organization subject. |
| **A8519** | Computer Networks (1 cr) | Computer Networks (3 cr, Year 3 Sem 1) | Credit mismatch (1 cr vs official 3 cr). |
| **A8520** | Software Engineering (2 cr) | Software Engineering (3 cr, Year 3 Sem 1) | Credit mismatch (2 cr vs official 3 cr). |
| **A8704** | Machine Learning (3 cr) | Machine Learning Laboratory (1 cr, Year 3 Sem 1) | Type & Credit mismatch (Prototype treats as 3 cr theory; official R22 theory is `A8703` 3 cr, and `A8704` is the 1 cr lab). |
| **A8851** | Data Science (4 cr) | Data Science for Engineers (3 cr, PE-I, Sem 5) | Name & Credit mismatch (Prototype 4 cr vs official 3 cr; it is an elective in R22). |

### 3.2 Non-Destructive Reconciliation Strategy
Because the 8 historical registrations link to `A8506`, `A8519`, `A8520`, `A8704`, and `A8851`, **we must NOT overwrite or delete these records**. Doing so would invalidate historical student enrollments.
**Proposed Architecture:**
1. Keep the existing `courses` table intact as the legacy/active course catalog.
2. In the multi-semester expansion, introduce an official catalog table (`r22_curriculum_catalog`) and an offerings/term table (`course_offerings`) or add non-destructive nullable columns (`year`, `semester`, `academic_term`, `elective_group`) to courses.
3. For `A8506`, preserve the historical record's meaning while enabling official R22 catalog courses to be distinguished safely.

### 3.3 Roll Number & Year Assumptions
1. **Client-side Regex:** [validation.js](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/webapp/js/validation.js) currently enforces:
   ```javascript
   var studentIdPattern = /^24881A\d{4}$/i;
   ```
   This rejects any student roll number from 1st Year (`26881A...`), 2nd Year (`25881A...`), or 4th Year (`23881A...`), or lateral entry (`25881A...`).
   **Fix:** Expand the regex to match institution format `/^\d{2}881A[0-9A-Z]{4}$/i` or general college format `/^[0-9]{2}[0-9A-Za-z]{8}$/i`.
2. **Server-side Assumption:** The existing servlets store `year` and `semester` directly in the session, but no logic checked course eligibility based on the student's assigned academic standing.

---

## 4. Current Application Control Flow Audit

1. **Authentication:**
   - Student: [LoginServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/LoginServlet.java) queries `students` using `PreparedStatement`. Session attributes: `studentId`, `studentName`, `branch`, `year`, `semester`, `email`, `role="student"`.
   - Admin: [AdminLoginServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/AdminLoginServlet.java) queries `admin`. Session attribute: `adminUser`, `role="admin"`.
2. **Course Catalog & Registration:**
   - [courses.jsp](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/webapp/courses.jsp) lists all rows from `courses` unconditionally.
   - [RegisterCourseServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/RegisterCourseServlet.java) inserts into `registrations`.
3. **Course Dropping:**
   - [DropCourseServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/DropCourseServlet.java) deletes from `registrations` scoped by student ID.
4. **Admin Management:**
   - [AddCourseServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/AddCourseServlet.java), [UpdateCourseServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/UpdateCourseServlet.java), [DeleteCourseServlet.java](file:///c:/Users/anike/OneDrive/Desktop/clg%20html/course%20end/src/main/java/com/courseportal/servlet/DeleteCourseServlet.java).
   - Currently, there is **no student management interface** (Admin cannot create or edit student accounts) and **no registration window control**.

---

## 5. Required Implementation Scope

### Phase 1: Application Upgrades (Safe & Fully Compatible)
1. **Roll Number Validation:** Support 4-year cohorts in `validation.js` and `LoginServlet.java`.
2. **Admin Student Management:**
   - Add `StudentManagementServlet.java` (Create student, edit student, update academic year/semester, toggle active status).
   - Add Student Management view tab in `admin-dashboard.jsp`.
3. **Registration Window Control:**
   - Add `RegistrationControlServlet.java` (Open/Close registration for specific semesters or terms).
   - Display Registration status banner on student dashboard and courses page.
4. **Semester-Aware Course Filtering:**
   - Filter available courses on `courses.jsp` so students register for courses matching their assigned year and semester.
   - Allow read-only exploration of other semester catalogs without registration bypass.
5. **Security & Validation:**
   - CSRF tokens for state-changing forms.
   - Server-side semester eligibility check before registration.

### Phase 2: Proposed Schema Changes (Prepared for Review, Zero Live Execution)
- New tables: `registration_windows`, `r22_curriculum_catalog`, `elective_groups`.
- Safe backward compatibility fallback in application code: if new tables do not exist in the database, servlets fall back cleanly to default open window / core catalog.
