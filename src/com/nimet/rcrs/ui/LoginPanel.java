package com.nimet.rcrs.ui;

import com.nimet.rcrs.model.User;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Login screen panel.
 *
 * Collects a User ID and password, delegates to {@link AuthenticationService},
 * and notifies {@link RcrsApp#onLoginSuccess(User)} on success.
 * After {@value #MAX_ATTEMPTS} failed attempts the input fields are disabled.
 */
public class LoginPanel extends JPanel {

    /** Maximum consecutive failed login attempts before the form is locked. */
    private static final int MAX_ATTEMPTS = 3;

    private final RcrsApp               app;
    private final AuthenticationService authService;

    private final JTextField     userIdField;
    private final JPasswordField passwordField;
    private final JLabel         messageLabel;

    /** Running count of consecutive failed attempts in this session. */
    private int failedAttempts = 0;

    /**
     * Constructs the login panel.
     *
     * @param app         the parent frame (receives the success callback)
     * @param authService the service that verifies credentials
     */
    public LoginPanel(RcrsApp app, AuthenticationService authService) {
        this.app         = app;
        this.authService = authService;

        // Navy background fills the full panel; the white card sits centred on it
        setLayout(new GridBagLayout());
        setBackground(new Color(0x1B, 0x3A, 0x5C));

        // ── Card (white rounded container) ────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(44, 56, 44, 56));

        GridBagConstraints g = new GridBagConstraints();
        g.fill      = GridBagConstraints.HORIZONTAL;
        g.gridwidth = 2;

        // Heading
        JLabel heading = label("NiMet — RCRS", 22, Font.BOLD, new Color(0x1B, 0x3A, 0x5C));
        heading.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        card.add(heading, g);

        JLabel subheading = label("Meteorological Training School", 13, Font.PLAIN, Color.GRAY);
        subheading.setHorizontalAlignment(SwingConstants.CENTER);
        g.gridy = 1; g.insets = new Insets(0, 0, 32, 0);
        card.add(subheading, g);

        // ── Form fields ───────────────────────────────────────────────────────
        g.gridwidth = 1; g.insets = new Insets(6, 0, 6, 10);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        card.add(new JLabel("User ID:"), g);
        userIdField = new JTextField(18);
        // Wrap in same BorderLayout structure as pwdRow so both input boxes are equal width
        JPanel userIdRow = new JPanel(new BorderLayout(2, 0));
        userIdRow.setOpaque(false);
        userIdRow.add(userIdField, BorderLayout.CENTER);
        userIdRow.add(Box.createHorizontalStrut(28), BorderLayout.EAST);
        g.gridx = 1; g.weightx = 1;
        card.add(userIdRow, g);

        g.gridy = 3; g.gridx = 0; g.weightx = 0;
        card.add(new JLabel("Password:"), g);
        passwordField = new JPasswordField(18);
        final char defaultEcho = passwordField.getEchoChar();
        JButton eyeBtn = new JButton("👁");
        eyeBtn.setFont(new Font("Dialog", Font.PLAIN, 14));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setBorderPainted(false);
        eyeBtn.setContentAreaFilled(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.setForeground(Color.GRAY);
        eyeBtn.setMargin(new Insets(0, 4, 0, 4));
        eyeBtn.setPreferredSize(new Dimension(28, 28));
        eyeBtn.setToolTipText("Show / hide password");
        eyeBtn.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\0') {
                passwordField.setEchoChar(defaultEcho);
                eyeBtn.setForeground(Color.GRAY);
            } else {
                passwordField.setEchoChar('\0');
                eyeBtn.setForeground(new Color(0x1B, 0x3A, 0x5C));
            }
            passwordField.requestFocus();
        });
        JPanel pwdRow = new JPanel(new BorderLayout(2, 0));
        pwdRow.setOpaque(false);
        pwdRow.add(passwordField, BorderLayout.CENTER);
        pwdRow.add(eyeBtn, BorderLayout.EAST);
        g.gridx = 1; g.weightx = 1;
        card.add(pwdRow, g);

        // ── Status message ────────────────────────────────────────────────────
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 12f));
        g.gridy = 4; g.gridx = 0; g.gridwidth = 2; g.weightx = 0;
        g.insets = new Insets(10, 0, 4, 0);
        card.add(messageLabel, g);

        // ── Login button ──────────────────────────────────────────────────────
        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn, new Color(0x1B, 0x3A, 0x5C));
        loginBtn.setFont(loginBtn.getFont().deriveFont(Font.BOLD, 14f));
        g.gridy = 5; g.insets = new Insets(6, 30, 0, 30);
        card.add(loginBtn, g);

        add(card);

        // ── Event wiring ──────────────────────────────────────────────────────
        loginBtn.addActionListener(e -> doLogin());
        // Enter in password field triggers login; Enter in userId field advances focus
        passwordField.addActionListener(e -> doLogin());
        userIdField.addActionListener(e -> passwordField.requestFocus());
    }

    /**
     * Reads the form, calls {@link AuthenticationService#authenticate(String, String)},
     * and either delegates to {@link RcrsApp#onLoginSuccess(User)} or shows an error.
     */
    private void doLogin() {
        String userId   = userIdField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (userId.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both User ID and Password.");
            return;
        }

        User user = authService.authenticate(userId, password);

        if (user != null) {
            // Reset state for the next login session
            failedAttempts = 0;
            userIdField.setText("");
            passwordField.setText("");
            messageLabel.setText(" ");
            app.onLoginSuccess(user);
        } else {
            failedAttempts++;
            int remaining = MAX_ATTEMPTS - failedAttempts;
            if (remaining > 0) {
                messageLabel.setText("Invalid credentials. " + remaining + " attempt(s) remaining.");
            } else {
                messageLabel.setText("Too many failed attempts. Form locked.");
                userIdField.setEnabled(false);
                passwordField.setEnabled(false);
            }
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private JLabel label(String text, float size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, (int) size));
        l.setForeground(color);
        return l;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
