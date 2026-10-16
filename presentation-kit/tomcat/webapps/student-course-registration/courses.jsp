<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.courseportal.util.DBConnection" %>
<%@ page import="com.courseportal.util.CSRFUtil" %>
<%@ page import="com.courseportal.util.RegistrationWindowManager" %>
<%@ page import="com.courseportal.util.CurriculumCatalogService" %>
<%@ page import="com.courseportal.util.CurriculumCatalogService.CurriculumCourse" %>
<%
    // -------------------------------------------------------------------------
    // 1. Session Protection
    // -------------------------------------------------------------------------
    if (session == null || session.getAttribute("studentId") == null) {
        response.sendRedirect("index.html?error=session_expired");
        return;
    }

    String studentId = (String) session.getAttribute("studentId");
    String csrfToken = CSRFUtil.getToken(session);
    Integer year = (Integer) session.getAttribute("year");
    Integer semester = (Integer) session.getAttribute("semester");
    if (year == null) year = 3;
    if (semester == null) semester = 1;

    boolean isRegOpen = RegistrationWindowManager.isWindowOpen(year, semester);

    // Selected semester filter for the read-only catalog explorer (1 to 8)
    int filterGlobalSem = (year - 1) * 2 + semester;
    String filterSemParam = request.getParameter("exploreSem");
    if (filterSemParam != null) {
        try {
            int parsed = Integer.parseInt(filterSemParam.trim());
            if (parsed >= 1 && parsed <= 8) {
                filterGlobalSem = parsed;
            }
        } catch (NumberFormatException ignored) {}
    }

    int exploreYear = (filterGlobalSem + 1) / 2;
    int exploreSemInYear = (filterGlobalSem % 2 == 1) ? 1 : 2;
    List<CurriculumCourse> catalogExplorerCourses = CurriculumCatalogService.getCoursesForSemester(exploreYear, exploreSemInYear);

    // Course item holder for eligible active courses
    class CourseItem {
        String code;
        String name;
        String faculty;
        int credits;
        String electiveGroup;

        CourseItem(String c, String n, String f, int cr, String eg) {
            this.code = c;
            this.name = n;
            this.faculty = f;
            this.credits = cr;
            this.electiveGroup = eg;
        }
    }

    List<CourseItem> eligibleCourses = new ArrayList<>();
    Set<String> enrolledCodes = new HashSet<>();
    String dbError = null;

    // 2. Fetch active courses and student's existing registrations via JDBC
    try (Connection conn = DBConnection.getConnection()) {

        // Query 1: Fetch all courses
        String coursesSql = "SELECT course_code, course_name, faculty, credits FROM courses ORDER BY course_code ASC";
        try (PreparedStatement coursesStmt = conn.prepareStatement(coursesSql);
             ResultSet rs = coursesStmt.executeQuery()) {
            while (rs.next()) {
                String cCode = rs.getString("course_code");
                String elGroup = CurriculumCatalogService.getElectiveGroup(cCode);
                eligibleCourses.add(new CourseItem(
                    cCode,
                    rs.getString("course_name"),
                    rs.getString("faculty"),
                    rs.getInt("credits"),
                    elGroup
                ));
            }
        }

        // Query 2: Fetch student's enrolled courses
        String regSql = "SELECT course_code FROM registrations WHERE student_id = ?";
        try (PreparedStatement regStmt = conn.prepareStatement(regSql)) {
            regStmt.setString(1, studentId);
            try (ResultSet regRs = regStmt.executeQuery()) {
                while (regRs.next()) {
                    enrolledCodes.add(regRs.getString("course_code"));
                }
            }
        }

    } catch (SQLException e) {
        dbError = "Database error while fetching courses: " + e.getMessage();
        e.printStackTrace();
    }

    // 3. Status message handling from URL query parameters
    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Courses & Curriculum | Course Registration Portal</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <!-- Header & Student Navigation Bar -->
    <header class="site-header">
        <div class="header-container">
            <div class="portal-title">
                <h1>Student Course Registration Portal</h1>
                <p>Web Technologies Laboratory — Department of Information Technology</p>
            </div>
            <nav class="nav-links">
                <a href="dashboard.jsp">Dashboard</a>
                <a href="courses.jsp" class="active">Courses</a>
                <a href="mycourses.jsp">My Courses</a>
                <a href="profile.jsp">Profile</a>
                <a href="LogoutServlet" class="logout-btn">Logout</a>
            </nav>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-wrapper">

        <div class="welcome-banner">
            <h2>Course Catalog & Registration</h2>
            <div class="welcome-meta">
                <span><strong>Your Academic Standing:</strong> Year <%= year %>, Semester <%= semester %></span>
                <span><strong>Registration Status:</strong>
                    <% if (isRegOpen) { %>
                        <span class="badge badge-success" style="font-weight: 600;">OPEN</span>
                    <% } else { %>
                        <span class="badge" style="background-color: var(--error-bg); color: var(--error-text); font-weight: 600;">CLOSED</span>
                    <% } %>
                </span>
            </div>
        </div>

        <!-- Feedback Alert Messages -->
        <% if ("registered_successfully".equals(msg)) { %>
            <div class="alert alert-success">Successfully registered for the course!</div>
        <% } %>

        <% if ("window_closed".equals(error)) { %>
            <div class="alert alert-error">Registration is currently CLOSED for your semester (Year <%= year %>, Semester <%= semester %>). Enrollment is disabled.</div>
        <% } else if ("elective_group_limit".equals(error)) { %>
            <div class="alert alert-error">You have already selected an elective from this group. Only one elective choice is allowed per group.</div>
        <% } else if ("already_registered".equals(error)) { %>
            <div class="alert alert-error">Already registered for this course.</div>
        <% } else if ("course_not_found".equals(error)) { %>
            <div class="alert alert-error">The selected course does not exist.</div>
        <% } else if ("invalid_request".equals(error)) { %>
            <div class="alert alert-error">Invalid course request.</div>
        <% } else if ("db_error".equals(error) || dbError != null) { %>
            <div class="alert alert-error"><%= (dbError != null) ? dbError : "A database error occurred. Please try again." %></div>
        <% } %>

        <% if (!isRegOpen) { %>
            <div class="alert" style="background-color: #fffbeb; border: 1px solid #fef3c7; color: #92400e; margin-bottom: 1.5rem;">
                <strong>Notice:</strong> The registration window for Year <%= year %>, Semester <%= semester %> is currently closed. You can review available courses and historical enrollments, but new registrations cannot be processed until the window is opened by the administrator.
            </div>
        <% } %>

        <!-- SECTION 1: Eligible Courses for Student's Semester -->
        <section class="content-section">
            <div class="section-header">
                <h3>Eligible Courses for Registration (Year <%= year %>, Sem <%= semester %>)</h3>
            </div>

            <div class="table-responsive">
                <% if (eligibleCourses.isEmpty()) { %>
                    <div class="empty-message">No courses are currently available for registration.</div>
                <% } else { %>
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 15%;">Course Code</th>
                                <th style="width: 30%;">Course Name</th>
                                <th style="width: 25%;">Faculty</th>
                                <th style="width: 15%;">Credits & Type</th>
                                <th style="width: 15%;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (CourseItem course : eligibleCourses) {
                                boolean isEnrolled = enrolledCodes.contains(course.code);
                            %>
                                <tr>
                                    <td><strong><%= course.code %></strong></td>
                                    <td>
                                        <%= course.name %>
                                        <% if (course.electiveGroup != null) { %>
                                            <div style="font-size: 0.78rem; color: var(--accent-blue); font-weight: 500;">[<%= course.electiveGroup %>]</div>
                                        <% } %>
                                    </td>
                                    <td><%= course.faculty %></td>
                                    <td><span class="badge badge-credits"><%= course.credits %> Credits</span></td>
                                    <td>
                                        <% if (isEnrolled) { %>
                                            <span class="badge badge-success">Enrolled</span>
                                        <% } else if (!isRegOpen) { %>
                                            <button type="button" class="btn-action" disabled
                                                    style="background-color: #cbd5e1; color: #64748b; cursor: not-allowed;"
                                                    title="Registration window is closed">Closed</button>
                                        <% } else { %>
                                            <form action="RegisterCourseServlet" method="POST" style="margin: 0;">
                                                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                <input type="hidden" name="courseCode" value="<%= course.code %>">
                                                <button type="submit" class="btn-action btn-register">Register</button>
                                            </form>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>
        </section>

        <!-- SECTION 2: Official R22 8-Semester Curriculum Explorer (Read-Only) -->
        <section class="content-section" style="margin-top: 2rem;">
            <div class="section-header" style="flex-wrap: wrap; gap: 0.75rem;">
                <div>
                    <h3>Official R22 Curriculum Explorer (Read-Only)</h3>
                    <p style="font-size: 0.85rem; color: var(--text-muted); margin-top: 0.2rem;">
                        Browse all 8 semesters from the official VCE R22 Curriculum.
                    </p>
                </div>
                <!-- Semester Filter Dropdown -->
                <form action="courses.jsp" method="GET" style="display: flex; align-items: center; gap: 0.5rem; margin: 0;">
                    <label for="exploreSem" style="font-size: 0.88rem; font-weight: 600; color: var(--text-main);">Semester:</label>
                    <select id="exploreSem" name="exploreSem" class="form-control" onchange="this.form.submit();"
                            style="width: auto; padding: 0.35rem 0.75rem; font-size: 0.88rem;">
                        <% for (int s = 1; s <= 8; s++) {
                            int yr = (s + 1) / 2;
                            int semInYr = (s % 2 == 1) ? 1 : 2;
                        %>
                            <option value="<%= s %>" <%= (s == filterGlobalSem) ? "selected" : "" %>>
                                Sem <%= s %> (Year <%= yr %>, Sem <%= semInYr %>)
                            </option>
                        <% } %>
                    </select>
                </form>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th style="width: 14%;">Course Code</th>
                            <th style="width: 32%;">Official Title</th>
                            <th style="width: 12%;">Category</th>
                            <th style="width: 16%;">Type</th>
                            <th style="width: 12%;">Credits</th>
                            <th style="width: 14%;">Requirement</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (CurriculumCourse cc : catalogExplorerCourses) { %>
                            <tr>
                                <td><strong><%= cc.getCode() %></strong></td>
                                <td>
                                    <%= cc.getName() %>
                                    <% if (cc.getElectiveGroup() != null) { %>
                                        <div style="font-size: 0.76rem; color: var(--accent-blue); font-weight: 500;">
                                            [<%= cc.getElectiveGroup() %>]
                                        </div>
                                    <% } %>
                                </td>
                                <td><span class="badge" style="background-color: #f1f5f9;"><%= cc.getCategory() %></span></td>
                                <td><%= cc.getType() %></td>
                                <td><span class="badge badge-credits"><%= cc.getCredits() %> Cr</span></td>
                                <td>
                                    <% if (cc.isMandatory()) { %>
                                        <span style="font-size: 0.82rem; color: var(--text-muted);">Mandatory</span>
                                    <% } else { %>
                                        <span style="font-size: 0.82rem; color: var(--accent-blue); font-weight: 600;">Elective</span>
                                    <% } %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <!-- Footer -->
    <footer class="site-footer">
        <p>&copy; Web Technologies Laboratory Course-End Project — B.Tech IT (R22 Regulations)</p>
    </footer>

</body>
</html>
