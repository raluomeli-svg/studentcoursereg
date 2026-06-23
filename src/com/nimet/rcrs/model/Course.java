package com.nimet.rcrs.model;

import java.util.Objects;

public class Course {

    private final String courseCode;
    private final String courseTitle;
    private final Programme programme;
    private final int semester;
    private final int level;
    private final int units;

    public Course(String courseCode, String courseTitle, Programme programme,
                  int level, int semester, int units) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.programme = programme;
        this.level = level;
        this.semester = semester;
        this.units = units;
    }

    public String getCourseCode()   { return courseCode; }
    public String getCourseTitle()  { return courseTitle; }
    public Programme getProgramme() { return programme; }
    public int getSemester()        { return semester; }
    public int getLevel()           { return level; }
    public int getUnits()           { return units; }

    // Equality is based solely on courseCode — required for correct Set<Course> behaviour in Registration
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Course)) return false;
        return Objects.equals(courseCode, ((Course) o).courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d units)", courseCode, courseTitle, units);
    }
}
