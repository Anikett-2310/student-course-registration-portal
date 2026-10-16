-- ============================================================================
-- PROPOSED R22 CURRICULUM SEED SCRIPT (PROPOSAL ONLY — DO NOT EXECUTE)
-- Database: student_course_portal
-- Authoritative extraction from VCE_BTech_IT_R22_Curriculum.pdf
-- Includes all 8 Semesters, 6 Professional Elective Groups, and all 41 Open Electives
-- ============================================================================

-- Semester 1 (Year 1, Sem 1) - Total 20 Credits
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
