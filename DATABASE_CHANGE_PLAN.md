# DATABASE CHANGE PLAN
## Multi-Year / 8-Semester Student Course Registration Portal

**Status:** PROPOSED ONLY — PENDING USER APPROVAL  
**Target Database:** `student_course_portal`  
**Database Product:** MySQL 8.0.46  
**Document Date:** 2026-10-13  

---

## 1. Summary of Proposed Changes

| Change Object | Type | Purpose | Live Data Impact |
| :--- | :--- | :--- | :--- |
| `registration_windows` | New Table | Controls open/closed registration state per semester & academic term | Zero impact on existing tables. Default window open if table absent. |
| `r22_curriculum_catalog` | New Table | Official R22 160-credit degree curriculum definitions across all 8 semesters (including distinct slots OE-I, OE-II, OE-III) | Completely separate from legacy courses; zero impact on existing tables. |
| `r22_open_electives` | New Table | Authoritative pool of all 41 inter-departmental open electives (from PDF pages 12-13) | Completely separate from legacy courses; zero impact on existing tables. |
| `students` (`is_active`) | Column Add (Nullable/Default) | Allows admin to deactivate student accounts without deleting registration history | Default `TRUE`; existing 5 student rows remain valid and active. |
| `courses` (`year`, `semester`, `category`, `course_type`, `elective_group`, `capacity`, `is_active`) | Columns Add (Default values) | Provides semester-awareness, capacity limits, and course deactivation | Defaults match existing Year 3 Sem 1 prototype records (`year=3, sem=1, category='PC', capacity=60, is_active=TRUE`). |
| `registrations` (`academic_term`) | Column Add (Default value) | Distinguishes historical registrations from future academic sessions | Default `'2026-2027 ODD'`; existing 8 registrations retain their exact timestamps. |
| `curriculum_registrations` | New Table | Persistent registrations for official R22 courses, professional electives, and separate OE-I/II/III slots | Completely separate from legacy `registrations`; zero impact on prototype rows. |

---

## 2. Table-by-Table Technical Specifications

### 2.1 Table: `registration_windows`
```sql
CREATE TABLE IF NOT EXISTS registration_windows (
    window_id INT AUTO_INCREMENT PRIMARY KEY,
    academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD',
    year INT NOT NULL,
    semester INT NOT NULL,
    status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'CLOSED',
    max_credits INT NOT NULL DEFAULT 24,
    notes VARCHAR(255) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_term_year_sem (academic_term, year, semester)
);
```
**Rationale:** Satisfies requirement 3 ("Admin controls which semester's registration is open. Students cannot bypass configured registration rules").

### 2.2 Table: `r22_curriculum_catalog`
```sql
CREATE TABLE IF NOT EXISTS r22_curriculum_catalog (
    catalog_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    global_semester INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    course_type VARCHAR(30) NOT NULL,
    credits INT NOT NULL,
    elective_group VARCHAR(50) NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT NULL,
    UNIQUE KEY uk_catalog_code_sem (course_code, global_semester)
);
```
**Rationale:** Decouples the official 160-credit R22 curriculum from the prototype database courses, eliminating destructive collisions between prototype `A8506` ("Web Technologies") and official `A8506` ("Computer Organization").

### 2.3 Table: `r22_open_electives`
```sql
CREATE TABLE IF NOT EXISTS r22_open_electives (
    open_elective_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'OE',
    credits INT NOT NULL DEFAULT 3,
    offering_department VARCHAR(100) NOT NULL DEFAULT 'Inter-Departmental / Open'
);
```
**Rationale:** Authoritative master table for all 41 open electives extracted from pages 12–13 of the official R22 curriculum PDF.

### 2.4 Non-Destructive Column Additions to `courses`
```sql
ALTER TABLE courses
  ADD COLUMN year INT NOT NULL DEFAULT 3 AFTER credits,
  ADD COLUMN semester INT NOT NULL DEFAULT 1 AFTER year,
  ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'PC' AFTER semester,
  ADD COLUMN course_type VARCHAR(30) NOT NULL DEFAULT 'Theory' AFTER category,
  ADD COLUMN elective_group VARCHAR(50) NULL AFTER course_type,
  ADD COLUMN capacity INT NOT NULL DEFAULT 60 AFTER elective_group,
  ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER capacity;
```

---

## 3. Registration & Elective Selection Storage Analysis

### 3.1 Current Schema Limitation (Why Official-Catalog Registration is NOT Production-Ready in DB)
The current live table `registrations` is defined as:
```sql
CREATE TABLE registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registrations_students FOREIGN KEY (student_id) REFERENCES students(student_id),
    CONSTRAINT fk_registrations_courses FOREIGN KEY (course_code) REFERENCES courses(course_code)
);
```
**Identified Persistence Gaps:**
1. **Foreign Key Constraint:** `registrations.course_code` strictly references prototype table `courses(course_code)`. The live `courses` table contains only 5 prototype records. Attempting to persist registrations for official R22 catalog courses (e.g. `A8604`) or open electives (e.g. `A8181`) would immediately fail with a foreign key constraint violation.
2. **Missing Elective Slot Tracking:** There is no column to record which elective slot a selection fulfills (`OE-I`, `OE-II`, `OE-III`, or `PE-I` through `PE-VI`).
3. **No Slot Disambiguation:** In Semester 8, students choose two separate open electives (`OE-II` and `OE-III`). Without an explicit slot column, the system cannot distinguish which course was selected for which slot.

### 3.2 Proposed Dedicated Registration Table: `curriculum_registrations`
To make 8-semester official registrations production-ready without corrupting prototype records:
```sql
CREATE TABLE IF NOT EXISTS curriculum_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD',
    year INT NOT NULL,
    semester INT NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL COMMENT 'PC, PE, OE, etc.',
    elective_slot VARCHAR(20) NULL COMMENT 'CORE, PE-I..VI, OE-I, OE-II, OE-III',
    credits INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_slot_term (student_id, academic_term, elective_slot, course_code),
    FOREIGN KEY (student_id) REFERENCES students(student_id)
);
```

---

## 4. Existing Data Compatibility & Conflict Handling

### 4.1 Prototype Course Conflicts
1. `A8506`:
   - Live DB: "Web Technologies", Ms. B. Swapna, 3 credits.
   - Official R22: "Computer Organization", 2 credits (Semester 2). Official Web Tech is `A8604` (Semester 5).
   - **Handling:** Preserve live `A8506` in `courses` table so Student `24881A1201`'s registration row 5 (`registration_id=5`) remains valid. In `r22_curriculum_catalog`, record official `A8506` as Computer Organization and `A8604` as Web Technologies.
2. `A8519`, `A8520`, `A8704`, `A8851`:
   - Live rows in `courses` are **not modified** without explicit approval.

### 4.2 Historical Registration Protection
The 8 existing registration records (`registration_id` 4 through 11) have exact foreign keys referencing `students(student_id)` and `courses(course_code)`.
- Foreign key definitions are preserved.
- Zero `DROP TABLE` or `DELETE` statements are ever executed.

---

## 5. Admin Password Security & Safe Migration Plan

### 5.1 Current Baseline
- Admin credentials currently reside in table `admin` (`username VARCHAR(50)`, `password VARCHAR(50)`).
- Plaintext password comparison in `AdminLoginServlet.java`.
- **Policy:** Credentials must never be displayed in plain text in audit logs, reports, or repository artifacts. Interactive password authentication (`-p`) or secure in-memory credentials must always be used.

### 5.2 Zero-Downtime Password Migration Plan
1. **Schema Extension (Non-Breaking):**
   ```sql
   ALTER TABLE admin
       ADD COLUMN password_hash VARCHAR(255) NULL AFTER password,
       ADD COLUMN hash_algo VARCHAR(30) NOT NULL DEFAULT 'LEGACY_PLAIN';
   ```
2. **Dual-Mode Authentication:**
   - In `AdminLoginServlet`, if `hash_algo == 'BCRYPT'` and `password_hash != null`, verify using BCrypt/PBKDF2.
   - If `hash_algo == 'LEGACY_PLAIN'`, verify legacy password via constant-time comparison.
   - **Transparent Upgrade:** Upon successful login with legacy credentials, immediately hash the password, populate `password_hash`, set `hash_algo = 'BCRYPT'`, and clear `password = NULL`.
3. **Safety Guarantee:** Zero downtime and zero lockout risk for existing administrative users.

---

## 6. Verification & Validation Queries

To run after an approved change:
```sql
-- 1. Verify prototype student count (must remain exactly 5)
SELECT COUNT(*) FROM students;

-- 2. Verify prototype course count (must remain exactly 5)
SELECT COUNT(*) FROM courses;

-- 3. Verify registrations count and exact integrity (must remain exactly 8)
SELECT COUNT(*) FROM registrations;
SELECT registration_id, student_id, course_code, registered_at FROM registrations ORDER BY registration_id;

-- 4. Verify admin account exists (must remain exactly 1)
SELECT username FROM admin WHERE username = 'admin';

-- 5. Verify official R22 curriculum catalog count (must equal exactly 92 courses across semesters 1-8)
SELECT COUNT(*) FROM r22_curriculum_catalog;

-- 6. Verify official open electives pool count (must equal exactly 41 open electives)
SELECT COUNT(*) FROM r22_open_electives;

-- 7. Verify registration windows count (must equal exactly 8 rows, all initialized to CLOSED)
SELECT COUNT(*) FROM registration_windows WHERE status = 'CLOSED';
```

---

## 7. Outstanding Risks & Approval Boundaries

1. **Risk:** Schema execution without backup could cause syntax or lock errors.  
   **Mitigation:** `mysqldump` full backup must be taken and verified prior to any DDL execution.
2. **Risk:** In-memory registration windows reset to CLOSED on Tomcat restart.  
   **Mitigation:** By design, registration windows default to CLOSED for security. Once approved, `registration_windows` table will persist states.
3. **Execution Boundary:** This change plan is **NOT APPROVED FOR EXECUTION**. It remains in proposal state until explicitly authorized by the user.

---

## 8. Safe Rollback & Disaster Recovery Procedures

### 8.1 Mandatory Pre-Migration Backup Verification & Baseline Recording
Before executing any DDL or seed scripts against the production database:
1. **Record Exact Production Baseline Row Counts:**
   - Table `students`: exactly 5 rows (`24881A1201` through `24881A1205`)
   - Table `courses`: exactly 5 rows (`A8506`, `A8519`, `A8520`, `A8704`, `A8851`)
   - Table `registrations`: exactly 8 rows (`registration_id` 4 through 11)
   - Table `admin`: exactly 1 row (`username = 'admin'`)
2. **Take Full Logical Dump:**
   ```powershell
   mysqldump -u root -p --routines --triggers --events student_course_portal > "C:\Users\anike\OneDrive\Desktop\clg html\course end\backup_student_course_portal_prod.sql"
   ```
3. **Verify Backup File Integrity Before Proceeding:**
   - Confirm file size is greater than 0 bytes.
   - Confirm the file tail terminates cleanly with `-- Dump completed`.
   - If the backup file is missing, zero bytes, or incomplete, **ABORT migration immediately**.

### 8.2 Safe Recovery Philosophy: Application-First Rollback (Leave Database Intact)
> [!IMPORTANT]
> **Preferred Recovery Approach:** If any operational or application defect is identified post-deployment, **roll back ONLY the application WAR file** (`student-course-registration.war`). **Do NOT revert or drop the database schema.**

**Why Database Rollback Is NOT Necessary for Application Rollbacks:**
- The database schema additions (`curriculum_registrations`, `r22_curriculum_catalog`, `r22_open_electives`, `registration_windows`) are **completely decoupled, non-destructive, and additive**.
- Neither the new tables nor new default columns conflict with the previous baseline application WAR.
- Rolling back only the WAR immediately restores the previous application behavior while keeping all registration history, newly enrolled courses, and student account records 100% intact.

### 8.3 Protection of Post-Backup Registrations (NEVER Blindly Restore Full Dumps)
> [!CAUTION]
> **NEVER restore a pre-migration database dump blindly over an active production database.**
> If students register for courses, admins update windows, or accounts are created after the pre-migration backup was taken, running `mysql < backup.sql` will **permanently wipe out all newly created registrations and data**.
>
> **NEVER blindly execute `DROP TABLE curriculum_registrations`:**
> Once the system is live, `curriculum_registrations` contains official student enrollments. Blindly dropping this table destroys student registration records. Furthermore, do NOT rely on `SELECT ... INTO OUTFILE` as a recovery mechanism, as file export permissions (`secure_file_priv`) are frequently restricted in production environments.

### 8.4 Strengthened Deployment Procedure & Artifact Preservation
To deploy the new release safely and prevent stale file contamination:
1. **Preserve Current Production Artifacts (Rollback Availability):**
   - Verify backup files exist and are non-empty before any modification:
     ```powershell
     # Verify existing baseline backups taken in Phase 1:
     Test-Path "C:\Users\anike\OneDrive\Desktop\clg html\course end\backup_student-course-registration_prod.war"
     Test-Path "C:\Users\anike\OneDrive\Desktop\clg html\course end\backup_student-course-registration_exploded"
     ```
   - Keep both `backup_student-course-registration_prod.war` and `backup_student-course-registration_exploded\` permanently available for instant rollback.

2. **Safely Stop Apache Tomcat and Verify Process Termination:**
   - Initiate graceful shutdown:
     ```powershell
     & "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin\shutdown.bat"
     ```
   - **Verification Assertion:** Poll for complete process and port termination before modifying files:
     ```powershell
     # Wait up to 15 seconds and verify port 8080 is fully released:
     $timeout = 15; $stopped = $false
     while ($timeout -gt 0) {
         $listening = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
         if (-not $listening) { $stopped = $true; break }
         Start-Sleep -Seconds 1; $timeout--
     }
     if (-not $stopped) { throw "Tomcat failed to stop cleanly within 15 seconds. ABORTING deployment." }
     ```

3. **Prevent Stale File Contamination (Clean Exploded Directory):**
   - With backups safely secured, remove the live exploded directory so Tomcat is forced to unpack the new WAR cleanly, preventing stale JSPs, cached classfiles, or outdated session artifacts:
     ```powershell
     $liveExploded = "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\student-course-registration"
     if (Test-Path $liveExploded) {
         Remove-Item -Path $liveExploded -Recurse -Force
     }
     ```

4. **Deploy Verified New WAR:**
   - Copy the newly built, verified WAR to Tomcat `webapps\`:
     ```powershell
     Copy-Item "C:\Users\anike\OneDrive\Desktop\clg html\course end\target\student-course-registration.war" `
               "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\student-course-registration.war" -Force
     ```

5. **Start Apache Tomcat and Verify Deployment:**
   - Start the Tomcat service:
     ```powershell
     & "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin\startup.bat"
     ```
   - **Verification Assertions (Startup Logs, Port, & HTTP Response):**
     - Wait 8 seconds for application initialization.
     - Verify port 8080 is listening:
       ```powershell
       Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object LocalAddress, LocalPort
       ```
     - Inspect startup logs for clean deployment:
       ```powershell
       Get-Content (Get-ChildItem "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\logs\catalina.*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName -Tail 50
       ```
     - Verify HTTP 200 response:
       ```powershell
       Invoke-WebRequest -Uri "http://localhost:8080/student-course-registration/" -UseBasicParsing | Select-Object StatusCode, StatusDescription
       ```

### 8.5 Two-Tiered Recovery: Pre-Traffic Abort vs. Post-Traffic Reconciliation
The recovery approach is strictly divided based on whether student traffic has commenced:

#### Tier A: Rollback BEFORE Application Traffic (Pre-Traffic Abort)
If schema creation or seeding encounters an unrecoverable failure *before* the application WAR is deployed and *before* any student has accessed the system:
1. **Prefer Safe Idempotent Re-execution:**
   - Because `PROPOSED_SCHEMA_CHANGES.sql` utilizes `CREATE TABLE IF NOT EXISTS` and `PROPOSED_R22_CURRICULUM_SEED.sql` utilizes `INSERT IGNORE INTO`, rerun the script safely rather than tearing down tables.
2. **Strict Guard Against Blind Dropping:**
   - Automated scripts and operators must **NEVER** execute an unverified or blind `DROP TABLE curriculum_registrations`.
   - If reference catalog objects must be cleared for a clean retry, the operator must verify zero data presence:
     ```sql
     -- Safety assertion: MUST return 0 before any table cleanup:
     SELECT COUNT(*) FROM curriculum_registrations;
     ```
   - Only when count is confirmed 0 and before traffic commences may empty auxiliary tables be reset if needed:
     ```sql
     -- NEVER DROP PROTOTYPE TABLES (students, courses, registrations, admin)
     -- Auxiliary tables may only be reset if curriculum_registrations is confirmed 0:
     DROP TABLE IF EXISTS curriculum_registrations;
     DROP TABLE IF EXISTS r22_open_electives;
     DROP TABLE IF EXISTS r22_curriculum_catalog;
     DROP TABLE IF EXISTS registration_windows;
     ```
3. Prototype tables (`students`, `courses`, `registrations`, `admin`) are **NEVER touched or dropped** under any circumstances.

#### Tier B: Incident Recovery AFTER Application Traffic Has Commenced (Post-Traffic)
Once the application is live and students have begun registering:
1. **ABSOLUTE PROHIBITION ON DROPPING `curriculum_registrations`:**
   - Under no circumstances may `curriculum_registrations` or any existing table be dropped. Doing so would irretrievably destroy official student course enrollments and elective choices.
2. **ABSOLUTE PROHIBITION ON RESTORING PRE-MIGRATION DUMP OVER LIVE DATABASE:**
   - Restoring `backup_student_course_portal_prod.sql` over the active production database would overwrite all course registrations, newly created student accounts, and window states created since the backup.
3. **Application WAR-Only Rollback (Zero-Data-Loss Standard):**
   - If an application bug, servlet defect, or UI issue is discovered, **revert ONLY the application WAR** to `backup_student-course-registration_prod.war` as detailed in Section 8.4.
   - **Leave the database schema and all tables completely intact.**
   - The previous application version will seamlessly serve students using prototype tables, while all newly created tables and rows remain preserved without interference.
4. **Preservation and Reconciliation of Newly Created Records:**
   - **Audit New Registrations:** All registrations submitted through the new system are safely retained in `curriculum_registrations`. Inspect them at any time:
     ```sql
     SELECT registration_id, student_id, academic_term, year, semester, course_code, course_name, category, elective_slot, credits, registered_at 
     FROM curriculum_registrations 
     ORDER BY registration_id;
     ```
   - **Reconciliation to Legacy Tables (if needed for backward compatibility):**
     If legacy reporting tools or administrators require course records visible in the legacy `registrations` table:
     ```sql
     -- Safe, idempotent reconciliation query for prototype courses:
     INSERT IGNORE INTO registrations (student_id, course_code, registered_at)
     SELECT cr.student_id, cr.course_code, cr.registered_at
     FROM curriculum_registrations cr
     JOIN courses c ON cr.course_code = c.course_code;
     ```
   - **Emergency State Snapshot (if low-level DB maintenance is required):**
     Before any diagnostic maintenance, create internal database snapshots to freeze and safeguard live registrations:
     ```sql
     CREATE TABLE IF NOT EXISTS emergency_save_curriculum_reg AS SELECT * FROM curriculum_registrations;
     CREATE TABLE IF NOT EXISTS emergency_save_registrations AS SELECT * FROM registrations;
     CREATE TABLE IF NOT EXISTS emergency_save_students AS SELECT * FROM students;
     ```
     Once maintenance concludes, verify row counts against the emergency snapshot tables before re-opening registration windows.

### 8.6 Preservation of Existing Production Data & Password Separation
- **Zero Modification of Prototype Records:** The migration scripts do not execute `UPDATE`, `DELETE`, or `DROP` on existing tables. Rows in `students` (5), `courses` (5), `registrations` (8), and `admin` (1) are 100% preserved.
- **Admin Password Migration Separation:** Admin password hashing remains completely excluded from this deployment. Table `admin` and `AdminLoginServlet` will not be altered during this release.

---

## 9. Corrected Windows PowerShell Execution Commands (Awaiting Phase 2 Authorization)

> [!WARNING]
> **DO NOT EXECUTE THESE COMMANDS UNTIL EXPLICIT APPROVAL IS GRANTED FOR PHASE 2.**
> All commands have been verified for Windows PowerShell compatibility. Redirection (`<`) has been replaced with `Get-Content -Raw | & mysql.exe`. Password authentication uses interactive prompts (`-p`) or temporary scoped credentials; passwords are never printed or saved.

### 9.1 Step 1: Pre-Execution Production Baseline Verification
```powershell
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
& $mysql -u root -p -e "SELECT 'students' AS tbl, COUNT(*) AS count FROM student_course_portal.students UNION ALL SELECT 'courses', COUNT(*) FROM student_course_portal.courses UNION ALL SELECT 'registrations', COUNT(*) FROM student_course_portal.registrations UNION ALL SELECT 'admin', COUNT(*) FROM student_course_portal.admin;"
```
*Expected: students=5, courses=5, registrations=8, admin=1.*

### 9.2 Step 2: Apply Schema DDL (`PROPOSED_SCHEMA_CHANGES.sql`)
```powershell
$schemaFile = "C:\Users\anike\OneDrive\Desktop\clg html\course end\PROPOSED_SCHEMA_CHANGES.sql"
Get-Content -Raw $schemaFile | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p student_course_portal
```

### 9.3 Step 3: Apply Curriculum Seed (`PROPOSED_R22_CURRICULUM_SEED.sql`)
```powershell
$seedFile = "C:\Users\anike\OneDrive\Desktop\clg html\course end\PROPOSED_R22_CURRICULUM_SEED.sql"
Get-Content -Raw $seedFile | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p student_course_portal
```

### 9.4 Step 4: Verify Database Post-Migration State
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p student_course_portal -e "
SELECT 'students' AS item, COUNT(*) AS cnt FROM students
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'registrations', COUNT(*) FROM registrations
UNION ALL SELECT 'admin', COUNT(*) FROM admin
UNION ALL SELECT 'r22_curriculum_catalog', COUNT(*) FROM r22_curriculum_catalog
UNION ALL SELECT 'r22_open_electives', COUNT(*) FROM r22_open_electives
UNION ALL SELECT 'registration_windows_closed', COUNT(*) FROM registration_windows WHERE status = 'CLOSED';
"
```
*Expected: students=5, courses=5, registrations=8, admin=1, r22_curriculum_catalog=92, r22_open_electives=41, registration_windows_closed=8.*

### 9.5 Step 5: Stop Tomcat, Replace WAR, and Clean Exploded Directory
```powershell
# 1. Gracefully shutdown Tomcat
& "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin\shutdown.bat"

# 2. Wait and verify port 8080 is released
$timeout = 15; $stopped = $false
while ($timeout -gt 0) {
    $listening = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if (-not $listening) { $stopped = $true; break }
    Start-Sleep -Seconds 1; $timeout--
}
if (-not $stopped) { throw "Tomcat failed to stop cleanly within 15 seconds. ABORTING deployment." }

# 3. Clean live exploded directory to avoid stale files
$liveExploded = "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\student-course-registration"
if (Test-Path $liveExploded) {
    Remove-Item -Path $liveExploded -Recurse -Force
}

# 4. Deploy newly built, verified WAR
Copy-Item "C:\Users\anike\OneDrive\Desktop\clg html\course end\target\student-course-registration.war" `
          "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\webapps\student-course-registration.war" -Force

# 5. Start Tomcat
& "C:\apache-tomcat-10.1.60-windows-x64\apache-tomcat-10.1.60\bin\startup.bat"

# 6. Verify startup logs and HTTP 200 response
Start-Sleep -Seconds 8
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object LocalAddress, LocalPort
Invoke-WebRequest -Uri "http://localhost:8080/student-course-registration/" -UseBasicParsing | Select-Object StatusCode, StatusDescription
```
