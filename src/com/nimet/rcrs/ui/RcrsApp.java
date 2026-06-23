package com.nimet.rcrs.ui;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for the NiMet RCRS GUI.
 *
 * Uses a {@link CardLayout} to navigate between three screens:
 *   <ul>
 *     <li>{@link LoginPanel}       — authentication screen</li>
 *     <li>{@link StudentDashboard} — student portal (built on successful student login)</li>
 *     <li>{@link AdminDashboard}   — admin portal (built on successful admin login)</li>
 *   </ul>
 *
 * This class also owns the shared infrastructure objects (DataStore, Catalogue,
 * AuthenticationService) and passes them down to the panels that need them.
 */
public class RcrsApp extends JFrame {

    // Card identifiers used with CardLayout.show()
    static final String CARD_LOGIN   = "LOGIN";
    static final String CARD_STUDENT = "STUDENT";
    static final String CARD_ADMIN   = "ADMIN";

    // Shared data layer — single instances for the lifetime of the application
    private final DataStore             dataStore;
    private final Catalogue             catalogue;
    private final AuthenticationService authService;

    private final CardLayout cardLayout;
    private final JPanel     cardPanel;

    private final LoginPanel loginPanel;

    // Created lazily after a successful login and removed again on logout
    private StudentDashboard studentDashboard;
    private AdminDashboard   adminDashboard;

    /** Bootstraps data, builds the window, and makes it visible. */
    public RcrsApp() {
        super("NiMet Meteorological Training School — Course Registration System");

        // ── Data bootstrap ────────────────────────────────────────────────────
        dataStore   = new DataStore();
        catalogue   = new Catalogue();
        authService = new AuthenticationService(dataStore, catalogue);

        // Seed the Catalogue from DataStore at startup (no user required)
        for (Course c : dataStore.getAllCourses()) catalogue.addCourse(c);

        // ── UI setup ──────────────────────────────────────────────────────────
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this, authService);
        cardPanel.add(loginPanel, CARD_LOGIN);

        add(cardPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);   // centre on screen
        setVisible(true);
    }

    /**
     * Called by {@link LoginPanel} after a successful authentication.
     * Builds the appropriate dashboard panel and switches to it.
     *
     * @param user the authenticated user (Student or Admin)
     */
    void onLoginSuccess(User user) {
        if (user instanceof Student) {
            studentDashboard = new StudentDashboard(this, (Student) user, dataStore, authService);
            cardPanel.add(studentDashboard, CARD_STUDENT);
            cardLayout.show(cardPanel, CARD_STUDENT);
            if (((Student) user).isTemporaryPassword()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "You are logged in with a temporary password.\nPlease change it now.",
                        "Password Change Required", JOptionPane.WARNING_MESSAGE);
                    studentDashboard.showChangePasswordDialog();
                });
            }
        } else if (user instanceof Admin) {
            adminDashboard = new AdminDashboard(this, (Admin) user, dataStore, catalogue, authService);
            cardPanel.add(adminDashboard, CARD_ADMIN);
            cardLayout.show(cardPanel, CARD_ADMIN);
        }
    }

    /**
     * Called by a dashboard's Logout button.
     * Ends the auth session, removes the current dashboard panel, and returns
     * to the login screen.
     */
    void onLogout() {
        authService.endSession();

        if (studentDashboard != null) {
            cardPanel.remove(studentDashboard);
            studentDashboard = null;
        }
        if (adminDashboard != null) {
            cardPanel.remove(adminDashboard);
            adminDashboard = null;
        }

        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    /** Returns the shared DataStore (used by dashboards that need direct store access). */
    DataStore getDataStore() { return dataStore; }

    /**
     * Application entry point.
     * Schedules UI construction on the Event Dispatch Thread as required by Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(RcrsApp::new);
    }
}
