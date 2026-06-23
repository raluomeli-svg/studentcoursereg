package com.nimet.rcrs.model;

public enum Programme {

    BIPMT("Basic Instructional Package for Meteorological Technicians"),
    BIPM("Basic Instructional Package for Meteorologists"),
    MFC("Meteorological Forecasting Course");

    private final String fullName;

    Programme(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}
