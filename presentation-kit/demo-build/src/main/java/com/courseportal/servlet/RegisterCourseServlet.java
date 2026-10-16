package com.courseportal.servlet;

import com.courseportal.util.CSRFUtil;
import com.courseportal.util.CurriculumCatalogService;
import com.courseportal.util.DBConnection;
import com.courseportal.util.RegistrationWindowManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RegisterCourseServlet.java
 * Handles course enrollment with comprehensive multi-year & 8-semester business rules:
 * 1. Verifies student authentication and session authorization.
 * 2. Enforces CSRF token validation.
 * 3. Enforces registration window state (Open / Closed) for student's assigned semester.
 * 4. Enforces academic semester eligibility (student can only register for courses in their assigned semester).
 * 5. Enforces elective group limits (only 1 choice allowed per elective group).
 * 6. Checks duplicate registration.
 * 7. Uses PreparedStatement to insert registration record.
 */
@WebServlet("/RegisterCourseServlet")
public class RegisterCourseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Session verification
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("index.html?error=session_expired");
            return;
        }

        // 2. CSRF Guard
        if (!CSRFUtil.isValid(request)) {
            response.sendRedirect("courses.jsp?error=invalid_csrf");
            return;
        }

        String studentId = (String) session.getAttribute("studentId");
        Integer studentYear = (Integer) session.getAttribute("year");
        Integer studentSem = (Integer) session.getAttribute("semester");

        // Fallback query if year/semester missing from session
        if (studentYear == null || studentSem == null) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT year, semester FROM students WHERE student_id = ?")) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentYear = rs.getInt("year");
                        studentSem = rs.getInt("semester");
                        session.setAttribute("year", studentYear);
                        session.setAttribute("semester", studentSem);
                    } else {
                        studentYear = 3;
                        studentSem = 1;
                    }
                }
            } catch (SQLException e) {
                studentYear = 3;
                studentSem = 1;
            }
        }

        // 3. Registration Window Guard
        if (!RegistrationWindowManager.isWindowOpen(studentYear, studentSem)) {
            response.sendRedirect("courses.jsp?error=window_closed");
            return;
        }

        // 4. Read and validate courseCode parameter
        String courseCode = request.getParameter("courseCode");
        if (courseCode == null || courseCode.trim().isEmpty()) {
            response.sendRedirect("courses.jsp?error=invalid_request");
            return;
        }

        courseCode = courseCode.trim().toUpperCase();

        // 5. Academic Semester Eligibility Check
        CurriculumCatalogService.CurriculumCourse curCourse = CurriculumCatalogService.getCourse(courseCode);
        if (curCourse != null) {
            boolean isLegacyExemption = (studentYear == 3 && studentSem == 1 && "A8506".equals(courseCode));
            boolean isOeSlotEligible = CurriculumCatalogService.isOpenElective(courseCode) && 
                ((studentYear == 4 && studentSem == 1) || (studentYear == 4 && studentSem == 2));

            if (!isLegacyExemption && !isOeSlotEligible &&
                (curCourse.getYear() != studentYear || curCourse.getSemester() != studentSem)) {
                response.sendRedirect("courses.jsp?error=semester_mismatch");
                return;
            }
        }

        // 6. Database operations
        try (Connection conn = DBConnection.getConnection()) {

            boolean useCurriculumReg = hasCurriculumRegistrationsTable(conn);

            if (useCurriculumReg) {
                // Determine course metadata and elective slot
                String courseName = (curCourse != null) ? curCourse.getName() : "Curriculum Course";
                String category = (curCourse != null) ? curCourse.getCategory() : "PC";
                int credits = (curCourse != null) ? curCourse.getCredits() : 3;
                String electiveSlot = null;

                if (studentYear >= 4 && CurriculumCatalogService.isOpenElective(courseCode)) {
                    if (studentYear == 4 && studentSem == 1) {
                        electiveSlot = "OE-I";
                    } else if (studentYear == 4 && studentSem == 2) {
                        String slotParam = request.getParameter("electiveSlot");
                        electiveSlot = ("OE-III".equalsIgnoreCase(slotParam)) ? "OE-III" : "OE-II";
                    } else {
                        electiveSlot = "OE-I";
                    }
                } else if (curCourse != null && curCourse.getElectiveGroup() != null) {
                    String eg = curCourse.getElectiveGroup();
                    if (eg.contains("IV")) electiveSlot = "PE-IV";
                    else if (eg.contains("VI")) electiveSlot = "PE-VI";
                    else if (eg.contains("V")) electiveSlot = "PE-V";
                    else if (eg.contains("III")) electiveSlot = "PE-III";
                    else if (eg.contains("II")) electiveSlot = "PE-II";
                    else if (eg.contains("I")) electiveSlot = "PE-I";
                    else electiveSlot = eg;
                }

                String academicTerm = "2026-2027 ODD";

                // Step 6a: Duplicate registration check in curriculum_registrations
                String dupSql = "SELECT 1 FROM curriculum_registrations WHERE student_id = ? AND course_code = ? AND academic_term = ?";
                try (PreparedStatement dupPs = conn.prepareStatement(dupSql)) {
                    dupPs.setString(1, studentId);
                    dupPs.setString(2, courseCode);
                    dupPs.setString(3, academicTerm);
                    try (ResultSet rs = dupPs.executeQuery()) {
                        if (rs.next()) {
                            response.sendRedirect("courses.jsp?error=already_registered");
                            return;
                        }
                    }
                }

                // Step 6b: Elective slot collision check (cannot pick 2 courses for same slot in same term)
                if (electiveSlot != null) {
                    String slotSql = "SELECT 1 FROM curriculum_registrations WHERE student_id = ? AND elective_slot = ? AND academic_term = ?";
                    try (PreparedStatement slotPs = conn.prepareStatement(slotSql)) {
                        slotPs.setString(1, studentId);
                        slotPs.setString(2, electiveSlot);
                        slotPs.setString(3, academicTerm);
                        try (ResultSet rs = slotPs.executeQuery()) {
                            if (rs.next()) {
                                response.sendRedirect("courses.jsp?error=elective_group_limit");
                                return;
                            }
                        }
                    }
                }

                // Step 6c: Insert into curriculum_registrations
                String insertCurSql = "INSERT INTO curriculum_registrations (student_id, academic_term, year, semester, course_code, course_name, category, elective_slot, credits) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insPs = conn.prepareStatement(insertCurSql)) {
                    insPs.setString(1, studentId);
                    insPs.setString(2, academicTerm);
                    insPs.setInt(3, studentYear);
                    insPs.setInt(4, studentSem);
                    insPs.setString(5, courseCode);
                    insPs.setString(6, courseName);
                    insPs.setString(7, category);
                    insPs.setString(8, electiveSlot);
                    insPs.setInt(9, credits);
                    insPs.executeUpdate();
                }

                response.sendRedirect("courses.jsp?msg=registered_successfully");
                return;
            }

            // Fallback for legacy prototype schema:
            // Step 6d: Verify that the course exists in courses table
            String verifyCourseSql = "SELECT course_code FROM courses WHERE course_code = ?";
            try (PreparedStatement verifyPstmt = conn.prepareStatement(verifyCourseSql)) {
                verifyPstmt.setString(1, courseCode);
                try (ResultSet rs = verifyPstmt.executeQuery()) {
                    if (!rs.next()) {
                        response.sendRedirect("courses.jsp?error=course_not_found");
                        return;
                    }
                }
            }

            // Step 6e: Application-level duplicate registration check
            String checkRegSql = "SELECT 1 FROM registrations WHERE student_id = ? AND course_code = ?";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkRegSql)) {
                checkPstmt.setString(1, studentId);
                checkPstmt.setString(2, courseCode);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next()) {
                        response.sendRedirect("courses.jsp?error=already_registered");
                        return;
                    }
                }
            }

            // Step 6f: Elective Group Rule Enforcement
            String electiveGroup = CurriculumCatalogService.getElectiveGroup(courseCode);
            if (electiveGroup != null && !electiveGroup.trim().isEmpty()) {
                String enrolledElectivesSql = "SELECT r.course_code FROM registrations r WHERE r.student_id = ?";
                try (PreparedStatement eeStmt = conn.prepareStatement(enrolledElectivesSql)) {
                    eeStmt.setString(1, studentId);
                    try (ResultSet rs = eeStmt.executeQuery()) {
                        while (rs.next()) {
                            String registeredCode = rs.getString("course_code");
                            String registeredGroup = CurriculumCatalogService.getElectiveGroup(registeredCode);
                            if (electiveGroup.equalsIgnoreCase(registeredGroup)) {
                                response.sendRedirect("courses.jsp?error=elective_group_limit");
                                return;
                            }
                        }
                    }
                }
            }

            // Step 6g: Insert legacy registration record
            String insertSql = "INSERT INTO registrations (student_id, course_code) VALUES (?, ?)";
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setString(1, studentId);
                insertPstmt.setString(2, courseCode);
                insertPstmt.executeUpdate();
            }

            // Successfully registered
            response.sendRedirect("courses.jsp?msg=registered_successfully");

        } catch (SQLException e) {
            System.err.println("Database error during course registration: " + e.getMessage());
            e.printStackTrace();

            if (e.getErrorCode() == 1062) {
                response.sendRedirect("courses.jsp?error=already_registered");
            } else {
                response.sendRedirect("courses.jsp?error=db_error");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("courses.jsp");
    }

    private static volatile Boolean curriculumRegTableCache = null;

    public static boolean hasCurriculumRegistrationsTable(Connection conn) {
        if (curriculumRegTableCache != null) {
            return curriculumRegTableCache;
        }
        try {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "curriculum_registrations", new String[]{"TABLE"})) {
                curriculumRegTableCache = rs.next();
                return curriculumRegTableCache;
            }
        } catch (Exception e) {
            curriculumRegTableCache = false;
            return false;
        }
    }
}
