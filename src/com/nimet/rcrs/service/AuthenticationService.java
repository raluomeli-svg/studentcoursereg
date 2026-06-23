package com.nimet.rcrs.service;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationService {

    private final DataStore dataStore;
    private final Catalogue catalogue;
    private User currentUser;

    public AuthenticationService(DataStore dataStore, Catalogue catalogue) {
        this.dataStore = dataStore;
        this.catalogue = catalogue;
    }

    /**
     * Looks up the user by userId, hashes the supplied password, and verifies it.
     * On success: marks the user as logged in and wires DataStore + Catalogue into
     * Student or Admin so their methods work immediately after this call.
     *
     * @return the authenticated User, or null if credentials are invalid
     */
    public User authenticate(String userId, String password) {
        User user = dataStore.findUserByLoginId(userId);
        if (user == null) return null;

        String hashed = hashPassword(password);
        if (!user.verifyPassword(hashed)) return null;

        user.login();
        currentUser = user;
        wireDependencies(user);
        return user;
    }

    public String hashPassword(String pwd) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void endSession() {
        if (currentUser != null) {
            currentUser.logout();
            currentUser = null;
        }
    }

    public boolean validateSession() {
        return currentUser != null && currentUser.isLoggedIn();
    }

    // Verifies the current password, then replaces it with a hash of the new one.
    // Clears the temporaryPassword flag if the user is a Student.
    // Returns false if the current password is wrong.
    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (!user.verifyPassword(hashPassword(currentPassword))) return false;
        user.setPasswordHash(hashPassword(newPassword));
        if (user instanceof Student) {
            ((Student) user).setTemporaryPassword(false);
        }
        return true;
    }

    // Injects DataStore and Catalogue into the user after a successful login
    // so Student/Admin methods can call them without needing constructor args.
    private void wireDependencies(User user) {
        if (user instanceof Student) {
            Student s = (Student) user;
            s.setCatalogue(catalogue);
            s.setDataStore(dataStore);
        } else if (user instanceof Admin) {
            Admin a = (Admin) user;
            a.setCatalogue(catalogue);
            a.setDataStore(dataStore);
        }
    }
}
