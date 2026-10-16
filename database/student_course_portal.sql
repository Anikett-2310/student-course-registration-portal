-- ============================================================
-- Database: student_course_portal
-- Course-End Project: Student Course Registration Portal
-- Web Technologies Laboratory - 3rd Year B.Tech IT
-- ============================================================

CREATE DATABASE IF NOT EXISTS student_course_portal;
USE student_course_portal;

-- ------------------------------------------------------------
-- Table 1: students
-- Stores basic student profile information and login credentials
-- ------------------------------------------------------------
DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS admin;

CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    branch VARCHAR(50) NOT NULL,
    year INT NOT NULL,
    semester INT NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- ------------------------------------------------------------
-- Table 2: courses
-- Stores available curriculum courses with faculty and credits
-- ------------------------------------------------------------
CREATE TABLE courses (
    course_code VARCHAR(10) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    faculty VARCHAR(100) NOT NULL,
    credits INT NOT NULL
);

-- ------------------------------------------------------------
-- Table 3: registrations
-- Maps student enrollments to courses with foreign key constraints
-- ------------------------------------------------------------
CREATE TABLE registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(10) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE,
    UNIQUE KEY unique_student_course (student_id, course_code)
);

-- ------------------------------------------------------------
-- Table 4: admin
-- Stores administrator login credentials
-- ------------------------------------------------------------
CREATE TABLE admin (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- 1. Sample Students
INSERT INTO students (student_id, name, password, branch, year, semester, email) VALUES
('24881A1203', 'Rahul Sharma', 'student123', 'Information Technology', 3, 1, 'rahul.24881a1203@college.edu'),
('24881A1204', 'Priya Patel', 'student123', 'Information Technology', 3, 1, 'priya.24881a1204@college.edu');

-- 2. Prescribed Lab Courses (Total 12 credits available)
INSERT INTO courses (course_code, course_name, faculty, credits) VALUES
('A8506', 'Web Technologies', 'Ms. B. Swapna', 3),
('A8520', 'Software Engineering', 'Dr. Ravi Kumar', 2),
('A8704', 'Machine Learning', 'Dr. B. K. Madhavi', 3),
('A8851', 'Data Science', 'Dr. Sreenivasulu Gogula', 3),
('A8519', 'Computer Networks', 'Ms. Sumaiya Shaikh', 1);

-- 3. Administrator Account
INSERT INTO admin (username, password) VALUES
('admin', 'admin123');

-- 4. Sample initial registration for 24881A1203 (Optional demo record)
INSERT INTO registrations (student_id, course_code) VALUES
('24881A1203', 'A8506');
