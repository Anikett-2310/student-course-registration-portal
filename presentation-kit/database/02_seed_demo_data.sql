-- ============================================================================
-- 02_seed_demo_data.sql
-- Presentation Kit: Student Course Registration Portal (Demo Environment)
-- ============================================================================
-- PURPOSE:
-- 1. Creates all tables required by the application in 'student_course_portal_demo'.
-- 2. Seeds FICTIONAL sample accounts only (zero production data).
-- 3. Seeds complete R22 Curriculum Catalog (92 courses) and Open Electives (41 courses).
-- 4. Seeds all 8 registration windows with status = 'CLOSED'.
--
-- EXECUTION:
-- Run this script as 'portal_demo' or MySQL administrator:
--   mysql -u portal_demo -pDemoPortal#2026 student_course_portal_demo < database\02_seed_demo_data.sql
-- ============================================================================

USE `student_course_portal_demo`;

-- ----------------------------------------------------------------------------
-- 1. Table: admin (Fictional Demo Administrator)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO admin (username, password) VALUES ('demo_admin', 'DemoAdmin#2026')
    ON DUPLICATE KEY UPDATE password = VALUES(password);

-- ----------------------------------------------------------------------------
-- 2. Table: courses (Prototype Core Courses)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    course_code VARCHAR(10) NOT NULL PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    faculty VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    elective_slot VARCHAR(50) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO courses (course_code, course_name, faculty, credits, elective_slot) VALUES
('A8506', 'Web Technologies', 'Prof. Sharma', 3, 'CORE'),
('A8519', 'Computer Networks', 'Dr. Iyer', 1, 'CORE'),
('A8520', 'Software Engineering', 'Prof. Rao', 2, 'CORE'),
('A8704', 'Machine Learning', 'Dr. Madhavi', 3, 'CORE'),
('A8851', 'Data Science', 'Dr. Gogula', 4, 'PE-I');

-- ----------------------------------------------------------------------------
-- 3. Table: students (Fictional Sample Students across All 4 Cohorts)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    branch VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    email VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO students (student_id, name, password, branch, year, semester, email) VALUES
('26881A1201', 'Aarav Sharma', 'DemoStudent#2026', 'IT', 1, 1, 'aarav.demo@college.edu'),
('25881A1201', 'Diya Patel', 'DemoStudent#2026', 'IT', 2, 1, 'diya.demo@college.edu'),
('24881A1201', 'Kavya Reddy', 'DemoStudent#2026', 'IT', 3, 1, 'kavya.demo@college.edu'),
('23881A1201', 'Rohan Verma', 'DemoStudent#2026', 'IT', 4, 1, 'rohan.demo@college.edu')
ON DUPLICATE KEY UPDATE name=VALUES(name), year=VALUES(year), semester=VALUES(semester), password=VALUES(password);

-- ----------------------------------------------------------------------------
-- 4. Table: registrations (Baseline Course Enrollments)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_demo_reg_student FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    CONSTRAINT fk_demo_reg_course FOREIGN KEY (course_code) REFERENCES courses (course_code) ON DELETE CASCADE,
    UNIQUE KEY uk_demo_student_course (student_id, course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO registrations (student_id, course_code) VALUES
('24881A1201', 'A8506');

-- ----------------------------------------------------------------------------
-- 5. Table: registration_windows (All 8 Semesters Default CLOSED)
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO registration_windows (academic_term, year, semester, status, max_credits, notes) VALUES
('2026-2027 ODD', 1, 1, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 1, 2, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 2, 1, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 2, 2, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 3, 1, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 3, 2, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 4, 1, 'CLOSED', 24, 'Default Closed for Demo'),
('2026-2027 ODD', 4, 2, 'CLOSED', 24, 'Default Closed for Demo')
ON DUPLICATE KEY UPDATE status = 'CLOSED';

-- ----------------------------------------------------------------------------
-- 6. Table: r22_curriculum_catalog (Full 92 Courses)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS r22_curriculum_catalog (
    catalog_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    global_semester INT NOT NULL,
    category VARCHAR(20) NOT NULL,
    course_type VARCHAR(30) NOT NULL DEFAULT 'Theory',
    credits INT NOT NULL,
    elective_group VARCHAR(50) NULL,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT NULL,
    UNIQUE KEY uk_catalog_code_sem (course_code, global_semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 7. Table: r22_open_electives (Full 41 Electives)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS r22_open_electives (
    open_elective_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'OE',
    credits INT NOT NULL DEFAULT 3,
    offering_department VARCHAR(100) NOT NULL DEFAULT 'Inter-Departmental / Open'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 8. Table: curriculum_registrations (Interactive Demo Registrations)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS curriculum_registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    academic_term VARCHAR(30) NOT NULL DEFAULT '2026-2027 ODD',
    year INT NOT NULL,
    semester INT NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    course_name VARCHAR(150) NOT NULL,
    category VARCHAR(20) NOT NULL,
    elective_slot VARCHAR(50) NOT NULL,
    credits INT NOT NULL DEFAULT 3,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_demo_curriculum_reg_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_term_course (student_id, academic_term, course_code),
    UNIQUE KEY uk_student_term_slot (student_id, academic_term, elective_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8001', 'Matrices and Calculus', 1, 1, 1, 'BS', 'Theory', 4, NULL, TRUE),
('A8008', 'Engineering Chemistry', 1, 1, 1, 'BS', 'Theory', 3, NULL, TRUE),
('A8010', 'English for Skill Enhancement', 1, 1, 1, 'HS', 'Theory', 2, NULL, TRUE),
('A8402', 'Digital Electronics', 1, 1, 1, 'ES', 'Theory', 3, NULL, TRUE),
('A8501', 'Problem Solving through C', 1, 1, 1, 'ES', 'Theory', 3, NULL, TRUE),
('A8009', 'Engineering Chemistry Laboratory', 1, 1, 1, 'BS', 'Laboratory', 1, NULL, TRUE),
('A8011', 'English Language and Communication Skills Laboratory', 1, 1, 1, 'HS', 'Laboratory', 1, NULL, TRUE),
('A8502', 'Problem Solving through C Laboratory', 1, 1, 1, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8302', 'Computer Aided Drawing', 1, 1, 1, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8025', 'Community Centered Design Thinking', 1, 1, 1, 'ES', 'Project', 1, NULL, TRUE);

-- Semester 2 (Year 1, Sem 2) - Total 20 Credits
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8002', 'Ordinary Differential Equations and Vector Calculus', 1, 2, 2, 'BS', 'Theory', 4, NULL, TRUE),
('A8006', 'Applied Physics', 1, 2, 2, 'BS', 'Theory', 3, NULL, TRUE),
('A8204', 'Basic Electrical Engineering', 1, 2, 2, 'ES', 'Theory', 2, NULL, TRUE),
('A8505', 'Data Structures', 1, 2, 2, 'ES', 'Theory', 3, NULL, TRUE),
('A8506', 'Computer Organization', 1, 2, 2, 'ES', 'Theory', 2, NULL, TRUE),
('A8007', 'Applied Physics Laboratory', 1, 2, 2, 'BS', 'Laboratory', 1, NULL, TRUE),
('A8205', 'Basic Electrical Engineering Laboratory', 1, 2, 2, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8301', 'Engineering Workshop', 1, 2, 2, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8507', 'Data Structures Laboratory', 1, 2, 2, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8508', 'Python Programming Laboratory', 1, 2, 2, 'ES', 'Laboratory', 1, NULL, TRUE),
('A8026', 'Product Design and Development', 1, 2, 2, 'ES', 'Project', 1, NULL, TRUE);

-- Semester 3 (Year 2, Sem 1) - Total 20 Credits + Mandatory Non-Credit
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8013', 'Business Economics and Financial Analysis', 2, 1, 3, 'HS', 'Theory', 3, NULL, TRUE),
('A8509', 'Discrete Mathematical Structures', 2, 1, 3, 'ES', 'Theory', 3, NULL, TRUE),
('A8601', 'Object Oriented Programming', 2, 1, 3, 'PC', 'Theory', 3, NULL, TRUE),
('A8510', 'Operating Systems', 2, 1, 3, 'PC', 'Theory', 3, NULL, TRUE),
('A8511', 'Advanced Data Structures', 2, 1, 3, 'PC', 'Theory', 4, NULL, TRUE),
('A8602', 'Object Oriented Programming Laboratory', 2, 1, 3, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8512', 'Operating Systems Laboratory', 2, 1, 3, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8513', 'Advanced Data Structures Laboratory', 2, 1, 3, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8027', 'Technology Entrepreneurship', 2, 1, 3, 'PW', 'Project', 1, NULL, TRUE),
('A8032', 'Environmental Science and Technology', 2, 1, 3, 'MC', 'Non-Credit', 0, NULL, TRUE);

-- Semester 4 (Year 2, Sem 2) - Total 20 Credits + Mandatory Non-Credit
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8005', 'Computer Oriented Statistical Methods', 2, 2, 4, 'BS', 'Theory', 4, NULL, TRUE),
('A8514', 'Database Management Systems', 2, 2, 4, 'PC', 'Theory', 3, NULL, TRUE),
('A8515', 'Formal Languages and Automata Theory', 2, 2, 4, 'PC', 'Theory', 3, NULL, TRUE),
('A8516', 'Design and Analysis of Algorithms', 2, 2, 4, 'PC', 'Theory', 3, NULL, TRUE),
('A8702', 'Artificial Intelligence', 2, 2, 4, 'PC', 'Theory', 3, NULL, TRUE),
('A8517', 'Database Management Systems Laboratory', 2, 2, 4, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8518', 'Python for Data Analysis (Skill Development)', 2, 2, 4, 'PC', 'Skill', 1, NULL, TRUE),
('A8603', 'IoT Laboratory', 2, 2, 4, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8028', 'Community Driven Product Evaluation', 2, 2, 4, 'PW', 'Project', 1, NULL, TRUE),
('A8031', 'Gender Sensitization', 2, 2, 4, 'MC', 'Non-Credit', 0, NULL, TRUE),
('A8033', 'Universal Human Values 2: Understanding Harmony', 2, 2, 4, 'MC', 'Non-Credit', 0, NULL, TRUE);

-- Semester 5 (Year 3, Sem 1) - Total 20 Credits + Mandatory Non-Credit
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8519', 'Computer Networks', 3, 1, 5, 'PC', 'Theory', 3, NULL, TRUE),
('A8520', 'Software Engineering', 3, 1, 5, 'PC', 'Theory', 3, NULL, TRUE),
('A8604', 'Web Technologies', 3, 1, 5, 'PC', 'Theory', 3, NULL, TRUE),
('A8703', 'Machine Learning', 3, 1, 5, 'PC', 'Theory', 3, NULL, TRUE),
('A8605', 'Web Technologies Laboratory', 3, 1, 5, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8704', 'Machine Learning Laboratory', 3, 1, 5, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8606', 'Mobile Application Development (Skill Development)', 3, 1, 5, 'PC', 'Skill', 2, NULL, TRUE),
('A8525', 'RUST Programming (Skill Development)', 3, 1, 5, 'PC', 'Skill', 1, NULL, TRUE),
('A8035', 'Research Methodology', 3, 1, 5, 'MC', 'Non-Credit', 0, NULL, TRUE),
-- Professional Elective - I (4 selectable options)
('A8651', 'Ethical Hacking', 3, 1, 5, 'PE', 'Theory', 3, 'Professional Elective I', FALSE),
('A8851', 'Data Science for Engineers', 3, 1, 5, 'PE', 'Theory', 3, 'Professional Elective I', FALSE),
('A8662', 'Image Processing', 3, 1, 5, 'PE', 'Theory', 3, 'Professional Elective I', FALSE),
('A8657', 'Prompt Engineering', 3, 1, 5, 'PE', 'Theory', 3, 'Professional Elective I', FALSE);

-- Semester 6 (Year 3, Sem 2) - Total 20 Credits + Mandatory Non-Credit
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8522', 'Cloud Computing and Virtualization', 3, 2, 6, 'PC', 'Theory', 3, NULL, TRUE),
('A8607', 'Information Security', 3, 2, 6, 'PC', 'Theory', 3, NULL, TRUE),
('A8523', 'Compiler Design', 3, 2, 6, 'PC', 'Theory', 3, NULL, TRUE),
('A8524', 'Cloud Computing and Virtualization Laboratory', 3, 2, 6, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8612', 'Network and Information Security Laboratory', 3, 2, 6, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8012', 'Advanced English Communication Skills Laboratory', 3, 2, 6, 'HS', 'Laboratory', 1, NULL, TRUE),
('A8041', 'Mini-Project / Internship', 3, 2, 6, 'PW', 'Project', 2, NULL, TRUE),
('A8034', 'Indian Constitution', 3, 2, 6, 'MC', 'Non-Credit', 0, NULL, TRUE),
-- Professional Elective - II (4 selectable options)
('A8652', 'Cyber Security', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective II', FALSE),
('A8804', 'Data Analytics', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective II', FALSE),
('A8663', 'Computer Vision', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective II', FALSE),
('A8658', 'Robotic Process Automation', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective II', FALSE),
-- Professional Elective - III (4 selectable options)
('A8653', 'Web and Database Security', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective III', FALSE),
('A8707', 'Deep Learning', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective III', FALSE),
('A8664', 'Pattern Recognition', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective III', FALSE),
('A8659', 'Salesforce', 3, 2, 6, 'PE', 'Theory', 3, 'Professional Elective III', FALSE);

-- Semester 7 (Year 4, Sem 1) - Total 20 Credits
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8526', 'Data Mining', 4, 1, 7, 'PC', 'Theory', 3, NULL, TRUE),
('A8806', 'Big Data Analytics', 4, 1, 7, 'PC', 'Theory', 3, NULL, TRUE),
('A8528', 'Data Mining Laboratory', 4, 1, 7, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8807', 'Big Data Analytics Laboratory', 4, 1, 7, 'PC', 'Laboratory', 1, NULL, TRUE),
('A8042', 'Project Work Phase - I', 4, 1, 7, 'PW', 'Project', 3, NULL, TRUE),
-- Professional Elective - IV (4 selectable options)
('A8654', 'Cloud Security', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective IV', FALSE),
('A8706', 'Natural Language Processing', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective IV', FALSE),
('A8665', 'Video Processing Analytics', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective IV', FALSE),
('A8660', 'SAP ABAP', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective IV', FALSE),
-- Professional Elective - V (4 selectable options)
('A8655', 'IoT Security', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective V', FALSE),
('A8856', 'Web and Social Media Analytics', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective V', FALSE),
('A8666', 'Augmented Reality and Virtual Reality', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective V', FALSE),
('A8555', 'Edge Computing', 4, 1, 7, 'PE', 'Theory', 3, 'Professional Elective V', FALSE),
-- Open Elective - I Slot (Selection made from r22_open_electives pool)
('OE-I', 'Open Elective - I (Institutional Selection)', 4, 1, 7, 'OE', 'Theory', 3, 'Open Elective I', FALSE);

-- Semester 8 (Year 4, Sem 2) - Total 20 Credits
INSERT IGNORE INTO r22_curriculum_catalog (course_code, course_name, year, semester, global_semester, category, course_type, credits, elective_group, is_mandatory) VALUES
('A8043', 'Project Work Phase - II', 4, 2, 8, 'PW', 'Project', 11, NULL, TRUE),
-- Professional Elective - VI (4 selectable options)
('A8656', 'Blockchain Technology', 4, 2, 8, 'PE', 'Theory', 3, 'Professional Elective VI', FALSE),
('A8853', 'Optimization Techniques for Analytics', 4, 2, 8, 'PE', 'Theory', 3, 'Professional Elective VI', FALSE),
('A8667', 'Optical Character Recognition', 4, 2, 8, 'PE', 'Theory', 3, 'Professional Elective VI', FALSE),
('A8661', 'DevOps', 4, 2, 8, 'PE', 'Theory', 3, 'Professional Elective VI', FALSE),
-- Open Elective - II and III Slots
('OE-II', 'Open Elective - II (Institutional Selection)', 4, 2, 8, 'OE', 'Theory', 3, 'Open Elective II', FALSE),
('OE-III', 'Open Elective - III (Institutional Selection)', 4, 2, 8, 'OE', 'Theory', 3, 'Open Elective III', FALSE);

-- ============================================================================
-- OFFICIAL OPEN ELECTIVES POOL (All 41 courses from PDF Pages 12-13)
-- Available for selection in OE-I (Sem 7), OE-II (Sem 8), and OE-III (Sem 8)
-- ============================================================================
INSERT IGNORE INTO r22_open_electives (course_code, course_name, category, credits, offering_department) VALUES
('A8181', 'Smart Cities', 'OE', 3, 'Inter-Departmental / Open'),
('A8182', 'Disaster Management', 'OE', 3, 'Inter-Departmental / Open'),
('A8183', 'Environmental Pollution Management', 'OE', 3, 'Inter-Departmental / Open'),
('A8155', 'Green Building and Sustainability', 'OE', 3, 'Inter-Departmental / Open'),
('A8224', 'Electric Vehicles', 'OE', 3, 'Inter-Departmental / Open'),
('A8281', 'Solar Energy and Applications', 'OE', 3, 'Inter-Departmental / Open'),
('A8282', 'Energy Storage Systems', 'OE', 3, 'Inter-Departmental / Open'),
('A8283', 'Power Generation Systems', 'OE', 3, 'Inter-Departmental / Open'),
('A8381', 'Hybrid Vehicles', 'OE', 3, 'Inter-Departmental / Open'),
('A8382', 'Fundamentals of Robotics', 'OE', 3, 'Inter-Departmental / Open'),
('A8383', '3D Printing', 'OE', 3, 'Inter-Departmental / Open'),
('A8402', 'Digital Electronics', 'OE', 3, 'Inter-Departmental / Open'),
('A8481', 'Basic Electronics', 'OE', 3, 'Inter-Departmental / Open'),
('A8482', 'Principles of Communication Engineering', 'OE', 3, 'Inter-Departmental / Open'),
('A8483', 'Fundamentals of IoT', 'OE', 3, 'Inter-Departmental / Open'),
('A8484', 'Introduction to Embedded Systems', 'OE', 3, 'Inter-Departmental / Open'),
('A8510', 'Operating Systems', 'OE', 3, 'Inter-Departmental / Open'),
('A8514', 'Database Management Systems', 'OE', 3, 'Inter-Departmental / Open'),
('A8520', 'Software Engineering', 'OE', 3, 'Inter-Departmental / Open'),
('A8607', 'Information Security', 'OE', 3, 'Inter-Departmental / Open'),
('A8608', 'Java Programming', 'OE', 3, 'Inter-Departmental / Open'),
('A8651', 'Ethical Hacking', 'OE', 3, 'Inter-Departmental / Open'),
('A8652', 'Cyber Security', 'OE', 3, 'Inter-Departmental / Open'),
('A8656', 'Blockchain Technology', 'OE', 3, 'Inter-Departmental / Open'),
('A8658', 'Robotic Process Automation', 'OE', 3, 'Inter-Departmental / Open'),
('A8681', 'E-Commerce', 'OE', 3, 'Inter-Departmental / Open'),
('A8682', 'Full Stack Development', 'OE', 3, 'Inter-Departmental / Open'),
('A8702', 'Artificial Intelligence', 'OE', 3, 'Inter-Departmental / Open'),
('A8781', 'Computer Organization and Architecture', 'OE', 3, 'Inter-Departmental / Open'),
('A8851', 'Data Science for Engineers', 'OE', 3, 'Inter-Departmental / Open'),
('A8081', 'Mathematical Programming', 'OE', 3, 'Inter-Departmental / Open'),
('A8082', 'Transform Calculus', 'OE', 3, 'Inter-Departmental / Open'),
('A8083', 'Numerical Techniques', 'OE', 3, 'Inter-Departmental / Open'),
('A8084', 'Entrepreneurship Development', 'OE', 3, 'Inter-Departmental / Open'),
('A8085', 'Logistics and Supply Chain Management', 'OE', 3, 'Inter-Departmental / Open'),
('A8086', 'Management Science', 'OE', 3, 'Inter-Departmental / Open'),
('A8087', 'Human Resource Management', 'OE', 3, 'Inter-Departmental / Open'),
('A8088', 'Organizational Behaviour', 'OE', 3, 'Inter-Departmental / Open'),
('A8089', 'Intellectual Property Rights', 'OE', 3, 'Inter-Departmental / Open'),
('A8090', 'Professional Practice, Law & Ethics', 'OE', 3, 'Inter-Departmental / Open'),
('A8091', 'National Cadet Corps (NCC)', 'OE', 3, 'Inter-Departmental / Open');
