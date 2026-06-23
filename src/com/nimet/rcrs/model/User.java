package com.nimet.rcrs.model;

public abstract class User {

    protected final String userId;
    protected final String fullName;
    protected String passwordHash;
    private boolean loggedIn = false;

    protected User(String userId, String fullName, String passwordHash) {
        this.userId = userId;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
    }

    public void login() {
        loggedIn = true;
    }

    public void logout() {
        loggedIn = false;
    }

    // Compares a plain-text password against the stored hash.
    // Actual hashing is handled by AuthenticationService before calling this.
    public boolean verifyPassword(String hashedInput) {
        return passwordHash.equals(hashedInput);
    }

    // Called by AuthenticationService.changePassword() after hashing the new value.
    public void setPasswordHash(String hashedPassword) {
        this.passwordHash = hashedPassword;
    }

    public String getUserId()       { return userId; }
    public String getFullName()     { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isLoggedIn()     { return loggedIn; }

    @Override
    public String toString() {
        return String.format("%s (%s)", fullName, userId);
    }
}
