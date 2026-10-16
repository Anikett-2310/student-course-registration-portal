package com.courseportal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CurriculumCatalogService.java
 * Encapsulates the authoritative VCE R22 B.Tech IT curriculum catalog.
 * Provides semester-aware course lookups, elective group groupings,
 * and credit metadata across all 4 years / 8 semesters, including all 41 Open Electives.
 */
public class CurriculumCatalogService {

    public static class CurriculumCourse {
        private final String code;
        private final String name;
        private final int year;
        private final int semester;
        private final int globalSemester;
        private final String category;
        private final String type;
        private final int credits;
        private final String electiveGroup;
        private final boolean mandatory;

        public CurriculumCourse(String code, String name, int year, int semester,
                                String category, String type, int credits,
                                String electiveGroup, boolean mandatory) {
            this.code = code;
            this.name = name;
            this.year = year;
            this.semester = semester;
            this.globalSemester = (year - 1) * 2 + semester;
            this.category = category;
            this.type = type;
            this.credits = credits;
            this.electiveGroup = electiveGroup;
            this.mandatory = mandatory;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getYear() { return year; }
        public int getSemester() { return semester; }
        public int getGlobalSemester() { return globalSemester; }
        public String getCategory() { return category; }
        public String getType() { return type; }
        public int getCredits() { return credits; }
        public String getElectiveGroup() { return electiveGroup; }
        public boolean isMandatory() { return mandatory; }
    }

    private static final List<CurriculumCourse> ALL_COURSES = new ArrayList<>();
    private static final Map<String, CurriculumCourse> CODE_MAP = new HashMap<>();
    private static final List<CurriculumCourse> OPEN_ELECTIVES = new ArrayList<>();
    private static final Map<String, CurriculumCourse> OE_MAP = new HashMap<>();

    static {
        // --- SEMESTER 1 (Year 1, Sem 1) ---
        add("A8001", "Matrices and Calculus", 1, 1, "BS", "Theory", 4, null, true);
        add("A8008", "Engineering Chemistry", 1, 1, "BS", "Theory", 3, null, true);
        add("A8010", "English for Skill Enhancement", 1, 1, "HS", "Theory", 2, null, true);
        add("A8402", "Digital Electronics", 1, 1, "ES", "Theory", 3, null, true);
        add("A8501", "Problem Solving through C", 1, 1, "ES", "Theory", 3, null, true);
        add("A8009", "Engineering Chemistry Laboratory", 1, 1, "BS", "Laboratory", 1, null, true);
        add("A8011", "English Language and Communication Skills Laboratory", 1, 1, "HS", "Laboratory", 1, null, true);
        add("A8502", "Problem Solving through C Laboratory", 1, 1, "ES", "Laboratory", 1, null, true);
        add("A8302", "Computer Aided Drawing", 1, 1, "ES", "Laboratory", 1, null, true);
        add("A8025", "Community Centered Design Thinking", 1, 1, "ES", "Project", 1, null, true);

        // --- SEMESTER 2 (Year 1, Sem 2) ---
        add("A8002", "Ordinary Differential Equations and Vector Calculus", 1, 2, "BS", "Theory", 4, null, true);
        add("A8006", "Applied Physics", 1, 2, "BS", "Theory", 3, null, true);
        add("A8204", "Basic Electrical Engineering", 1, 2, "ES", "Theory", 2, null, true);
        add("A8505", "Data Structures", 1, 2, "ES", "Theory", 3, null, true);
        add("A8506", "Computer Organization", 1, 2, "ES", "Theory", 2, null, true);
        add("A8007", "Applied Physics Laboratory", 1, 2, "BS", "Laboratory", 1, null, true);
        add("A8205", "Basic Electrical Engineering Laboratory", 1, 2, "ES", "Laboratory", 1, null, true);
        add("A8301", "Engineering Workshop", 1, 2, "ES", "Laboratory", 1, null, true);
        add("A8507", "Data Structures Laboratory", 1, 2, "ES", "Laboratory", 1, null, true);
        add("A8508", "Python Programming Laboratory", 1, 2, "ES", "Laboratory", 1, null, true);
        add("A8026", "Product Design and Development", 1, 2, "ES", "Project", 1, null, true);

        // --- SEMESTER 3 (Year 2, Sem 1) ---
        add("A8013", "Business Economics and Financial Analysis", 2, 1, "HS", "Theory", 3, null, true);
        add("A8509", "Discrete Mathematical Structures", 2, 1, "ES", "Theory", 3, null, true);
        add("A8601", "Object Oriented Programming", 2, 1, "PC", "Theory", 3, null, true);
        add("A8510", "Operating Systems", 2, 1, "PC", "Theory", 3, null, true);
        add("A8511", "Advanced Data Structures", 2, 1, "PC", "Theory", 4, null, true);
        add("A8602", "Object Oriented Programming Laboratory", 2, 1, "PC", "Laboratory", 1, null, true);
        add("A8512", "Operating Systems Laboratory", 2, 1, "PC", "Laboratory", 1, null, true);
        add("A8513", "Advanced Data Structures Laboratory", 2, 1, "PC", "Laboratory", 1, null, true);
        add("A8027", "Technology Entrepreneurship", 2, 1, "PW", "Project", 1, null, true);
        add("A8032", "Environmental Science and Technology", 2, 1, "MC", "Non-Credit", 0, null, true);

        // --- SEMESTER 4 (Year 2, Sem 2) ---
        add("A8005", "Computer Oriented Statistical Methods", 2, 2, "BS", "Theory", 4, null, true);
        add("A8514", "Database Management Systems", 2, 2, "PC", "Theory", 3, null, true);
        add("A8515", "Formal Languages and Automata Theory", 2, 2, "PC", "Theory", 3, null, true);
        add("A8516", "Design and Analysis of Algorithms", 2, 2, "PC", "Theory", 3, null, true);
        add("A8702", "Artificial Intelligence", 2, 2, "PC", "Theory", 3, null, true);
        add("A8517", "Database Management Systems Laboratory", 2, 2, "PC", "Laboratory", 1, null, true);
        add("A8518", "Python for Data Analysis (Skill Development)", 2, 2, "PC", "Skill", 1, null, true);
        add("A8603", "IoT Laboratory", 2, 2, "PC", "Laboratory", 1, null, true);
        add("A8028", "Community Driven Product Evaluation", 2, 2, "PW", "Project", 1, null, true);
        add("A8031", "Gender Sensitization", 2, 2, "MC", "Non-Credit", 0, null, true);
        add("A8033", "Universal Human Values 2: Understanding Harmony", 2, 2, "MC", "Non-Credit", 0, null, true);

        // --- SEMESTER 5 (Year 3, Sem 1) ---
        add("A8519", "Computer Networks", 3, 1, "PC", "Theory", 3, null, true);
        add("A8520", "Software Engineering", 3, 1, "PC", "Theory", 3, null, true);
        add("A8604", "Web Technologies", 3, 1, "PC", "Theory", 3, null, true);
        add("A8703", "Machine Learning", 3, 1, "PC", "Theory", 3, null, true);
        add("A8605", "Web Technologies Laboratory", 3, 1, "PC", "Laboratory", 1, null, true);
        add("A8704", "Machine Learning Laboratory", 3, 1, "PC", "Laboratory", 1, null, true);
        add("A8606", "Mobile Application Development (Skill Dev)", 3, 1, "PC", "Skill", 2, null, true);
        add("A8525", "RUST Programming (Skill Development)", 3, 1, "PC", "Skill", 1, null, true);
        add("A8035", "Research Methodology", 3, 1, "MC", "Non-Credit", 0, null, true);
        // Professional Elective - I (4 selectable options)
        add("A8651", "Ethical Hacking", 3, 1, "PE", "Theory", 3, "Professional Elective I", false);
        add("A8851", "Data Science for Engineers", 3, 1, "PE", "Theory", 3, "Professional Elective I", false);
        add("A8662", "Image Processing", 3, 1, "PE", "Theory", 3, "Professional Elective I", false);
        add("A8657", "Prompt Engineering", 3, 1, "PE", "Theory", 3, "Professional Elective I", false);

        // --- SEMESTER 6 (Year 3, Sem 2) ---
        add("A8522", "Cloud Computing and Virtualization", 3, 2, "PC", "Theory", 3, null, true);
        add("A8607", "Information Security", 3, 2, "PC", "Theory", 3, null, true);
        add("A8523", "Compiler Design", 3, 2, "PC", "Theory", 3, null, true);
        add("A8524", "Cloud Computing and Virtualization Laboratory", 3, 2, "PC", "Laboratory", 1, null, true);
        add("A8612", "Network and Information Security Laboratory", 3, 2, "PC", "Laboratory", 1, null, true);
        add("A8012", "Advanced English Communication Skills Laboratory", 3, 2, "HS", "Laboratory", 1, null, true);
        add("A8041", "Mini-Project / Internship", 3, 2, "PW", "Project", 2, null, true);
        add("A8034", "Indian Constitution", 3, 2, "MC", "Non-Credit", 0, null, true);
        // Professional Elective - II (4 selectable options)
        add("A8652", "Cyber Security", 3, 2, "PE", "Theory", 3, "Professional Elective II", false);
        add("A8804", "Data Analytics", 3, 2, "PE", "Theory", 3, "Professional Elective II", false);
        add("A8663", "Computer Vision", 3, 2, "PE", "Theory", 3, "Professional Elective II", false);
        add("A8658", "Robotic Process Automation", 3, 2, "PE", "Theory", 3, "Professional Elective II", false);
        // Professional Elective - III (4 selectable options)
        add("A8653", "Web and Database Security", 3, 2, "PE", "Theory", 3, "Professional Elective III", false);
        add("A8707", "Deep Learning", 3, 2, "PE", "Theory", 3, "Professional Elective III", false);
        add("A8664", "Pattern Recognition", 3, 2, "PE", "Theory", 3, "Professional Elective III", false);
        add("A8659", "Salesforce", 3, 2, "PE", "Theory", 3, "Professional Elective III", false);

        // --- SEMESTER 7 (Year 4, Sem 1) ---
        add("A8526", "Data Mining", 4, 1, "PC", "Theory", 3, null, true);
        add("A8806", "Big Data Analytics", 4, 1, "PC", "Theory", 3, null, true);
        add("A8528", "Data Mining Laboratory", 4, 1, "PC", "Laboratory", 1, null, true);
        add("A8807", "Big Data Analytics Laboratory", 4, 1, "PC", "Laboratory", 1, null, true);
        add("A8042", "Project Work Phase - I", 4, 1, "PW", "Project", 3, null, true);
        // Professional Elective - IV (4 selectable options)
        add("A8654", "Cloud Security", 4, 1, "PE", "Theory", 3, "Professional Elective IV", false);
        add("A8706", "Natural Language Processing", 4, 1, "PE", "Theory", 3, "Professional Elective IV", false);
        add("A8665", "Video Processing Analytics", 4, 1, "PE", "Theory", 3, "Professional Elective IV", false);
        add("A8660", "SAP ABAP", 4, 1, "PE", "Theory", 3, "Professional Elective IV", false);
        // Professional Elective - V (4 selectable options)
        add("A8655", "IoT Security", 4, 1, "PE", "Theory", 3, "Professional Elective V", false);
        add("A8856", "Web and Social Media Analytics", 4, 1, "PE", "Theory", 3, "Professional Elective V", false);
        add("A8666", "Augmented Reality and Virtual Reality", 4, 1, "PE", "Theory", 3, "Professional Elective V", false);
        add("A8555", "Edge Computing", 4, 1, "PE", "Theory", 3, "Professional Elective V", false);
        // Open Elective - I Slot
        add("OE-I", "Open Elective - I", 4, 1, "OE", "Theory", 3, "Open Elective I", false);

        // --- SEMESTER 8 (Year 4, Sem 2) ---
        add("A8043", "Project Work Phase - II", 4, 2, "PW", "Project", 11, null, true);
        // Professional Elective - VI (4 selectable options)
        add("A8656", "Blockchain Technology", 4, 2, "PE", "Theory", 3, "Professional Elective VI", false);
        add("A8853", "Optimization Techniques for Analytics", 4, 2, "PE", "Theory", 3, "Professional Elective VI", false);
        add("A8667", "Optical Character Recognition", 4, 2, "PE", "Theory", 3, "Professional Elective VI", false);
        add("A8661", "DevOps", 4, 2, "PE", "Theory", 3, "Professional Elective VI", false);
        // Open Elective - II and III Slots
        add("OE-II", "Open Elective - II", 4, 2, "OE", "Theory", 3, "Open Elective II", false);
        add("OE-III", "Open Elective - III", 4, 2, "OE", "Theory", 3, "Open Elective III", false);

        // --- ALL 41 OPEN ELECTIVES (From Official PDF Pages 12-13) ---
        addOE("A8181", "Smart Cities");
        addOE("A8182", "Disaster Management");
        addOE("A8183", "Environmental Pollution Management");
        addOE("A8155", "Green Building and Sustainability");
        addOE("A8224", "Electric Vehicles");
        addOE("A8281", "Solar Energy and Applications");
        addOE("A8282", "Energy Storage Systems");
        addOE("A8283", "Power Generation Systems");
        addOE("A8381", "Hybrid Vehicles");
        addOE("A8382", "Fundamentals of Robotics");
        addOE("A8383", "3D Printing");
        addOE("A8402", "Digital Electronics");
        addOE("A8481", "Basic Electronics");
        addOE("A8482", "Principles of Communication Engineering");
        addOE("A8483", "Fundamentals of IoT");
        addOE("A8484", "Introduction to Embedded Systems");
        addOE("A8510", "Operating Systems");
        addOE("A8514", "Database Management Systems");
        addOE("A8520", "Software Engineering");
        addOE("A8607", "Information Security");
        addOE("A8608", "Java Programming");
        addOE("A8651", "Ethical Hacking");
        addOE("A8652", "Cyber Security");
        addOE("A8656", "Blockchain Technology");
        addOE("A8658", "Robotic Process Automation");
        addOE("A8681", "E-Commerce");
        addOE("A8682", "Full Stack Development");
        addOE("A8702", "Artificial Intelligence");
        addOE("A8781", "Computer Organization and Architecture");
        addOE("A8851", "Data Science for Engineers");
        addOE("A8081", "Mathematical Programming");
        addOE("A8082", "Transform Calculus");
        addOE("A8083", "Numerical Techniques");
        addOE("A8084", "Entrepreneurship Development");
        addOE("A8085", "Logistics and Supply Chain Management");
        addOE("A8086", "Management Science");
        addOE("A8087", "Human Resource Management");
        addOE("A8088", "Organizational Behaviour");
        addOE("A8089", "Intellectual Property Rights");
        addOE("A8090", "Professional Practice, Law & Ethics");
        addOE("A8091", "National Cadet Corps (NCC)");
    }

    private static void add(String code, String name, int year, int sem,
                            String cat, String type, int credits,
                            String elGroup, boolean mandatory) {
        CurriculumCourse c = new CurriculumCourse(code, name, year, sem, cat, type, credits, elGroup, mandatory);
        ALL_COURSES.add(c);
        CODE_MAP.put(code, c);
    }

    private static void addOE(String code, String name) {
        CurriculumCourse oe = new CurriculumCourse(code, name, 4, 1, "OE", "Theory", 3, "Open Elective", false);
        OPEN_ELECTIVES.add(oe);
        OE_MAP.put(code, oe);
        if (!CODE_MAP.containsKey(code)) {
            CODE_MAP.put(code, oe);
        }
    }

    public static List<CurriculumCourse> getAllCourses() {
        return Collections.unmodifiableList(ALL_COURSES);
    }

    public static List<CurriculumCourse> getOpenElectives() {
        return Collections.unmodifiableList(OPEN_ELECTIVES);
    }

    public static List<CurriculumCourse> getCoursesForSemester(int year, int semester) {
        List<CurriculumCourse> result = new ArrayList<>();
        for (CurriculumCourse c : ALL_COURSES) {
            if (c.getYear() == year && c.getSemester() == semester) {
                result.add(c);
            }
        }
        return result;
    }

    public static CurriculumCourse getCourse(String code) {
        CurriculumCourse c = CODE_MAP.get(code);
        if (c == null) {
            c = OE_MAP.get(code);
        }
        return c;
    }

    public static String getElectiveGroup(String code) {
        CurriculumCourse c = getCourse(code);
        return (c != null) ? c.getElectiveGroup() : null;
    }

    public static boolean isOpenElective(String code) {
        return OE_MAP.containsKey(code);
    }
}
