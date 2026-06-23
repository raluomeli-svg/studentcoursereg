package com.nimet.rcrs.model;

import com.nimet.rcrs.exception.ErrorType;
import com.nimet.rcrs.exception.RegistrationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Registration {

    // Business rule constants — BIPMT has 8/semester, MFC up to 12, BIPM up to 10
    public static final int MIN_COURSES = 8;
    public static final int MAX_COURSES = 12;

    private final String registrationId;
    private final Student student;
    // LinkedHashSet: preserves insertion order so courses are always displayed in the order they were added
    private final Set<Course> selectedCourses = new LinkedHashSet<>();
    private final String semester;
    private LocalDateTime submittedAt;

    public Registration(Student student, String semester) {
        this.registrationId = UUID.randomUUID().toString();
        this.student = student;
        this.semester = semester;
    }

    public void addCourse(Course course) {
        selectedCourses.add(course);
    }

    // Returns true if the course was present and removed
    public boolean removeCourse(Course course) {
        return selectedCourses.remove(course);
    }

    // Returns true if the course is already in the selection
    public boolean getCourse(Course course) {
        return selectedCourses.contains(course);
    }

    public void validateCourseCount() throws RegistrationException {
        int count = selectedCourses.size();
        if (count < MIN_COURSES) {
            throw new RegistrationException(ErrorType.TOO_FEW_COURSES,
                    "You have selected " + count + " course(s). Minimum required is " + MIN_COURSES + ".");
        }
        if (count > MAX_COURSES) {
            throw new RegistrationException(ErrorType.TOO_MANY_COURSES,
                    "You have selected " + count + " course(s). Maximum allowed is " + MAX_COURSES + ".");
        }
    }

    // Validates the selection and locks the registration with a timestamp
    public void submit() throws RegistrationException {
        validateCourseCount();
        submittedAt = LocalDateTime.now();
    }

    public String getRegistrationId()       { return registrationId; }
    public Student getStudent()             { return student; }
    public String getSemester()             { return semester; }
    public LocalDateTime getSubmittedAt()   { return submittedAt; }
    public boolean isSubmitted()            { return submittedAt != null; }

    // Returns an unmodifiable view so callers cannot mutate the set directly
    public Set<Course> getSelectedCourses() {
        return Collections.unmodifiableSet(selectedCourses);
    }

    @Override
    public String toString() {
        String status = isSubmitted()
                ? "Submitted at " + submittedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "PENDING";
        return String.format("Registration[%s] | %s | %d course(s) | %s",
                semester, student.getStudentId(), selectedCourses.size(), status);
    }
}
