package com.nimet.rcrs.exception;

public enum ErrorType {

    TOO_FEW_COURSES(1, "You must select the minimum required number of courses before submitting."),
    TOO_MANY_COURSES(2, "You have exceeded the maximum number of courses allowed for this semester.");

    private final int code;
    private final String defaultMessage;

    ErrorType(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
