-- ============================================================================
-- PROPOSED SCHEMA CHANGES (PROPOSAL ONLY — DO NOT EXECUTE)
-- Database: student_course_portal
-- Target: Multi-Year / 8-Semester Architecture with Registration Windows
-- ============================================================================

-- SAFETY PREAMBLE:
-- This script is DESIGN-ONLY and prepared for formal administrative review.
-- It MUST NOT be executed without explicit authorization.
-- Existing students, courses, and registrations are completely preserved.

-- ----------------------------------------------------------------------------
-- 1. Table: registration_windows
-- Controls whether course registration is OPEN or CLOSED for a specific semester
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registration_windows (
    window_id INT AUTO_INCREMENT PRIMARY KEY,
    academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD',
    year INT NOT NULL COMMENT 'Academic Year (1-4)',
    semester INT NOT NULL COMMENT 'Semester within Year (1-2)',
    status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'CLOSED',
    max_credits INT NOT NULL DEFAULT 24,
    notes VARCHAR(255) NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_term_year_sem (academic_term, year, semester)
);

-- Seed initial registration windows (Default: CLOSED for all 4 years / 8 semesters; opened only by admin)
-- (To be executed only upon approved migration)
-- INSERT INTO registration_windows (academic_term, year, semester, status, max_credits) VALUES
-- ('2026-2027 ODD', 1, 1, 'CLOSED', 20),
-- ('2026-2027 ODD', 1, 2, 'CLOSED', 20),
-- ('2026-2027 ODD', 2, 1, 'CLOSED', 20),
-- ('2026-2027 ODD', 2, 2, 'CLOSED', 20),
-- ('2026-2027 ODD', 3, 1, 'CLOSED', 20),
-- ('2026-2027 ODD', 3, 2, 'CLOSED', 20),
-- ('2026-2027 ODD', 4, 1, 'CLOSED', 20),
-- ('2026-2027 ODD', 4, 2, 'CLOSED', 20);

-- ----------------------------------------------------------------------------
-- 2. Non-Destructive Extension of Table: students
-- Adds account status flag for secure disablement without deleting registration history
-- ----------------------------------------------------------------------------
-- ALTER TABLE students
--   ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER email;

-- ----------------------------------------------------------------------------
-- 3. Non-Destructive Extension of Table: courses
-- Adds semester-awareness, course categorization, and capacity limits
-- ----------------------------------------------------------------------------
-- ALTER TABLE courses
--   ADD COLUMN year INT NOT NULL DEFAULT 3 AFTER credits,
--   ADD COLUMN semester INT NOT NULL DEFAULT 1 AFTER year,
--   ADD COLUMN category VARCHAR(20) NOT NULL DEFAULT 'PC' AFTER semester,
--   ADD COLUMN course_type VARCHAR(30) NOT NULL DEFAULT 'Theory' AFTER category,
--   ADD COLUMN elective_group VARCHAR(50) NULL AFTER course_type,
--   ADD COLUMN capacity INT NOT NULL DEFAULT 60 AFTER elective_group,
--   ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER capacity;

-- ----------------------------------------------------------------------------
-- 4. Table: r22_curriculum_catalog
-- Stores the authoritative official 160-credit R22 curriculum definitions
-- completely decoupled from prototype legacy courses
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS r22_curriculum_catalog (
    catalog_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    year INT NOT NULL COMMENT '1 to 4',
    semester INT NOT NULL COMMENT '1 to 2',
    global_semester INT NOT NULL COMMENT '1 to 8',
    category VARCHAR(20) NOT NULL COMMENT 'BS, ES, HS, PC, PE, OE, PW, MC',
    course_type VARCHAR(30) NOT NULL COMMENT 'Theory, Laboratory, Skill, Project, Non-Credit',
    credits INT NOT NULL,
    elective_group VARCHAR(50) NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT NULL,
    UNIQUE KEY uk_catalog_code_sem (course_code, global_semester)
);

-- ----------------------------------------------------------------------------
-- 5. Table: r22_open_electives
-- Stores the authoritative list of 41 inter-departmental open electives
-- selectable across OE-I (Sem 7), OE-II (Sem 8), and OE-III (Sem 8)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS r22_open_electives (
    open_elective_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'OE',
    credits INT NOT NULL DEFAULT 3,
    offering_department VARCHAR(100) NOT NULL DEFAULT 'Inter-Departmental / Open'
);

-- ----------------------------------------------------------------------------
-- 6. Non-Destructive Extension of Table: registrations
-- Adds academic term tracking to preserve historical registration records across terms
-- ----------------------------------------------------------------------------
-- ALTER TABLE registrations
--   ADD COLUMN academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD' AFTER course_code;

-- ----------------------------------------------------------------------------
-- 7. Table: curriculum_registrations
-- Stores persistent course registrations for official R22 courses, professional electives,
-- and separate open-elective slots (OE-I, OE-II, OE-III) across all 8 semesters.
-- Completely decoupled from legacy prototype registrations table to preserve history.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS curriculum_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD',
    year INT NOT NULL COMMENT 'Academic Year (1-4)',
    semester INT NOT NULL COMMENT 'Semester within Year (1-2)',
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL COMMENT 'BS, ES, HS, PC, PE, OE, PW, MC',
    elective_slot VARCHAR(20) NULL COMMENT 'NULL for core - or PE-I..VI, OE-I, OE-II, OE-III',
    credits INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_curriculum_reg_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_course_term (student_id, course_code, academic_term),
    UNIQUE KEY uk_student_slot_term (student_id, elective_slot, academic_term)
);

