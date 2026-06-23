package com.nimet.rcrs.gui;

import com.nimet.rcrs.model.User;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final AuthenticationService authService;
    private final Consumer<User> onLoginSuccess;

    private JTextField userIdField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginPanel(AuthenticationService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.PRIMARY);

        // ── Card centred in the dark background ──────────────────────────────
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(UITheme.PRIMARY);
        add(centre, BorderLayout.CENTER);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            BorderFactory.createEmptyBorder(32, 36, 32, 36)
        ));
        card.setPreferredSize(new Dimension(400, 490));
        centre.add(card);

        // ── NiMet header ─────────────────────────────────────────────────────
        JLabel logoMark = new JLabel("NiMet", SwingConstants.CENTER);
        logoMark.setFont(new Font("SansSerif", Font.BOLD, 36));
        logoMark.setForeground(UITheme.PRIMARY);
        logoMark.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appTitle = new JLabel("Course Registration System", SwingConstants.CENTER);
        appTitle.setFont(UITheme.FONT_HEADING);
        appTitle.setForeground(UITheme.TEXT_MUTED);
        appTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel agency = new JLabel("Meteorological Training School — RCRS", SwingConstants.CENTER);
        agency.setFont(UITheme.FONT_SMALL);
        agency.setForeground(UITheme.TEXT_MUTED);
        agency.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(logoMark);
        card.add(Box.createVerticalStrut(4));
        card.add(appTitle);
        card.add(Box.createVerticalStrut(2));
        card.add(agency);
        card.add(Box.createVerticalStrut(24));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));
        sep.setForeground(UITheme.BORDER);
        card.add(sep);
        card.add(Box.createVerticalStrut(24));

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_CARD);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.insets = new Insets(0, 0, 4, 0);

        form.add(UITheme.label("User ID"), gc);
        gc.insets = new Insets(0, 0, 14, 0);
        userIdField = UITheme.styledField();
        form.add(userIdField, gc);

        gc.insets = new Insets(0, 0, 4, 0);
        form.add(UITheme.label("Password"), gc);
        gc.insets = new Insets(0, 0, 6, 0);
        passwordField = UITheme.styledPasswordField();
        form.add(passwordField, gc);

        card.add(form);
        card.add(Box.createVerticalStrut(4));

        // ── Error label ───────────────────────────────────────────────────────
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        errorLabel.setForeground(UITheme.ERROR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(10));

        // ── Login button ─────────────────────────────────────────────────────
        JButton loginBtn = UITheme.primaryButton("Sign In");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 42));
        loginBtn.setPreferredSize(new Dimension(328, 42));
        loginBtn.addActionListener(e -> attemptLogin());
        card.add(loginBtn);


        // ── Key bindings ─────────────────────────────────────────────────────
        userIdField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String userId   = userIdField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (userId.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both User ID and password.");
            return;
        }
        User user = authService.authenticate(userId, password);
        if (user == null) {
            errorLabel.setText("Invalid credentials. Please try again.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        } else {
            errorLabel.setText(" ");
            onLoginSuccess.accept(user);
        }
    }

    public void reset() {
        userIdField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        SwingUtilities.invokeLater(() -> userIdField.requestFocusInWindow());
    }
}
