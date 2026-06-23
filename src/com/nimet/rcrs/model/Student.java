package com.nimet.rcrs.model;

import com.nimet.rcrs.repository.DataStore;

import java.util.List;

public class Student extends User {

    // Format: NMS/YYYY/NNNN  e.g. NMS/2024/0001
    private final String studentId;
    private final Programme programme;
    private final int yearOfAdmission;
    private final int currentYear;
    private final int currentSemester;

    // Profile fields — set at registration, editable only via admin
    private String address;
    private String email;
    private String phone;

    // True when the student has not yet changed their system-generated password
    private boolean temporaryPassword;

    // Injected after construction by AuthenticationService (or Main)
    private Catalogue catalogue;
    private DataStore dataStore;

    public Student(String userId, String fullName, String passwordHash,
                   String studentId, Programme programme,
                   int yearOfAdmission, int currentYear, int currentSemester,
                   String address, String email, String phone) {
        super(userId, fullName, passwordHash);
        this.studentId       = studentId;
        this.programme       = programme;
        this.yearOfAdmission = yearOfAdmission;
        this.currentYear     = currentYear;
        this.currentSemester = currentSemester;
        this.address         = address;
        this.email           = email;
        this.phone           = phone;
        this.temporaryPassword = false;
    }

    // Returns courses available for the student's current level and semester
    public List<Course> viewAvailableCourses() {
        if (catalogue == null) throw new IllegalStateException("Catalogue not wired into Student.");
        return catalogue.filterByLvl_sem(currentYear, currentSemester);
    }

    // Returns all registrations filed by this student
    public List<Registration> getRegistrations() {
        if (dataStore == null) throw new IllegalStateException("DataStore not wired into Student.");
        return dataStore.findRegistration(this);
    }

    // Creates and returns a blank Registration for the current semester.
    // Caller (Main) populates it, then calls registration.submit().
    public Registration submitRegistration() {
        int calendarYear = yearOfAdmission + (currentYear - 1);
        String semester  = calendarYear + "/" + currentSemester;
        return new Registration(this, semester);
    }

    public String viewProfile() {
        return String.format(
                "┌─ Student Profile ────────────────────────────┐\n" +
                "  ID          : %s\n" +
                "  Name        : %s\n" +
                "  Programme   : %s — %s\n" +
                "  Year / Sem  : Year %d · Semester %d\n" +
                "  Admitted    : %d\n" +
                "  Address     : %s\n" +
                "  Email       : %s\n" +
                "  Phone       : %s\n" +
                "  Password    : %s\n" +
                "└──────────────────────────────────────────────┘",
                studentId, fullName,
                programme.name(), programme.getFullName(),
                currentYear, currentSemester,
                yearOfAdmission,
                address.isEmpty() ? "—" : address,
                email.isEmpty()   ? "—" : email,
                phone.isEmpty()   ? "—" : phone,
                temporaryPassword ? "Temporary (please change)" : "Set"
        );
    }

    public String getStudentId()          { return studentId; }
    public Programme getProgramme()       { return programme; }
    public int getYearOfAdmission()       { return yearOfAdmission; }
    public int getCurrentYear()           { return currentYear; }
    public int getCurrentSemester()       { return currentSemester; }
    public String getAddress()            { return address; }
    public String getEmail()              { return email; }
    public String getPhone()              { return phone; }
    public boolean isTemporaryPassword()  { return temporaryPassword; }

    public void setTemporaryPassword(boolean flag) { this.temporaryPassword = flag; }

    public void setCatalogue(Catalogue catalogue)  { this.catalogue = catalogue; }
    public void setDataStore(DataStore dataStore)  { this.dataStore = dataStore; }

    @Override
    public String toString() {
        return String.format("%s | %s | Year %d Sem %d | %s",
                studentId, fullName, currentYear, currentSemester, programme.name());
    }
}
