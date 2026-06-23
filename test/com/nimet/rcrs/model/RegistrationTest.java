package com.nimet.rcrs.model;

import com.nimet.rcrs.exception.RegistrationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Registration class.
 *
 * Covers: adding/removing courses, duplicate prevention, course-count validation
 * (too few / too many / valid boundary), submit state, and the unmodifiable view.
 */
public class RegistrationTest {

    /** A minimal Student used as the owner for every Registration under test. */
    private Student student;
    private Registration registration;

    @Before
    public void setUp() {
        student = new Student(
                "NMS/2024/0001", "Test Student", "hash",
                "NMS/2024/0001", Programme.BIPMT, 2024, 1, 1);
        registration = new Registration(student, "2024/1");
    }

    // ── addCourse / getCourse ──────────────────────────────────────────────────

    @Test
    public void testAddCourseIncreasesSelectionSize() {
        Course c = makeCourse("BIP110");
        registration.addCourse(c);
        assertEquals(1, registration.getSelectedCourses().size());
    }

    @Test
    public void testGetCourseReturnsTrueAfterAdd() {
        Course c = makeCourse("BIP110");
        registration.addCourse(c);
        assertTrue(registration.getCourse(c));
    }

    /** Adding the same course twice must not create a duplicate entry (Set semantics). */
    @Test
    public void testDuplicateCourseIsNotAdded() {
        Course c = makeCourse("BIP110");
        registration.addCourse(c);
        registration.addCourse(c);
        assertEquals(1, registration.getSelectedCourses().size());
    }

    // ── removeCourse ──────────────────────────────────────────────────────────

    @Test
    public void testRemoveCourseReturnsTrueAndDecreasesSize() {
        Course c = makeCourse("BIP110");
        registration.addCourse(c);
        boolean removed = registration.removeCourse(c);
        assertTrue(removed);
        assertFalse(registration.getCourse(c));
        assertEquals(0, registration.getSelectedCourses().size());
    }

    @Test
    public void testRemoveCourseNotPresentReturnsFalse() {
        Course c = makeCourse("BIP110");
        boolean result = registration.removeCourse(c);
        assertFalse(result);
    }

    // ── validateCourseCount / submit ──────────────────────────────────────────

    /** Below MIN_COURSES must throw TOO_FEW_COURSES. */
    @Test(expected = RegistrationException.class)
    public void testSubmitWithTooFewCoursesThrows() throws RegistrationException {
        addCourses(2); // MIN is 8
        registration.submit();
    }

    /** Above MAX_COURSES must throw TOO_MANY_COURSES. */
    @Test(expected = RegistrationException.class)
    public void testSubmitWithTooManyCoursesThrows() throws RegistrationException {
        addCourses(Registration.MAX_COURSES + 1);
        registration.submit();
    }

    /** Exactly MIN_COURSES is valid — submit must succeed and set the timestamp. */
    @Test
    public void testSubmitAtMinimumSucceeds() throws RegistrationException {
        addCourses(Registration.MIN_COURSES);
        registration.submit();
        assertTrue(registration.isSubmitted());
        assertNotNull(registration.getSubmittedAt());
    }

    /** Exactly MAX_COURSES is also valid. */
    @Test
    public void testSubmitAtMaximumSucceeds() throws RegistrationException {
        addCourses(Registration.MAX_COURSES);
        registration.submit();
        assertTrue(registration.isSubmitted());
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    public void testNewRegistrationIsNotSubmitted() {
        assertFalse(registration.isSubmitted());
        assertNull(registration.getSubmittedAt());
    }

    @Test
    public void testNewRegistrationHasNoSelectedCourses() {
        assertTrue(registration.getSelectedCourses().isEmpty());
    }

    // ── unmodifiable view ─────────────────────────────────────────────────────

    /**
     * getSelectedCourses() must return an unmodifiable view — callers must not
     * be able to bypass addCourse() / removeCourse() business logic.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetSelectedCoursesIsUnmodifiable() {
        registration.getSelectedCourses().add(makeCourse("BIP999"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Creates a throwaway Course with a unique code suffix. */
    private Course makeCourse(String code) {
        return new Course(code, "Title " + code, Programme.BIPMT, 1, 1, 3);
    }

    /**
     * Adds {@code n} distinct courses to the registration.
     * Codes are generated as BIP1000, BIP1001, … so they never clash with real codes.
     */
    private void addCourses(int n) {
        for (int i = 0; i < n; i++) {
            registration.addCourse(makeCourse("BIP" + (1000 + i)));
        }
    }
}
