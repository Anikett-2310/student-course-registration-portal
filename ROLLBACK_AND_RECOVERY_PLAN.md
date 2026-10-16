# ROLLBACK AND RECOVERY PLAN
## Student Course Registration Portal | Multi-Year / 8-Semester System

**Target Database:** `student_course_portal`  
**Execution Status:** DESIGN-ONLY / DRAFT  
**Document Date:** 2026-10-13  

---

## 1. Pre-Execution Backup Strategy

Prior to executing any approved DDL in MySQL 8, take a consistent, authenticated logical backup:

```powershell
# Command to generate physical SQL dump with routines and events
mysqldump -u root -proot --routines --triggers --databases student_course_portal > "C:\Users\anike\OneDrive\Desktop\clg html\course end\database\backup_student_course_portal_pre_migration.sql"
```

Verify that the backup file contains:
- 5 `INSERT INTO students` records (`24881A1201` - `24881A1205`)
- 5 `INSERT INTO courses` records (`A8506`, `A8519`, `A8520`, `A8704`, `A8851`)
- 8 `INSERT INTO registrations` records (IDs 4 to 11 with exact 2026-10-12 timestamps)
- 1 `INSERT INTO admin` record (`admin`)

---

## 2. In-Flight Failure Recovery Matrix

| Failure Point | Cause | Action |
| :--- | :--- | :--- |
| `registration_windows` table creation fails | Syntax error or privilege error | Abort script. No existing tables modified. |
| `r22_curriculum_catalog` creation fails | Disk full or name conflict | Abort script. Drop partially created table if needed. Existing data untouched. |
| `ALTER TABLE students ADD COLUMN` fails | Table lock timeout | Cancel ALTER. Existing table unchanged. |
| `ALTER TABLE courses ADD COLUMN` fails | Lock or metadata mismatch | Cancel ALTER. Revert to original table. |
| Application fails after deployment | Java exception / class not found | Re-deploy baseline `student-course-registration.war`. |

---

## 3. Reversal / Rollback SQL Script

If a change was executed and must be undone cleanly:

```sql
-- 1. Drop newly created auxiliary tables
DROP TABLE IF EXISTS r22_curriculum_catalog;
DROP TABLE IF EXISTS registration_windows;

-- 2. Drop added columns from courses (if added)
-- ALTER TABLE courses DROP COLUMN is_active;
-- ALTER TABLE courses DROP COLUMN capacity;
-- ALTER TABLE courses DROP COLUMN elective_group;
-- ALTER TABLE courses DROP COLUMN course_type;
-- ALTER TABLE courses DROP COLUMN category;
-- ALTER TABLE courses DROP COLUMN semester;
-- ALTER TABLE courses DROP COLUMN year;

-- 3. Drop added columns from students (if added)
-- ALTER TABLE students DROP COLUMN is_active;

-- 4. Drop added columns from registrations (if added)
-- ALTER TABLE registrations DROP COLUMN academic_term;
```

---

## 4. Post-Rollback Integrity Verification

Execute the read-only audit to verify restoration:
```powershell
& "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" -cp "target/classes;target/test-classes;C:\Users\anike\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" com.courseportal.test.ReadOnlyDatabaseAudit
```
Expected output:
- Exactly 5 students
- Exactly 5 courses
- Exactly 8 registrations
- Exactly 1 admin
- Zero missing timestamps or lost records.
