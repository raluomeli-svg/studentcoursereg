package com.nimet.rcrs.ui;

import com.nimet.rcrs.exception.RegistrationException;
import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StudentDashboard extends JPanel {

    private static final Color NAVY  = new Color(0x1B, 0x3A, 0x5C);
    private static final Color GREEN = new Color(0x28, 0x7A, 0x3B);
    private static final Color RED   = new Color(0xAA, 0x33, 0x22);

    private final RcrsApp               app;
    private final Student               student;
    private final DataStore             dataStore;
    private final AuthenticationService authService;

    // Register-courses tab
    private List<Course>       availableCourses;
    private final List<Course> selectedCoursesList = new ArrayList<>();
    private DefaultTableModel  availableModel;
    private DefaultTableModel  selectedModel;
    private JLabel             selectionCountLabel;
    private Registration       existingRegistration;

    // Registrations + results tab
    private DefaultTableModel  myRegModel;
    private DefaultTableModel  resultsModel;
    private JLabel             sgpaLabel, cgpaLabel, honoursLabel;

    public StudentDashboard(RcrsApp app, Student student,
                            DataStore dataStore, AuthenticationService authService) {
        this.app         = app;
        this.student     = student;
        this.dataStore   = dataStore;
        this.authService = authService;

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile",       buildProfileTab());
        tabs.addTab("Register Courses", buildRegisterTab());
        tabs.addTab("My Registrations", buildRegistrationsTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel name = styledLabel("Welcome, " + student.getFullName(), 16, Font.BOLD, Color.WHITE);
        JLabel info = styledLabel(
                student.getStudentId() + "  ·  " + student.getProgramme().name()
                + "  ·  Year " + student.getCurrentYear() + " · Sem " + student.getCurrentSemester(),
                12, Font.PLAIN, new Color(0xCC, 0xDD, 0xFF));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(name);
        left.add(info);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, RED);
        logoutBtn.addActionListener(e -> app.onLogout());

        header.add(left, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    // ── Tab 1: My Profile ─────────────────────────────────────────────────────

    private JScrollPane buildProfileTab() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 8, 6, 24);

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor    = GridBagConstraints.WEST;
        vc.fill      = GridBagConstraints.HORIZONTAL;
        vc.weightx   = 1;
        vc.gridwidth = GridBagConstraints.REMAINDER;
        vc.insets    = new Insets(6, 0, 6, 8);

        String[][] rows = {
            {"Student ID",      student.getStudentId()},
            {"Full Name",       student.getFullName()},
            {"Programme",       student.getProgramme().name() + "  —  " + student.getProgramme().getFullName()},
            {"Year / Semester", "Year " + student.getCurrentYear() + "  ·  Semester " + student.getCurrentSemester()},
            {"Year Admitted",   String.valueOf(student.getYearOfAdmission())},
            {"Address",         blank(student.getAddress())},
            {"Email",           blank(student.getEmail())},
            {"Phone",           blank(student.getPhone())},
            {"Password",        student.isTemporaryPassword() ? "Temporary — please change" : "Permanent"},
        };

        for (int i = 0; i < rows.length; i++) {
            lc.gridy = i; vc.gridy = i;
            card.add(styledLabel(rows[i][0], 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
            JLabel val = styledLabel(rows[i][1], 13, Font.PLAIN, Color.BLACK);
            if (i == rows.length - 1 && student.isTemporaryPassword()) val.setForeground(RED);
            card.add(val, vc);
        }

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = rows.length; bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.anchor = GridBagConstraints.EAST; bc.insets = new Insets(16, 0, 0, 8);
        JButton changePwdBtn = new JButton("Change Password");
        styleButton(changePwdBtn, NAVY);
        changePwdBtn.addActionListener(e -> showChangePasswordDialog());
        card.add(changePwdBtn, bc);

        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setBackground(new Color(0xF0, 0xF2, 0xF5));
        anchor.setBorder(new EmptyBorder(20, 40, 20, 40));
        anchor.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(anchor,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    public void showChangePasswordDialog() {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Change Password", true);
        dlg.setLayout(new GridBagLayout());
        dlg.setSize(380, 290);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 16, 2, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1; fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 16, 8, 16);

        JPasswordField currentF = new JPasswordField();
        JPasswordField newF     = new JPasswordField();
        JPasswordField confirmF = new JPasswordField();
        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(RED);
        errorLbl.setFont(errorLbl.getFont().deriveFont(Font.PLAIN, 11f));

        int row = 0;
        lc.gridy = row; fc.gridy = row++; dlg.add(new JLabel("Current Password"), lc);        dlg.add(currentF, fc);
        lc.gridy = row; fc.gridy = row++; dlg.add(new JLabel("New Password (min 6 chars)"), lc); dlg.add(newF, fc);
        lc.gridy = row; fc.gridy = row++; dlg.add(new JLabel("Confirm New Password"), lc);    dlg.add(confirmF, fc);

        GridBagConstraints ec = new GridBagConstraints();
        ec.gridy = row++; ec.gridwidth = GridBagConstraints.REMAINDER;
        ec.insets = new Insets(0, 16, 0, 16);
        dlg.add(errorLbl, ec);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        JButton save   = new JButton("Save");
        cancel.setPreferredSize(new Dimension(90, 34));
        save.setPreferredSize(new Dimension(90, 34));
        styleButton(save, NAVY);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String cur  = new String(currentF.getPassword()).trim();
            String np   = new String(newF.getPassword()).trim();
            String conf = new String(confirmF.getPassword()).trim();
            if (np.length() < 6)  { errorLbl.setText("Password must be at least 6 characters."); return; }
            if (!np.equals(conf)) { errorLbl.setText("Passwords do not match."); return; }
            if (!authService.changePassword(student, cur, np)) {
                errorLbl.setText("Current password is incorrect."); return;
            }
            JOptionPane.showMessageDialog(dlg, "Password changed successfully.", "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        btns.add(cancel); btns.add(save);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = row; bc.gridwidth = GridBagConstraints.REMAINDER; bc.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(btns, bc);
        dlg.setVisible(true);
    }

    // ── Tab 2: Register Courses ───────────────────────────────────────────────

    private JPanel buildRegisterTab() {
        if (student.getCurrentSemester() > 1 && !hasSubmittedSemester(student.getCurrentSemester() - 1)) {
            return buildSemesterGatePanel(student.getCurrentSemester() - 1);
        }

        existingRegistration = findCurrentSemesterRegistration();
        Set<Course> alreadyRegistered = existingRegistration != null
                ? existingRegistration.getSelectedCourses() : Collections.emptySet();

        if (existingRegistration != null && alreadyRegistered.size() >= Registration.MAX_COURSES) {
            return buildMaxReachedPanel(alreadyRegistered.size());
        }

        boolean isTopUp = existingRegistration != null;

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        availableCourses = student.viewAvailableCourses().stream()
                .filter(c -> c.getProgramme() == student.getProgramme())
                .filter(c -> !alreadyRegistered.contains(c))
                .collect(Collectors.toList());

        availableModel = new DefaultTableModel(new String[]{"Code", "Title", "Units"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshAvailableModel();
        JTable availTable = new JTable(availableModel);
        styleTable(availTable);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Courses"));
        leftPanel.add(new JScrollPane(availTable), BorderLayout.CENTER);

        selectedModel = new DefaultTableModel(new String[]{"Code", "Title", "Units"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable selTable = new JTable(selectedModel);
        styleTable(selTable);

        String initialHint = isTopUp
                ? "Adding: 0  ·  registered: " + alreadyRegistered.size()
                  + "  ·  max " + Registration.MAX_COURSES
                : "Selected: 0  (min " + Registration.MIN_COURSES + " · max " + Registration.MAX_COURSES + ")";
        selectionCountLabel = styledLabel(initialHint, 12, Font.BOLD, NAVY);
        selectionCountLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("My Selection"));
        rightPanel.add(selectionCountLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(selTable), BorderLayout.CENTER);

        JButton addBtn    = new JButton("Add →");
        JButton removeBtn = new JButton("← Remove");
        conventionButton(addBtn,    GREEN);
        conventionButton(removeBtn, RED);
        addBtn.addActionListener(e -> addSelectedCourse(availTable));
        removeBtn.addActionListener(e -> removeSelectedCourse(selTable));

        // Buttons sit between the two tables, centred vertically
        JPanel btnCol = new JPanel();
        btnCol.setLayout(new BoxLayout(btnCol, BoxLayout.Y_AXIS));
        btnCol.setOpaque(false);
        btnCol.setBorder(new EmptyBorder(0, 10, 0, 10));
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCol.add(Box.createVerticalGlue());
        btnCol.add(addBtn);
        btnCol.add(Box.createVerticalStrut(10));
        btnCol.add(removeBtn);
        btnCol.add(Box.createVerticalGlue());

        // Three-column layout: [Available] [Buttons] [Selection]
        JPanel mainArea = new JPanel(new GridBagLayout());
        mainArea.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH; gc.weighty = 1;
        gc.gridx = 0; gc.weightx = 0.55; mainArea.add(leftPanel, gc);
        gc.gridx = 1; gc.weightx = 0;    mainArea.add(btnCol, gc);
        gc.gridx = 2; gc.weightx = 0.45; mainArea.add(rightPanel, gc);

        JButton submitBtn = new JButton(isTopUp ? "Add Courses" : "Submit Registration");
        styleButton(submitBtn, NAVY);
        submitBtn.setFont(submitBtn.getFont().deriveFont(Font.BOLD, 13f));
        submitBtn.addActionListener(e -> submitRegistration());

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.add(submitBtn);

        panel.add(mainArea,  BorderLayout.CENTER);
        panel.add(bottomRow, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAvailableModel() {
        availableModel.setRowCount(0);
        for (Course c : availableCourses)
            if (!selectedCoursesList.contains(c))
                availableModel.addRow(new Object[]{c.getCourseCode(), c.getCourseTitle(), c.getUnits()});
    }

    private void addSelectedCourse(JTable availTable) {
        int row = availTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a course to add.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String code = (String) availableModel.getValueAt(row, 0);
        Course course = availableCourses.stream().filter(c -> c.getCourseCode().equals(code)).findFirst().orElse(null);
        if (course != null && !selectedCoursesList.contains(course)) {
            selectedCoursesList.add(course);
            selectedModel.addRow(new Object[]{course.getCourseCode(), course.getCourseTitle(), course.getUnits()});
            refreshAvailableModel();
            updateSelectionCount();
        }
    }

    private void removeSelectedCourse(JTable selTable) {
        int row = selTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a course to remove.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String code = (String) selectedModel.getValueAt(row, 0);
        selectedCoursesList.removeIf(c -> c.getCourseCode().equals(code));
        selectedModel.removeRow(row);
        refreshAvailableModel();
        updateSelectionCount();
    }

    private void updateSelectionCount() {
        if (existingRegistration != null) {
            int already = existingRegistration.getSelectedCourses().size();
            selectionCountLabel.setText("Adding: " + selectedCoursesList.size()
                    + "  ·  registered: " + already
                    + "  ·  max " + Registration.MAX_COURSES);
        } else {
            selectionCountLabel.setText("Selected: " + selectedCoursesList.size()
                    + "  (min " + Registration.MIN_COURSES + " · max " + Registration.MAX_COURSES + ")");
        }
    }

    private void submitRegistration() {
        if (existingRegistration != null) {
            if (selectedCoursesList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one course to add.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int currentCount = existingRegistration.getSelectedCourses().size();
            int newCount = selectedCoursesList.size();
            if (currentCount + newCount > Registration.MAX_COURSES) {
                JOptionPane.showMessageDialog(this,
                        "Adding " + newCount + " course(s) would exceed the maximum of "
                        + Registration.MAX_COURSES + ".\nYou can add at most "
                        + (Registration.MAX_COURSES - currentCount) + " more course(s).",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (Course c : selectedCoursesList) existingRegistration.addCourse(c);
            availableCourses.removeAll(selectedCoursesList);
            JOptionPane.showMessageDialog(this,
                    "Courses added successfully!\n\nSemester : " + existingRegistration.getSemester()
                    + "\nTotal courses : " + existingRegistration.getSelectedCourses().size(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            selectedCoursesList.clear();
            selectedModel.setRowCount(0);
            refreshAvailableModel();
            updateSelectionCount();
        } else {
            Registration reg = student.submitRegistration();
            for (Course c : selectedCoursesList) reg.addCourse(c);
            try {
                reg.submit();
                dataStore.saveRegistration(reg);
                existingRegistration = reg;
                JOptionPane.showMessageDialog(this,
                        "Registration submitted successfully!\n\nID       : " + reg.getRegistrationId()
                        + "\nSemester : " + reg.getSemester() + "\nCourses  : " + reg.getSelectedCourses().size(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                availableCourses.removeAll(selectedCoursesList);
                selectedCoursesList.clear();
                selectedModel.setRowCount(0);
                refreshAvailableModel();
                updateSelectionCount();
            } catch (RegistrationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Registration findCurrentSemesterRegistration() {
        int calendarYear = student.getYearOfAdmission() + (student.getCurrentYear() - 1);
        String semKey = calendarYear + "/" + student.getCurrentSemester();
        return dataStore.findRegistration(student).stream()
                .filter(r -> r.getSemester().equals(semKey) && r.isSubmitted())
                .findFirst()
                .orElse(null);
    }

    private JPanel buildMaxReachedPanel(int count) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0xF0, 0xF2, 0xF5));
        JLabel icon = styledLabel("✓", 36, Font.BOLD, GREEN);
        JLabel msg  = styledLabel(
                "You have registered " + count + " courses for this semester (maximum reached).",
                14, Font.PLAIN, new Color(0x44, 0x44, 0x44));
        JLabel sub  = styledLabel(
                "No further additions are allowed (max " + Registration.MAX_COURSES + ").",
                12, Font.PLAIN, Color.GRAY);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0, 0, 8, 0);
        panel.add(icon, gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 6, 0);
        panel.add(msg, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 0);
        panel.add(sub, gc);
        return panel;
    }

    private boolean hasSubmittedSemester(int semester) {
        int calendarYear = student.getYearOfAdmission() + (student.getCurrentYear() - 1);
        String semKey = calendarYear + "/" + semester;
        return dataStore.findRegistration(student).stream()
                .anyMatch(r -> r.getSemester().equals(semKey) && r.isSubmitted());
    }

    private JPanel buildSemesterGatePanel(int completedSemester) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(0xF0, 0xF2, 0xF5));
        JLabel icon = styledLabel("🔒", 36, Font.PLAIN, new Color(0x88, 0x88, 0x88));
        JLabel msg  = styledLabel(
                "Semester 2 courses are only available after Semester " + completedSemester
                + " registration has been submitted.",
                14, Font.PLAIN, new Color(0x44, 0x44, 0x44));
        JLabel sub  = styledLabel(
                "Please submit your Semester " + completedSemester + " registration first.",
                12, Font.PLAIN, Color.GRAY);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.insets = new Insets(0, 0, 8, 0);
        panel.add(icon, gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 6, 0);
        panel.add(msg, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 0, 0);
        panel.add(sub, gc);
        return panel;
    }

    // ── Tab 3: My Registrations & Results ─────────────────────────────────────

    private JPanel buildRegistrationsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top — registrations list
        String[] rCols = {"Semester", "Courses", "Submitted At", "Status"};
        myRegModel = new DefaultTableModel(rCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable regTable = new JTable(myRegModel);
        styleTable(regTable);
        regTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshMyRegistrations();

        // Bottom — results for selected registration
        String[] resCols = {"Code", "Course Title", "CA /40", "Exam /60", "Total /100", "Grade"};
        resultsModel = new DefaultTableModel(resCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultsTable = new JTable(resultsModel);
        styleTable(resultsTable);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(320);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(55);

        // GPA bar
        sgpaLabel    = styledLabel("—", 14, Font.BOLD, NAVY);
        cgpaLabel    = styledLabel("—", 14, Font.BOLD, NAVY);
        honoursLabel = styledLabel("—", 14, Font.BOLD, NAVY);

        JPanel gpaBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 6));
        gpaBar.setBackground(new Color(0xE8, 0xEA, 0xF6));
        gpaBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xCC, 0xCC, 0xCC)));
        gpaBar.add(gpaGroup("Semester GPA", sgpaLabel));
        gpaBar.add(vSep());
        gpaBar.add(gpaGroup("Cumulative GPA", cgpaLabel));
        gpaBar.add(vSep());
        gpaBar.add(gpaGroup("Class of Degree", honoursLabel));

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 4));
        resultsPanel.add(styledLabel("Course Results", 13, Font.BOLD, new Color(0x33, 0x33, 0x33)), BorderLayout.NORTH);
        resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        resultsPanel.add(gpaBar, BorderLayout.SOUTH);

        // Load results and update GPA when a registration row is clicked
        regTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = regTable.getSelectedRow();
            resultsModel.setRowCount(0);
            sgpaLabel.setText("—"); cgpaLabel.setText("—"); honoursLabel.setText("—");
            if (row < 0) return;
            List<Registration> regs = dataStore.findRegistration(student);
            if (row >= regs.size()) return;
            List<Result> semResults = dataStore.findResults(regs.get(row).getRegistrationId());
            for (Result res : semResults) {
                resultsModel.addRow(new Object[]{
                    res.getCourse().getCourseCode(), res.getCourse().getCourseTitle(),
                    String.format("%.1f", res.getTestScore()), String.format("%.1f", res.getExamScore()),
                    String.format("%.1f", res.getTotal()), res.getGrade()
                });
            }
            if (resultsModel.getRowCount() == 0) {
                resultsModel.addRow(new Object[]{"—", "No results recorded for this semester yet.", "", "", "", ""});
            } else {
                double sgpa = computeGPA(semResults);
                double cgpa = computeCGPA();
                sgpaLabel.setText(String.format("%.2f / 5.00", sgpa));
                cgpaLabel.setText(String.format("%.2f / 5.00", cgpa));
                honoursLabel.setText(honours(cgpa));
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(regTable), resultsPanel);
        split.setDividerLocation(160);
        split.setResizeWeight(0.35);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private void refreshMyRegistrations() {
        if (myRegModel == null) return;
        myRegModel.setRowCount(0);
        for (Registration r : dataStore.findRegistration(student)) {
            myRegModel.addRow(new Object[]{
                r.getSemester(), r.getSelectedCourses().size() + " course(s)",
                r.isSubmitted() ? r.getSubmittedAt().toString().substring(0, 16) : "—",
                r.isSubmitted() ? "Submitted" : "Pending"
            });
        }
    }

    // ── GPA helpers ───────────────────────────────────────────────────────────

    private double computeGPA(List<Result> results) {
        double weighted = 0; int units = 0;
        for (Result r : results) {
            int u = r.getCourse().getUnits();
            if (u > 0) { weighted += r.getGradePoint() * u; units += u; }
        }
        return units > 0 ? weighted / units : 0;
    }

    private double computeCGPA() {
        double weighted = 0; int units = 0;
        for (Registration reg : dataStore.findRegistration(student))
            for (Result r : dataStore.findResults(reg.getRegistrationId())) {
                int u = r.getCourse().getUnits();
                if (u > 0) { weighted += r.getGradePoint() * u; units += u; }
            }
        return units > 0 ? weighted / units : 0;
    }

    private static String honours(double cgpa) {
        if (cgpa >= 4.50) return "First Class";
        if (cgpa >= 3.50) return "Second Class Upper";
        if (cgpa >= 2.40) return "Second Class Lower";
        if (cgpa >= 1.50) return "Third Class";
        if (cgpa >= 1.00) return "Pass";
        return "Fail";
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private static String blank(String s) { return (s == null || s.isEmpty()) ? "—" : s; }

    private static JPanel gpaGroup(String caption, JLabel value) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xE8, 0xEA, 0xF6));
        JLabel cap = new JLabel(caption);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 11));
        cap.setForeground(Color.GRAY);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cap); p.add(value);
        return p;
    }

    private static JSeparator vSep() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 36));
        s.setForeground(new Color(0xCC, 0xCC, 0xCC));
        return s;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void conventionButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 16, 8, 16));
        Dimension size = new Dimension(120, 36);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        btn.setMinimumSize(size);
    }

    private JLabel styledLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }
}
