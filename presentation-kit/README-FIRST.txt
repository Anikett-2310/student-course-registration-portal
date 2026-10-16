================================================================================
  STUDENT COURSE REGISTRATION PORTAL - BEGINNER PRESENTATION GUIDE
================================================================================

Everything is pre-packaged and ready to present on any Windows 11 laptop!
Both Java 21 and Apache Tomcat 10.1 are ALREADY BUNDLED inside this kit.
You DO NOT need to install Java, Tomcat, or Maven!

--------------------------------------------------------------------------------
QUICK 3-STEP GUIDE FOR PRESENTING
--------------------------------------------------------------------------------

STEP 1: ONE-TIME DATABASE SETUP (Takes 1 minute)
  1. Ensure MySQL Server is installed and running on your laptop.
     (If you don't have MySQL yet, see "MySQL Setup" below)
  2. Double-click:
       1-SETUP-DATABASE.bat
  3. Enter your MySQL root password when prompted.
     The wizard will automatically set up the demo database and seed all courses!

STEP 2: START THE DEMO
  Double-click:
       2-START-PORTAL.bat

  - Automatically launches the isolated demo server.
  - Automatically opens your web browser to:
       http://localhost:8088/student-course-registration/

STEP 3: STOP THE DEMO WHEN FINISHED
  Double-click:
       3-STOP-PORTAL.bat

  - Cleanly shuts down the demo server and frees all ports.

--------------------------------------------------------------------------------
DEMO LOGIN CREDENTIALS
--------------------------------------------------------------------------------
[FACULTY / ADMIN PORTAL]:
  URL:      http://localhost:8088/student-course-registration/admin-login.html
  Username: demo_admin
  Password: DemoAdmin#2026

[STUDENT PORTAL]:
  URL:      http://localhost:8088/student-course-registration/
  Password for all demo students: DemoStudent#2026

  Student Accounts by Year:
  - Year 1 (Cohort 2026): Roll No `26881A1201` | Aarav Sharma (Sem 1)
  - Year 2 (Cohort 2025): Roll No `25881A1201` | Diya Patel   (Sem 1)
  - Year 3 (Cohort 2024): Roll No `24881A1201` | Kavya Reddy  (Sem 1)
  - Year 4 (Cohort 2023): Roll No `23881A1201` | Rohan Verma  (Sem 1)

--------------------------------------------------------------------------------
NEED MYSQL? (ONLY IF NOT ALREADY INSTALLED)
--------------------------------------------------------------------------------
If your laptop does not have MySQL Server installed yet:
  1. Download the free MySQL Community Installer from:
     https://dev.mysql.com/downloads/installer/
  2. Run the installer, select default settings ("Server only" or "Developer"),
     and set a root password (e.g., 'root' or 'DemoPass#2026').
  3. Once installed, double-click `1-SETUP-DATABASE.bat`!

--------------------------------------------------------------------------------
DIAGNOSTICS & VERIFICATION
--------------------------------------------------------------------------------
If you want to check that everything is working before faculty arrives,
double-click:
  VERIFY-SETUP.bat

For the complete step-by-step viva script to demonstrate features to professors,
read:
  PRESENTATION-CHECKLIST.txt
================================================================================
