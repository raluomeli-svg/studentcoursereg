package com.nimet.rcrs.gui;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;

/**
 * Main window for the NiMet RCRS graphical interface.
 * Run via: java -cp out com.nimet.rcrs.gui.AppFrame
 */
public class AppFrame extends JFrame {

    private final DataStore dataStore;
    private final Catalogue catalogue;
    private final AuthenticationService authService;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final LoginPanel loginPanel;
    private AdminPanel   adminPanel;
    private StudentPanel studentPanel;

    public AppFrame() {
        // ── Bootstrap back-end ────────────────────────────────────────────────
        dataStore   = new DataStore();
        catalogue   = new Catalogue();
        authService = new AuthenticationService(dataStore, catalogue);
        for (Course c : dataStore.getAllCourses()) catalogue.addCourse(c);

        // ── Frame setup ───────────────────────────────────────────────────────
        setTitle("NiMet RCRS — Course Registration System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(860, 600));
        setLocationRelativeTo(null);

        // ── Login panel is always present ─────────────────────────────────────
        loginPanel = new LoginPanel(authService, this::onLogin);
        root.add(loginPanel, "login");
        setContentPane(root);
        cards.show(root, "login");
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void onLogin(User user) {
        if (user instanceof Admin) {
            Admin a = (Admin) user;
            adminPanel = new AdminPanel(a, dataStore, catalogue, authService, this::onLogout);
            root.add(adminPanel, "admin");
            cards.show(root, "admin");

        } else if (user instanceof Student) {
            Student s = (Student) user;
            studentPanel = new StudentPanel(s, dataStore, authService, this::onLogout);
            root.add(studentPanel, "student");
            cards.show(root, "student");

            // Force password change for new students
            if (s.isTemporaryPassword()) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "You are logged in with a temporary password.\nPlease change it now before continuing.",
                        "Password Change Required", JOptionPane.WARNING_MESSAGE);
                    studentPanel.showChangePasswordDialog();
                });
            }
        }
    }

    private void onLogout() {
        authService.endSession();
        if (adminPanel   != null) { root.remove(adminPanel);   adminPanel   = null; }
        if (studentPanel != null) { root.remove(studentPanel); studentPanel = null; }
        loginPanel.reset();
        cards.show(root, "login");
    }

    // ── Application entry point ───────────────────────────────────────────────

    public static void main(String[] args) {
        // Try Nimbus for a clean cross-platform look
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // Fall back to system look and feel silently
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored2) {}
        }
        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}
