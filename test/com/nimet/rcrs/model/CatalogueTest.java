package com.nimet.rcrs.model;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the Catalogue class.
 *
 * Covers: filtering by level/semester, find by code (including case-insensitivity),
 * find by programme, add, remove, and getAllCourses.
 */
public class CatalogueTest {

    private Catalogue catalogue;

    /**
     * Populate a small, known catalogue before each test:
     *   BIP110 — BIPMT, Y1 S1, 4 units
     *   BIP111 — BIPMT, Y1 S1, 3 units
     *   BIP120 — BIPMT, Y1 S2, 3 units
     *   BIP310 — MFC,   Y1 S1, 3 units
     */
    @Before
    public void setUp() {
        catalogue = new Catalogue();
        catalogue.addCourse(new Course("BIP110", "Met Observations I", Programme.BIPMT, 1, 1, 4));
        catalogue.addCourse(new Course("BIP111", "General Physics",    Programme.BIPMT, 1, 1, 3));
        catalogue.addCourse(new Course("BIP120", "Upper Air Obs",      Programme.BIPMT, 1, 2, 3));
        catalogue.addCourse(new Course("BIP310", "Physical Met",       Programme.MFC,   1, 1, 3));
    }

    // ── filterByLvl_sem ───────────────────────────────────────────────────────

    @Test
    public void testFilterByLvlSemReturnsMatchingCourses() {
        // BIP110, BIP111 (BIPMT) and BIP310 (MFC) are all Y1 S1
        List<Course> result = catalogue.filterByLvl_sem(1, 1);
        assertEquals(3, result.size());
    }

    @Test
    public void testFilterByLvlSemExcludesOtherSemester() {
        // Only BIP120 is Y1 S2
        List<Course> result = catalogue.filterByLvl_sem(1, 2);
        assertEquals(1, result.size());
        assertEquals("BIP120", result.get(0).getCourseCode());
    }

    @Test
    public void testFilterByLvlSemReturnsEmptyForUnknownLevel() {
        List<Course> result = catalogue.filterByLvl_sem(9, 1);
        assertTrue(result.isEmpty());
    }

    // ── findByCode ────────────────────────────────────────────────────────────

    @Test
    public void testFindByCodeReturnsCorrectCourse() {
        Course c = catalogue.findByCode("BIP110");
        assertNotNull(c);
        assertEquals("Met Observations I", c.getCourseTitle());
    }

    /** Course code lookup must be case-insensitive. */
    @Test
    public void testFindByCodeIsCaseInsensitive() {
        assertNotNull(catalogue.findByCode("bip110"));
        assertNotNull(catalogue.findByCode("Bip110"));
    }

    @Test
    public void testFindByCodeReturnsNullForUnknownCode() {
        assertNull(catalogue.findByCode("INVALID999"));
    }

    // ── findCourses (by programme) ────────────────────────────────────────────

    @Test
    public void testFindCoursesByProgrammeReturnsMfcOnly() {
        List<Course> mfc = catalogue.findCourses(Programme.MFC);
        assertEquals(1, mfc.size());
        assertEquals("BIP310", mfc.get(0).getCourseCode());
    }

    @Test
    public void testFindCoursesByProgrammeReturnsBipmtCourses() {
        List<Course> bipmt = catalogue.findCourses(Programme.BIPMT);
        assertEquals(3, bipmt.size());
    }

    @Test
    public void testFindCoursesByProgrammeReturnsEmptyForBipm() {
        // No BIPM courses were added in setUp
        List<Course> bipm = catalogue.findCourses(Programme.BIPM);
        assertTrue(bipm.isEmpty());
    }

    // ── addCourse ─────────────────────────────────────────────────────────────

    @Test
    public void testAddCourseIncreasesTotalCount() {
        catalogue.addCourse(new Course("BIP999", "New Course", Programme.BIPM, 1, 1, 3));
        assertEquals(5, catalogue.getAllCourses().size());
        assertNotNull(catalogue.findByCode("BIP999"));
    }

    // ── removeCourse ──────────────────────────────────────────────────────────

    @Test
    public void testRemoveCourseDecreasesTotalCount() {
        Course c = catalogue.findByCode("BIP110");
        catalogue.removeCourse(c);
        assertEquals(3, catalogue.getAllCourses().size());
    }

    @Test
    public void testRemovedCourseCanNoLongerBeFound() {
        Course c = catalogue.findByCode("BIP110");
        catalogue.removeCourse(c);
        assertNull(catalogue.findByCode("BIP110"));
    }

    // ── getAllCourses ──────────────────────────────────────────────────────────

    @Test
    public void testGetAllCoursesReturnsAllFour() {
        assertEquals(4, catalogue.getAllCourses().size());
    }

    /** getAllCourses() must return a defensive copy — mutating it must not affect the catalogue. */
    @Test
    public void testGetAllCoursesIsDefensiveCopy() {
        List<Course> copy = catalogue.getAllCourses();
        copy.clear();
        // Original catalogue must still have 4 courses
        assertEquals(4, catalogue.getAllCourses().size());
    }
}
