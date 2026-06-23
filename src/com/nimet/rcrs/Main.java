package com.nimet.rcrs;

import com.nimet.rcrs.exception.RegistrationException;
import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private static DataStore dataStore;
    private static Catalogue catalogue;
    private static AuthenticationService authService;
    private static final Scanner scanner = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        bootstrap();

        while (true) {
            User user = loginPrompt();
            if (user == null) {
                print("\nToo many failed login attempts. Goodbye.");
                break;
            }
            if (user instanceof Student) {
                Student s = (Student) user;
                if (s.isTemporaryPassword()) {
                    print("  *** You are using a temporary password. Please change it. ***\n");
                    changePasswordFlow(s);
                }
                studentMenu(s);
            } else if (user instanceof Admin) {
                adminMenu((Admin) user);
            }
        }

        scanner.close();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bootstrap
    // ─────────────────────────────────────────────────────────────────────────

    private static void bootstrap() {
        dataStore  = new DataStore();
        catalogue  = new Catalogue();
        authService = new AuthenticationService(dataStore, catalogue);

        // Seed the Catalogue directly from DataStore (no user needed at startup)
        List<Course> courses = dataStore.getAllCourses();
        for (Course c : courses) catalogue.addCourse(c);

        printBanner();
        print("System ready. " + courses.size() + " courses loaded.\n");
        print("  Default credentials:");
        print("    Admin   — ID: admin01          Password: admin123");
        print("    Student — ID: NMS/2024/0001    Password: student123\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────────────────────────────

    private static User loginPrompt() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            printDivider("Login");
            String userId   = prompt("  User ID  : ").trim();
            String password = prompt("  Password : ").trim();

            User user = authService.authenticate(userId, password);
            if (user != null) {
                print("\nWelcome, " + user.getFullName() + "!\n");
                return user;
            }

            int remaining = MAX_LOGIN_ATTEMPTS - attempt;
            if (remaining > 0)
                print("  Invalid credentials. " + remaining + " attempt(s) remaining.\n");
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Student menu
    // ─────────────────────────────────────────────────────────────────────────

    private static void studentMenu(Student student) {
        boolean active = true;
        while (active) {
            printDivider("Student Menu");
            print("  " + student);
            print("  [1] View available courses");
            print("  [2] Register courses");
            print("  [3] View my registrations");
            print("  [4] View profile");
            print("  [5] Change password");
            print("  [0] Logout");

            switch (readChoice()) {
                case 1: viewAvailableCourses(student); break;
                case 2: registerCourses(student);      break;
                case 3: viewRegistrations(student);    break;
                case 4: viewProfile(student);          break;
                case 5: changePasswordFlow(student);   break;
                case 0:
                    authService.endSession();
                    print("Logged out.\n");
                    active = false;
                    break;
                default: print("  Invalid option.\n");
            }
        }
    }

    private static void viewProfile(Student student) {
        printDivider("My Profile");
        print(student.viewProfile());
        print("");
    }

    private static void changePasswordFlow(Student student) {
        printDivider("Change Password");
        String current = prompt("  Current password : ").trim();
        String newPwd  = prompt("  New password     : ").trim();
        String confirm = prompt("  Confirm password : ").trim();

        if (!newPwd.equals(confirm)) {
            print("  Passwords do not match. Cancelled.\n");
            return;
        }
        if (newPwd.length() < 6) {
            print("  Password must be at least 6 characters.\n");
            return;
        }
        if (authService.changePassword(student, current, newPwd)) {
            print("  Password changed successfully.\n");
        } else {
            print("  Current password is incorrect. Cancelled.\n");
        }
    }

    private static void viewAvailableCourses(Student student) {
        List<Course> courses = student.viewAvailableCourses();
        printDivider("Available Courses — Year " + student.getCurrentYear()
                + " · Sem " + student.getCurrentSemester());
        if (courses.isEmpty()) {
            print("  No courses found for your current level and semester.\n");
        } else {
            printCourseTable(courses);
        }
        print("");
    }

    private static void registerCourses(Student student) {
        List<Course> available = student.viewAvailableCourses();
        if (available.isEmpty()) {
            print("\n  No available courses for your current semester.\n");
            return;
        }

        Registration reg = student.submitRegistration();
        boolean active   = true;

        while (active) {
            printDivider("Register — Semester " + reg.getSemester());
            print("  Selection : " + reg.getSelectedCourses().size() + " course(s)"
                    + "   (min " + Registration.MIN_COURSES
                    + " · max " + Registration.MAX_COURSES + ")");
            print("  [1] Add a course");
            print("  [2] Remove a course");
            print("  [3] View current selection");
            print("  [4] Submit registration");
            print("  [0] Cancel");

            switch (readChoice()) {
                case 1: addCourseToReg(reg, available);    break;
                case 2: removeCourseFromReg(reg);          break;
                case 3: viewCurrentSelection(reg);         break;
                case 4:
                    if (submitRegistration(reg)) {
                        dataStore.saveRegistration(reg);
                        active = false;
                    }
                    break;
                case 0:
                    print("  Registration cancelled.\n");
                    active = false;
                    break;
                default: print("  Invalid option.\n");
            }
        }
    }

    private static void addCourseToReg(Registration reg, List<Course> available) {
        printDivider("Add Course");
        printCourseTable(available);
        print("  [0] Back");
        int choice = readChoice();
        if (choice <= 0 || choice > available.size()) return;

        Course selected = available.get(choice - 1);
        if (reg.getCourse(selected)) {
            print("  '" + selected.getCourseCode() + "' is already in your selection.\n");
        } else {
            reg.addCourse(selected);
            print("  Added: " + selected + "\n");
        }
    }

    private static void removeCourseFromReg(Registration reg) {
        Set<Course> selection = reg.getSelectedCourses();
        if (selection.isEmpty()) {
            print("  No courses selected yet.\n");
            return;
        }
        List<Course> list = new ArrayList<>(selection);
        printDivider("Remove Course");
        printCourseTable(list);
        print("  [0] Back");
        int choice = readChoice();
        if (choice <= 0 || choice > list.size()) return;

        reg.removeCourse(list.get(choice - 1));
        print("  Removed: " + list.get(choice - 1) + "\n");
    }

    private static void viewCurrentSelection(Registration reg) {
        Set<Course> selection = reg.getSelectedCourses();
        printDivider("Current Selection (" + selection.size() + " course(s))");
        if (selection.isEmpty()) {
            print("  Nothing selected yet.\n");
        } else {
            printCourseTable(new ArrayList<>(selection));
        }
        print("");
    }

    private static boolean submitRegistration(Registration reg) {
        try {
            reg.submit();
            print("\n  Registration submitted successfully!");
            print("  ID       : " + reg.getRegistrationId());
            print("  Semester : " + reg.getSemester());
            print("  Courses  : " + reg.getSelectedCourses().size());
            print("  Time     : " + reg.getSubmittedAt() + "\n");
            return true;
        } catch (RegistrationException e) {
            print("\n  " + e.getMessage() + "\n");
            return false;
        }
    }

    private static void viewRegistrations(Student student) {
        List<Registration> regs = student.getRegistrations();
        printDivider("My Registrations");
        if (regs.isEmpty()) {
            print("  No registrations on file.\n");
            return;
        }
        for (Registration r : regs) {
            print("  " + r);
            for (Course c : r.getSelectedCourses())
                print("      • " + c);
            print("");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Admin menu
    // ─────────────────────────────────────────────────────────────────────────

    private static void adminMenu(Admin admin) {
        boolean active = true;
        while (active) {
            printDivider("Admin Menu");
            print("  " + admin);
            print("  [1] View catalogue");
            print("  [2] Add course");
            print("  [3] Remove course");
            print("  [4] View student registration");
            print("  [5] Generate report");
            print("  [6] Register new student");
            print("  [0] Logout");

            switch (readChoice()) {
                case 1: viewCatalogue(admin);           break;
                case 2: addCourse();                    break;
                case 3: removeCourse(admin);            break;
                case 4: viewStudentRegistration(admin); break;
                case 5: generateReport(admin);          break;
                case 6: registerNewStudent(admin);      break;
                case 0:
                    authService.endSession();
                    print("Logged out.\n");
                    active = false;
                    break;
                default: print("  Invalid option.\n");
            }
        }
    }

    private static void viewCatalogue(Admin admin) {
        List<Course> all = admin.viewCatalogue();
        printDivider("Course Catalogue (" + all.size() + " courses)");
        if (all.isEmpty()) {
            print("  Catalogue is empty.\n");
        } else {
            printCourseTable(all);
            print("");
        }
    }

    private static void addCourse() {
        printDivider("Add Course");
        String code  = prompt("  Course code  : ").trim().toUpperCase();
        if (code.isEmpty()) { print("  Code cannot be blank.\n"); return; }
        String title = prompt("  Course title : ").trim();
        if (title.isEmpty()) { print("  Title cannot be blank.\n"); return; }

        Programme[] progs = Programme.values();
        print("  Programme:");
        for (int i = 0; i < progs.length; i++)
            print("    [" + (i + 1) + "] " + progs[i].name() + " — " + progs[i].getFullName());
        int pi = readChoice() - 1;
        if (pi < 0 || pi >= progs.length) { print("  Invalid programme.\n"); return; }

        int level    = readInt("  Level (year) : ");
        int semester = readInt("  Semester     : ");
        int units    = readInt("  Credit units : ");

        Course c = new Course(code, title, progs[pi], level, semester, units);
        dataStore.saveCourse(c);
        catalogue.addCourse(c);
        print("  Course '" + code + "' added to catalogue.\n");
    }

    private static void removeCourse(Admin admin) {
        String code = prompt("\n  Enter course code to remove: ").trim().toUpperCase();
        if (catalogue.findByCode(code) == null) {
            print("  Course '" + code + "' not found in catalogue.\n");
            return;
        }
        admin.removeCourse(code);
        print("  Course '" + code + "' removed.\n");
    }

    private static void viewStudentRegistration(Admin admin) {
        String id = prompt("\n  Enter student ID (e.g. NMS/2024/0001): ").trim();
        Student student = admin.getRegistrationData(id);
        if (student == null) {
            print("  Student '" + id + "' not found.\n");
            return;
        }
        printDivider("Registration — " + student.getStudentId());
        print("  " + student);
        List<Registration> regs = dataStore.findRegistration(student);
        if (regs.isEmpty()) {
            print("  No registrations on file.\n");
            return;
        }
        for (Registration r : regs) {
            print("\n  " + r);
            for (Course c : r.getSelectedCourses())
                print("      • " + c);
        }
        print("");
    }

    private static void generateReport(Admin admin) {
        print("\n" + admin.generateStudentReport());
    }

    private static void registerNewStudent(Admin admin) {
        printDivider("Register New Student");
        String fullName = prompt("  Full name    : ").trim();
        if (fullName.isEmpty()) { print("  Name cannot be blank.\n"); return; }
        String address  = prompt("  Address      : ").trim();
        String email    = prompt("  Email        : ").trim();
        String phone    = prompt("  Phone        : ").trim();

        Programme[] progs = Programme.values();
        print("  Programme:");
        for (int i = 0; i < progs.length; i++)
            print("    [" + (i + 1) + "] " + progs[i].name() + " — " + progs[i].getFullName());
        int pi = readChoice() - 1;
        if (pi < 0 || pi >= progs.length) { print("  Invalid programme.\n"); return; }

        // Generate a temporary password: first 8 chars of a UUID (uppercase)
        String tempPassword = java.util.UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

        Student student = admin.registerStudent(
                fullName, address, email, phone, progs[pi], tempPassword);

        print("\n  Student registered successfully!");
        print("  ┌─────────────────────────────────────────┐");
        print("  │  Student ID       : " + student.getStudentId());
        print("  │  Name             : " + student.getFullName());
        print("  │  Programme        : " + student.getProgramme().name());
        print("  │  Login ID         : " + student.getUserId());
        print("  │  Temporary Pwd    : " + tempPassword);
        print("  └─────────────────────────────────────────┘");
        print("  Share these credentials with the student.");
        print("  They will be prompted to change their password on first login.\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // I/O helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static void printBanner() {
        print("╔══════════════════════════════════════════════╗");
        print("║    NiMet Meteorological Training School      ║");
        print("║    Course Registration System  (RCRS)        ║");
        print("╚══════════════════════════════════════════════╝");
    }

    private static void printDivider(String title) {
        print("\n── " + title + " " + "─".repeat(Math.max(0, 44 - title.length())));
    }

    private static void printCourseTable(List<Course> courses) {
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            print(String.format("  [%2d] %-8s  %-52s  %d unit(s)",
                    i + 1, c.getCourseCode(), c.getCourseTitle(), c.getUnits()));
        }
    }

    private static int readChoice() {
        try {
            return Integer.parseInt(prompt("  Choice: ").trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int readInt(String label) {
        while (true) {
            try {
                return Integer.parseInt(prompt(label).trim());
            } catch (NumberFormatException e) {
                print("  Please enter a valid number.");
            }
        }
    }

    private static String prompt(String label) {
        System.out.print(label);
        if (!scanner.hasNextLine()) {
            print("\nExiting.");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    private static void print(String msg) {
        System.out.println(msg);
    }
}
