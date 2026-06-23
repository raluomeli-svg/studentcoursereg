package com.nimet.rcrs.exception;

public class RegistrationException extends Exception {

    private final ErrorType errorType;

    public RegistrationException(ErrorType errorType) {
        super(errorType.getDefaultMessage());
        this.errorType = errorType;
    }

    // Use when you want to supply a more specific message (e.g. "You have 13 courses; max is 12.")
    public RegistrationException(ErrorType errorType, String detail) {
        super(detail);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public int getErrorCode() {
        return errorType.getCode();
    }

    @Override
    public String getMessage() {
        return "[" + errorType.name() + "] " + super.getMessage();
    }
}
