package com.nimet.rcrs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Catalogue {

    private final List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    // Returns all courses for a given academic level (year) and semester
    public List<Course> filterByLvl_sem(int level, int semester) {
        return courses.stream()
                .filter(c -> c.getLevel() == level && c.getSemester() == semester)
                .collect(Collectors.toList());
    }

    public Course findByCode(String code) {
        return courses.stream()
                .filter(c -> c.getCourseCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    // Returns all courses belonging to a specific programme
    public List<Course> findCourses(Programme programme) {
        return courses.stream()
                .filter(c -> c.getProgramme() == programme)
                .collect(Collectors.toList());
    }

    public List<Course> getAllCourses() {
        return new ArrayList<>(courses);
    }
}
