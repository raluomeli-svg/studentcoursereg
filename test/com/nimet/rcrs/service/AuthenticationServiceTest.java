package com.nimet.rcrs.service;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for AuthenticationService.
 *
 * Covers: successful login for both roles, wrong-password rejection, unknown-user
 * rejection, session management (getCurrentUser, endSession, validateSession),
 * and automatic dependency wiring into Student/Admin after login.
 */
public class AuthenticationServiceTest {

    private DataStore dataStore;
    private Catalogue catalogue;
    private AuthenticationService authService;

    /**
     * Build a fresh DataStore (which seeds the default users and courses) and
     * a Catalogue loaded from it, then create the service under test.
     * Default credentials seeded by DataStore:
     *   Admin   — userId: "admin01"        password: "admin123"
     *   Student — userId: "NMS/2024/0001"  password: "student123"
     */
    @Before
    public void setUp() {
        dataStore   = new DataStore();
        catalogue   = new Catalogue();
        for (Course c : dataStore.getAllCourses()) catalogue.addCourse(c);
        authService = new AuthenticationService(dataStore, catalogue);
    }

    // ── Successful authentication ─────────────────────────────────────────────

    @Test
    public void testValidStudentLoginReturnsStudent() {
        User user = authService.authenticate("NMS/2024/0001", "student123");
        assertNotNull(user);
        assertTrue(user instanceof Student);
    }

    @Test
    public void testValidAdminLoginReturnsAdmin() {
        User user = authService.authenticate("admin01", "admin123");
        assertNotNull(user);
        assertTrue(user instanceof Admin);
    }

    /** The returned user must be marked logged-in after a successful authentication. */
    @Test
    public void testSuccessfulLoginMarksUserLoggedIn() {
        User user = authService.authenticate("admin01", "admin123");
        assertNotNull(user);
        assertTrue(user.isLoggedIn());
    }

    // ── Failed authentication ─────────────────────────────────────────────────

    @Test
    public void testWrongPasswordReturnsNull() {
        User user = authService.authenticate("admin01", "wrongpassword");
        assertNull(user);
    }

    @Test
    public void testUnknownUserIdReturnsNull() {
        User user = authService.authenticate("nobody", "password");
        assertNull(user);
    }

    @Test
    public void testEmptyPasswordReturnsNull() {
        // SHA-256 of "" will not match the stored hash
        User user = authService.authenticate("admin01", "");
        assertNull(user);
    }

    // ── Session management ────────────────────────────────────────────────────

    @Test
    public void testCurrentUserSetAfterSuccessfulLogin() {
        authService.authenticate("admin01", "admin123");
        assertNotNull(authService.getCurrentUser());
    }

    @Test
    public void testCurrentUserNullBeforeAnyLogin() {
        // Fresh service — no login has occurred yet
        assertNull(authService.getCurrentUser());
    }

    @Test
    public void testEndSessionClearsCurrentUser() {
        authService.authenticate("admin01", "admin123");
        authService.endSession();
        assertNull(authService.getCurrentUser());
    }

    @Test
    public void testValidateSessionReturnsTrueAfterLogin() {
        authService.authenticate("NMS/2024/0001", "student123");
        assertTrue(authService.validateSession());
    }

    @Test
    public void testValidateSessionReturnsFalseAfterLogout() {
        authService.authenticate("NMS/2024/0001", "student123");
        authService.endSession();
        assertFalse(authService.validateSession());
    }

    @Test
    public void testValidateSessionReturnsFalseWithoutLogin() {
        assertFalse(authService.validateSession());
    }

    // ── Dependency wiring ─────────────────────────────────────────────────────

    /**
     * After a Student logs in, the service wires Catalogue and DataStore into the
     * Student object so viewAvailableCourses() can be called without throwing.
     */
    @Test
    public void testStudentDependenciesWiredAfterLogin() {
        User user = authService.authenticate("NMS/2024/0001", "student123");
        Student student = (Student) user;
        List<Course> courses = student.viewAvailableCourses();
        assertNotNull(courses);
    }

    /**
     * After an Admin logs in, viewCatalogue() must not throw — dependencies are wired.
     */
    @Test
    public void testAdminDependenciesWiredAfterLogin() {
        User user = authService.authenticate("admin01", "admin123");
        Admin admin = (Admin) user;
        List<Course> courses = admin.viewCatalogue();
        assertNotNull(courses);
    }
}
