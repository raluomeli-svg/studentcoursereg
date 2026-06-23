package com.nimet.rcrs.repository;

import com.nimet.rcrs.exception.RegistrationException;
import com.nimet.rcrs.model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DataStore {

    // userId → Student  (fast lookup for auth and direct access)
    private final Map<String, Student> studentsByUserId = new HashMap<>();
    // studentId (NMS/YYYY/NNNN) → Student  (admin lookup by student number)
    private final Map<String, Student> studentsByStudentId = new HashMap<>();
    // year → highest sequence number issued that year, for ID generation
    private final Map<Integer, Integer> yearCounters = new HashMap<>();

    // userId → Admin
    private final Map<String, Admin> admins = new HashMap<>();

    // courseCode → Course
    private final Map<String, Course> courses = new LinkedHashMap<>();

    // studentId → list of registrations
    private final Map<String, List<Registration>> registrations = new HashMap<>();

    // registrationId → list of results (one per course in that registration)
    private final Map<String, List<Result>> resultsByRegId = new HashMap<>();

    public DataStore() {
        initializeCourses();
        seedDefaultUsers();
        seedDemoResults();
    }

    // -------------------------------------------------------------------------
    // Student
    // -------------------------------------------------------------------------

    public void saveStudent(Student s) {
        studentsByUserId.put(s.getUserId(), s);
        studentsByStudentId.put(s.getStudentId(), s);
        // Keep yearCounters consistent with any student saved
        String sid = s.getStudentId();
        if (sid != null && sid.matches("NMS/\\d{4}/\\d{4}")) {
            int year = Integer.parseInt(sid.split("/")[1]);
            int seq  = Integer.parseInt(sid.split("/")[2]);
            yearCounters.merge(year, seq, Math::max);
        }
    }

    // Generates the next available NMS/YYYY/NNNN student ID for the given admission year
    public String generateNextStudentId(int admissionYear) {
        int next = yearCounters.getOrDefault(admissionYear, 0) + 1;
        yearCounters.put(admissionYear, next);
        return String.format("NMS/%d/%04d", admissionYear, next);
    }

    // Lookup by NMS/YYYY/NNNN — used by Admin.getRegistrationData()
    public Student loadStudent(String studentId) {
        return studentsByStudentId.get(studentId);
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(studentsByUserId.values());
    }

    // -------------------------------------------------------------------------
    // Course
    // -------------------------------------------------------------------------

    public void saveCourse(Course c) {
        courses.put(c.getCourseCode(), c);
    }

    public Course loadCourse(String code) {
        return courses.get(code);
    }

    public void deleteCourse(String code) {
        courses.remove(code);
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses.values());
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    public void saveRegistration(Registration r) {
        registrations
                .computeIfAbsent(r.getStudent().getStudentId(), k -> new ArrayList<>())
                .add(r);
    }

    // No-op for in-memory store; present as an extension point for file/DB persistence
    public void loadRegistration() { }

    public List<Registration> findRegistration(Student s) {
        return registrations.getOrDefault(s.getStudentId(), Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // Results
    // -------------------------------------------------------------------------

    public void saveResults(String registrationId, List<Result> results) {
        resultsByRegId.put(registrationId, new ArrayList<>(results));
    }

    public List<Result> findResults(String registrationId) {
        return resultsByRegId.getOrDefault(registrationId, Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // Auth support — finds any user (Student or Admin) by their login userId
    // -------------------------------------------------------------------------

    public User findUserByLoginId(String userId) {
        if (studentsByUserId.containsKey(userId)) return studentsByUserId.get(userId);
        if (admins.containsKey(userId))           return admins.get(userId);
        return null;
    }

    // -------------------------------------------------------------------------
    // Seed data — NiMet course catalogue (74 courses across BIPMT / MFC / BIPM)
    // -------------------------------------------------------------------------

    private void initializeCourses() {

        // ── BIPMT  Year 1 · Semester 1 ──────────────────────────────────────
        add("BIP110", "Meteorological Observations, Coding & Plotting I", Programme.BIPMT, 1, 1, 4);
        add("BIP111", "General Physics",                                   Programme.BIPMT, 1, 1, 3);
        add("BIP112", "General Studies",                                   Programme.BIPMT, 1, 1, 2);
        add("BIP113", "General Statistics",                                Programme.BIPMT, 1, 1, 3);
        add("BIP114", "General Meteorology",                               Programme.BIPMT, 1, 1, 3);
        add("BIP115", "Agrometeorology",                                   Programme.BIPMT, 1, 1, 4);
        add("BIP116", "General Mathematics",                               Programme.BIPMT, 1, 1, 3);
        add("BIP117", "Introduction to Computer Studies",                  Programme.BIPMT, 1, 1, 2);

        // ── BIPMT  Year 1 · Semester 2 ──────────────────────────────────────
        add("BIP120", "Upper Air Observations",                            Programme.BIPMT, 1, 2, 3);
        add("BIP121", "Meteorological Instrumentation",                    Programme.BIPMT, 1, 2, 3);
        add("BIP122", "Vector Analysis",                                   Programme.BIPMT, 1, 2, 3);
        add("BIP123", "Physical Meteorology",                              Programme.BIPMT, 1, 2, 3);
        add("BIP124", "Climatology",                                       Programme.BIPMT, 1, 2, 3);
        add("BIP125", "Hydrometeorology",                                  Programme.BIPMT, 1, 2, 3);
        add("BIP126", "Remote Sensing & GIS",                              Programme.BIPMT, 1, 2, 3);
        add("BIP127", "Aeronautical Meteorology I",                        Programme.BIPMT, 1, 2, 3);

        // ── BIPMT  Year 2 · Semester 1 ──────────────────────────────────────
        add("BIP210", "Aeronautical Meteorology II",                       Programme.BIPMT, 2, 1, 3);
        add("BIP211", "Synoptic Meteorology & Weather Analysis",           Programme.BIPMT, 2, 1, 4);
        add("BIP212", "Differential Equation",                             Programme.BIPMT, 2, 1, 3);
        add("BIP213", "Research Methodology and Applications",             Programme.BIPMT, 2, 1, 3);
        add("BIP214", "Satellite Meteorology",                             Programme.BIPMT, 2, 1, 3);
        add("BIP215", "Environmental Science",                             Programme.BIPMT, 2, 1, 3);
        add("BIP216", "Atmospheric Thermodynamics",                        Programme.BIPMT, 2, 1, 3);
        add("BIP217", "Field Trip",                                        Programme.BIPMT, 2, 1, 3);

        // ── BIPMT  Year 2 · Semester 2 ──────────────────────────────────────
        add("BIP220", "Dynamic Meteorology",                               Programme.BIPMT, 2, 2, 3);
        add("BIP221", "Climate Change",                                    Programme.BIPMT, 2, 2, 2);
        add("BIP222", "Meteorological Observations, Coding & Plotting II", Programme.BIPMT, 2, 2, 3);
        add("BIP223", "Marine Meteorology",                                Programme.BIPMT, 2, 2, 3);
        add("BIP224", "Climatological Returns",                            Programme.BIPMT, 2, 2, 3);
        add("BIP225", "Oral Examination",                                  Programme.BIPMT, 2, 2, 2);
        add("BIP226", "On the Job Training (OJT)",                         Programme.BIPMT, 2, 2, 3);
        add("BIP227", "Project",                                           Programme.BIPMT, 2, 2, 6);

        // ── MFC  Year 1 · Semester 1 ─────────────────────────────────────────
        add("BIP310", "Physical Meteorology",                              Programme.MFC,   1, 1, 3);
        add("BIP311", "Agricultural Meteorology",                          Programme.MFC,   1, 1, 3);
        add("BIP312", "Aeronautical Meteorology II",                       Programme.MFC,   1, 1, 3);
        add("BIP313", "Atmospheric Thermodynamics",                        Programme.MFC,   1, 1, 3);
        add("BIP314", "Biometeorology",                                    Programme.MFC,   1, 1, 3);
        add("BIP315", "Climatology",                                       Programme.MFC,   1, 1, 3);
        add("BIP316", "Introduction to Data Analysis",                     Programme.MFC,   1, 1, 3);
        add("BIP317", "Vector Analysis",                                   Programme.MFC,   1, 1, 2);
        add("BIP318", "Differential Equation",                             Programme.MFC,   1, 1, 3);
        add("BIP319", "Research Methodology",                              Programme.MFC,   1, 1, 3);
        add("BIP320", "Marine Meteorology II",                             Programme.MFC,   1, 1, 3);
        add("BIP330", "On the Job Training (OJT)",                         Programme.MFC,   1, 1, 3);

        // ── MFC  Year 1 · Semester 2 ─────────────────────────────────────────
        add("BIP321", "Dynamic Meteorology",                               Programme.MFC,   1, 2, 3);
        add("BIP322", "Synoptic Meteorology and Weather Analysis",         Programme.MFC,   1, 2, 3);
        add("BIP323", "Hydrological Meteorology",                          Programme.MFC,   1, 2, 2);
        add("BIP324", "Satellite Meteorology",                             Programme.MFC,   1, 2, 3);
        add("BIP325", "Military Meteorology",                              Programme.MFC,   1, 2, 2);
        add("BIP326", "Urban Climatology",                                 Programme.MFC,   1, 2, 3);
        add("BIP327", "Introduction to Numerical Weather Prediction",      Programme.MFC,   1, 2, 2);
        add("BIP328", "Applied Meteorology",                               Programme.MFC,   1, 2, 2);
        add("BIP329", "Field Trip",                                        Programme.MFC,   1, 2, 2);
        add("BIP331", "Student Project",                                   Programme.MFC,   1, 2, 6);

        // ── BIPM  Year 1 · Semester 1 ────────────────────────────────────────
        add("BIP410", "Upper Air Observations",                            Programme.BIPM,  1, 1, 3);
        add("BIP411", "Meteorological Observations, Coding & Plotting",    Programme.BIPM,  1, 1, 4);
        add("BIP412", "Aeronautic Meteorology I",                          Programme.BIPM,  1, 1, 3);
        add("BIP413", "Research Methodology",                              Programme.BIPM,  1, 1, 3);
        add("BIP414", "Climatology",                                       Programme.BIPM,  1, 1, 3);
        add("BIP415", "Physical Meteorology",                              Programme.BIPM,  1, 1, 3);
        add("BIP416", "Agricultural Meteorology",                          Programme.BIPM,  1, 1, 4);
        add("BIP417", "Remote Sensing / Satellite Meteorology",            Programme.BIPM,  1, 1, 0); // units TBC
        add("BIP418", "Marine Meteorology",                                Programme.BIPM,  1, 1, 3);
        add("BIP419", "Atmospheric Thermodynamics",                        Programme.BIPM,  1, 1, 3);

        // ── BIPM  Year 1 · Semester 2 ────────────────────────────────────────
        add("BIP420", "Climate Change Sciences",                           Programme.BIPM,  1, 2, 2);
        add("BIP421", "GIS Applications in Meteorology",                   Programme.BIPM,  1, 2, 3);
        add("BIP422", "Synoptic Meteorology & Weather Analysis",           Programme.BIPM,  1, 2, 0); // units TBC
        add("BIP423", "Aeronautical Meteorology II",                       Programme.BIPM,  1, 2, 3);
        add("BIP424", "Hydrometeorology",                                  Programme.BIPM,  1, 2, 3);
        add("BIP425", "Dynamic Meteorology",                               Programme.BIPM,  1, 2, 3);
        add("BIP426", "Applied Meteorology",                               Programme.BIPM,  1, 2, 3);
        add("BIP427", "Statistical Applications in Meteorology",           Programme.BIPM,  1, 2, 0); // units TBC
        add("BIP428", "On the Job Training (OJT)",                         Programme.BIPM,  1, 2, 3);
        add("BIP429", "Project",                                           Programme.BIPM,  1, 2, 6);
    }

    private void add(String code, String title, Programme prog, int level, int sem, int units) {
        Course c = new Course(code, title, prog, level, sem, units);
        courses.put(code, c);
    }

    // -------------------------------------------------------------------------
    // Seed data — default users
    // Default credentials:  admin01 / admin123  |  NMS/2024/0001 / student123
    // -------------------------------------------------------------------------

    private void seedDefaultUsers() {
        Admin sysAdmin = new Admin(
                "admin01", "System Administrator", sha256("admin123"), "STAFF001");
        admins.put(sysAdmin.getUserId(), sysAdmin);

        Student demo = new Student(
                "NMS/2024/0001", "Demo Student", sha256("student123"),
                "NMS/2024/0001", Programme.BIPMT, 2024, 1, 1,
                "1 Meteorology Close, Oshodi, Lagos",
                "demo.student@nimet.gov.ng",
                "+234 801 234 5678");
        saveStudent(demo);
    }

    // Seeds a submitted registration + results for the demo student so the UI is not empty.
    private void seedDemoResults() {
        Student demo = studentsByStudentId.get("NMS/2024/0001");
        if (demo == null) return;

        // Build a submitted registration for Year-1 Semester-1
        Registration reg = new Registration(demo, "2024/1");
        String[] bip1s1 = {"BIP110","BIP111","BIP112","BIP113","BIP114","BIP115","BIP116","BIP117"};
        for (String code : bip1s1) {
            Course c = courses.get(code);
            if (c != null) reg.addCourse(c);
        }
        try { reg.submit(); } catch (RegistrationException ignored) {}
        saveRegistration(reg);

        // Seed realistic CA + exam scores for each course
        double[][] scores = {
            {32, 48}, {28, 42}, {35, 52}, {30, 46},
            {26, 40}, {33, 50}, {29, 44}, {31, 47}
        };
        List<Course> courseList = new ArrayList<>(reg.getSelectedCourses());
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < courseList.size(); i++) {
            results.add(new Result(reg.getRegistrationId(), courseList.get(i),
                    scores[i][0], scores[i][1]));
        }
        saveResults(reg.getRegistrationId(), results);
    }

    // SHA-256 utility — mirrors the algorithm in AuthenticationService.
    // Public so Admin can hash a temp password without depending on AuthenticationService.
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
