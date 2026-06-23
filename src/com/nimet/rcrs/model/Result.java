package com.nimet.rcrs.model;

public class Result {

    private final String registrationId;
    private final Course course;
    private double testScore;   // continuous assessment, max 40
    private double examScore;   // examination, max 60

    public Result(String registrationId, Course course, double testScore, double examScore) {
        this.registrationId = registrationId;
        this.course         = course;
        this.testScore      = Math.max(0, Math.min(40, testScore));
        this.examScore      = Math.max(0, Math.min(60, examScore));
    }

    public double getTotal() { return testScore + examScore; }

    public String getGrade() {
        double t = getTotal();
        if (t >= 70) return "A";
        if (t >= 60) return "B";
        if (t >= 50) return "C";
        if (t >= 45) return "D";
        if (t >= 40) return "E";
        return "F";
    }

    // Grade point on the 5.0 scale (NUC standard)
    public double getGradePoint() {
        double t = getTotal();
        if (t >= 70) return 5.0;
        if (t >= 60) return 4.0;
        if (t >= 50) return 3.0;
        if (t >= 45) return 2.0;
        if (t >= 40) return 1.0;
        return 0.0;
    }

    public String getRegistrationId() { return registrationId; }
    public Course  getCourse()         { return course; }
    public double  getTestScore()      { return testScore; }
    public double  getExamScore()      { return examScore; }

    public void setTestScore(double s) { testScore = Math.max(0, Math.min(40, s)); }
    public void setExamScore(double s) { examScore = Math.max(0, Math.min(60, s)); }
}
