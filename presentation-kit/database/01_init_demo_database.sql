-- ============================================================================
-- 01_init_demo_database.sql
-- Presentation Kit: Student Course Registration Portal (Demo Environment)
-- ============================================================================
-- PURPOSE:
-- 1. Creates an isolated database named 'student_course_portal_demo'.
-- 2. Creates a dedicated, least-privilege demo MySQL account 'portal_demo'.
-- 3. Grants permissions STRICTLY on 'student_course_portal_demo'.
--
-- EXECUTION:
-- Run this script as MySQL administrator (e.g., 'root'):
--   mysql -u root -p < 01_init_demo_database.sql
-- ============================================================================

-- 1. Create isolated demo database
CREATE DATABASE IF NOT EXISTS `student_course_portal_demo`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. Create dedicated demo user with default demo password
-- Note: You may change 'DemoPortal#2026' to your own preferred demo password.
-- If you change it here, also update 'PORTAL_DB_PASSWORD' in 'config/portal-env.bat'.
CREATE USER IF NOT EXISTS 'portal_demo'@'localhost' IDENTIFIED BY 'DemoPortal#2026';
ALTER USER 'portal_demo'@'localhost' IDENTIFIED BY 'DemoPortal#2026';

-- Also allow connection from 127.0.0.1 (standard on Windows)
CREATE USER IF NOT EXISTS 'portal_demo'@'127.0.0.1' IDENTIFIED BY 'DemoPortal#2026';
ALTER USER 'portal_demo'@'127.0.0.1' IDENTIFIED BY 'DemoPortal#2026';

-- 3. Grant LEAST-PRIVILEGE access strictly to the demo database ONLY
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
  ON `student_course_portal_demo`.* TO 'portal_demo'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES
  ON `student_course_portal_demo`.* TO 'portal_demo'@'127.0.0.1';

FLUSH PRIVILEGES;

SELECT 'Demo database and portal_demo user initialized successfully.' AS Status;
