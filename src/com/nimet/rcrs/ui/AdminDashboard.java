package com.nimet.rcrs.ui;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminDashboard extends JPanel {

    private static final Color NAVY   = new Color(0x1B, 0x3A, 0x5C);
    private static final Color GREEN  = new Color(0x28, 0x7A, 0x3B);
    private static final Color RED    = new Color(0xAA, 0x33, 0x22);
    private static final Color BG     = new Color(0xF0, 0xF2, 0xF5);

    private final RcrsApp   app;
    private final Admin     admin;
    private final DataStore dataStore;
    private final Catalogue catalogue;

    // Register-student tab
    private JTextField    nameField, addressField, emailField, phoneField;
    private JComboBox<Programme> programmeCombo;
    private JPanel        resultCard;
    private JLabel        resultIdLabel, resultLoginLabel, resultPwdLabel;

    // Course catalogue tab
    private DefaultTableModel catalogueModel;

    // Students tab
    private DefaultTableModel studentModel;
    private DefaultTableModel regModel;
    private List<Registration> currentStudentRegs = new ArrayList<>();
    private Registration selectedReg = null;
    private Student      selectedStudentForResults = null;
    private JButton      enterResultsBtn;
    private JButton      editProfileBtn;

    public AdminDashboard(RcrsApp app, Admin admin, DataStore dataStore,
                          Catalogue catalogue, AuthenticationService authService) {
        this.app       = app;
        this.admin     = admin;
        this.dataStore = dataStore;
        this.catalogue = catalogue;

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Register Student", buildRegisterStudentTab());
        tabs.addTab("Course Catalogue", buildCatalogueTab());
        tabs.addTab("Students",         buildStudentsTab());
        tabs.addTab("Reports",          buildReportsTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Header ─────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel name    = styledLabel("Admin: " + admin.getFullName(), 16, Font.BOLD, Color.WHITE);
        JLabel staffId = styledLabel(admin.getStaffId(), 12, Font.PLAIN, new Color(0xCC, 0xDD, 0xFF));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(name);
        left.add(staffId);

        JButton logoutBtn = new JButton("Logout");
        styleButton(logoutBtn, RED);
        logoutBtn.addActionListener(e -> app.onLogout());

        header.add(left,      BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    // ── Tab 1: Register Student ────────────────────────────────────────────────

    private JScrollPane buildRegisterStudentTab() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(24, 40, 24, 40));

        JLabel heading = styledLabel("Register New Student", 22, Font.BOLD, NAVY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = styledLabel(
            "Fill in the student's details. A unique student ID and temporary password will be generated.",
            12, Font.PLAIN, Color.GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(heading);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(sub);
        wrapper.add(Box.createVerticalStrut(20));

        // Form card
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDF, 0xE6, 0xE9), 1),
            new EmptyBorder(20, 24, 20, 24)));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 2, 16);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 0, 10, 0);

        int row = 0;

        lc.gridy = row; fc.gridy = row++;
        formCard.add(styledLabel("Full Name  *", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        nameField = new JTextField();
        formCard.add(nameField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(styledLabel("Address", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        addressField = new JTextField();
        formCard.add(addressField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(styledLabel("Contact Email", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        emailField = new JTextField();
        formCard.add(emailField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(styledLabel("Phone Number", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        phoneField = new JTextField();
        formCard.add(phoneField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(styledLabel("Programme  *", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        programmeCombo = new JComboBox<>(Programme.values());
        programmeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Programme) {
                    Programme p = (Programme) value;
                    setText(p.name() + "  —  " + p.getFullName());
                }
                return this;
            }
        });
        formCard.add(programmeCombo, fc);

        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = row; bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.anchor = GridBagConstraints.EAST; bc.insets = new Insets(10, 0, 0, 0);
        JButton registerBtn = new JButton("Register Student");
        styleButton(registerBtn, NAVY);
        registerBtn.setPreferredSize(new Dimension(200, 38));
        registerBtn.addActionListener(e -> doRegister());
        formCard.add(registerBtn, bc);

        wrapper.add(formCard);
        wrapper.add(Box.createVerticalStrut(20));

        // Result card (hidden until registration succeeds)
        resultCard = buildResultCard();
        resultCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultCard.setVisible(false);
        wrapper.add(resultCard);

        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setBackground(BG);
        anchor.add(wrapper, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(anchor,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(0xE8, 0xF5, 0xE9));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x00, 0xB8, 0x94), 2),
            new EmptyBorder(16, 24, 16, 24)));

        JLabel header = styledLabel("Student Registered Successfully", 15, Font.BOLD, new Color(0x1B, 0x5E, 0x20));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = styledLabel(
            "Share these credentials with the student. They will be prompted to change their password on first login.",
            12, Font.PLAIN, Color.GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(header);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 8));
        grid.setBackground(new Color(0xE8, 0xF5, 0xE9));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(520, 90));

        grid.add(styledLabel("Student ID :", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)));
        resultIdLabel = new JLabel();
        resultIdLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        resultIdLabel.setForeground(NAVY);
        grid.add(resultIdLabel);

        grid.add(styledLabel("Login ID :", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)));
        resultLoginLabel = new JLabel();
        resultLoginLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        resultLoginLabel.setForeground(NAVY);
        grid.add(resultLoginLabel);

        grid.add(styledLabel("Temporary Password :", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)));
        resultPwdLabel = new JLabel();
        resultPwdLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        resultPwdLabel.setForeground(RED);
        grid.add(resultPwdLabel);

        card.add(grid);
        card.add(Box.createVerticalStrut(14));

        JButton again = new JButton("Register Another Student");
        again.setAlignmentX(Component.LEFT_ALIGNMENT);
        again.addActionListener(e -> {
            clearForm();
            resultCard.setVisible(false);
        });
        card.add(again);
        return card;
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        String address = addressField.getText().trim();
        String email   = emailField.getText().trim();
        String phone   = phoneField.getText().trim();
        Programme prog = (Programme) programmeCombo.getSelectedItem();
        String tempPwd = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        Student student = admin.registerStudent(name, address, email, phone, prog, tempPwd);

        resultIdLabel.setText(student.getStudentId());
        resultLoginLabel.setText(student.getUserId());
        resultPwdLabel.setText(tempPwd);
        resultCard.setVisible(true);

        refreshStudentTable();
    }

    private void clearForm() {
        nameField.setText("");
        addressField.setText("");
        emailField.setText("");
        phoneField.setText("");
        programmeCombo.setSelectedIndex(0);
    }

    // ── Tab 2: Course Catalogue ────────────────────────────────────────────────

    private JPanel buildCatalogueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        String[] cols = {"Code", "Title", "Programme", "Level", "Sem", "Units"};
        catalogueModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        refreshCatalogueModel();
        JTable table = new JTable(catalogueModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(45);
        table.getColumnModel().getColumn(4).setPreferredWidth(38);
        table.getColumnModel().getColumn(5).setPreferredWidth(46);

        JButton addBtn = new JButton("Add Course");
        styleButton(addBtn, GREEN);
        addBtn.addActionListener(e -> showAddCourseDialog());

        JButton removeBtn = new JButton("Remove Selected");
        styleButton(removeBtn, RED);
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a course to remove first."); return; }
            String code = (String) catalogueModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove course '" + code + "'? This cannot be undone.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                admin.removeCourse(code);
                catalogueModel.removeRow(row);
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(addBtn);
        toolbar.add(removeBtn);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void refreshCatalogueModel() {
        catalogueModel.setRowCount(0);
        for (Course c : catalogue.getAllCourses()) {
            catalogueModel.addRow(new Object[]{
                c.getCourseCode(), c.getCourseTitle(),
                c.getProgramme().name(), c.getLevel(), c.getSemester(), c.getUnits()
            });
        }
    }

    private void showAddCourseDialog() {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Add Course", true);
        dlg.setLayout(new GridBagLayout());
        dlg.setSize(440, 380);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST; lc.insets = new Insets(8, 16, 2, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL; fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER; fc.insets = new Insets(0, 16, 8, 16);

        JTextField codeF   = new JTextField();
        JTextField titleF  = new JTextField();
        JComboBox<Programme> progC = new JComboBox<>(Programme.values());
        JSpinner levelS = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        JSpinner semS   = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
        JSpinner unitsS = new JSpinner(new SpinnerNumberModel(3, 0, 12, 1));

        String[]    labels = {"Course Code", "Course Title", "Programme", "Level (Year)", "Semester", "Credit Units"};
        Component[] comps  = {codeF, titleF, progC, levelS, semS, unitsS};
        for (int i = 0; i < comps.length; i++) {
            lc.gridy = i; fc.gridy = i;
            dlg.add(new JLabel(labels[i]), lc);
            dlg.add(comps[i], fc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        cancel.setPreferredSize(new Dimension(90, 34));
        JButton save = new JButton("Add");
        styleButton(save, NAVY);
        save.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String code  = codeF.getText().trim().toUpperCase();
            String title = titleF.getText().trim();
            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Code and Title are required."); return;
            }
            Course c = new Course(code, title, (Programme) progC.getSelectedItem(),
                (int) levelS.getValue(), (int) semS.getValue(), (int) unitsS.getValue());
            dataStore.saveCourse(c);
            catalogue.addCourse(c);
            refreshCatalogueModel();
            dlg.dispose();
        });
        btns.add(cancel); btns.add(save);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = comps.length; bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(btns, bc);
        dlg.setVisible(true);
    }

    // ── Tab 3: Students ────────────────────────────────────────────────────────

    private JPanel buildStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 12, 4, 12));

        String[] sCols = {"Student ID", "Full Name", "Programme", "Year", "Sem", "Email", "Phone"};
        studentModel = new DefaultTableModel(sCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable studentTable = new JTable(studentModel);
        styleTable(studentTable);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(35);

        String[] rCols = {"Semester", "Courses", "Submitted At", "Status"};
        regModel = new DefaultTableModel(rCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable regTable = new JTable(regModel);
        styleTable(regTable);

        enterResultsBtn = new JButton("Enter / Edit Results");
        styleButton(enterResultsBtn, NAVY);
        enterResultsBtn.setPreferredSize(new Dimension(190, 34));
        enterResultsBtn.setEnabled(false);
        enterResultsBtn.addActionListener(e -> {
            if (selectedReg != null && selectedStudentForResults != null)
                showEnterResultsDialog(selectedStudentForResults, selectedReg);
        });

        editProfileBtn = new JButton("View / Edit Profile");
        editProfileBtn.setPreferredSize(new Dimension(175, 34));
        editProfileBtn.setEnabled(false);
        editProfileBtn.addActionListener(e -> {
            if (selectedStudentForResults != null)
                showEditProfileDialog(selectedStudentForResults);
        });

        JPanel regToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        regToolbar.add(editProfileBtn);
        regToolbar.add(enterResultsBtn);

        JPanel regSection = new JPanel(new BorderLayout(0, 4));
        regSection.add(new JScrollPane(regTable), BorderLayout.CENTER);
        regSection.add(regToolbar, BorderLayout.SOUTH);

        studentTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = studentTable.getSelectedRow();
            regModel.setRowCount(0);
            selectedReg = null; selectedStudentForResults = null;
            enterResultsBtn.setEnabled(false); editProfileBtn.setEnabled(false);
            if (row < 0) return;
            String sid = (String) studentModel.getValueAt(row, 0);
            selectedStudentForResults = dataStore.loadStudent(sid);
            if (selectedStudentForResults == null) return;
            editProfileBtn.setEnabled(true);
            currentStudentRegs = new ArrayList<>(dataStore.findRegistration(selectedStudentForResults));
            for (Registration r : currentStudentRegs) {
                regModel.addRow(new Object[]{
                    r.getSemester(), r.getSelectedCourses().size() + " course(s)",
                    r.isSubmitted() ? r.getSubmittedAt().toString().substring(0, 16) : "—",
                    r.isSubmitted() ? "Submitted" : "Pending"
                });
            }
        });

        regTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = regTable.getSelectedRow();
            if (row < 0 || row >= currentStudentRegs.size()) {
                selectedReg = null; enterResultsBtn.setEnabled(false);
            } else {
                selectedReg = currentStudentRegs.get(row); enterResultsBtn.setEnabled(true);
            }
        });

        refreshStudentTable();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(studentTable), regSection);
        split.setDividerLocation(240);
        split.setResizeWeight(0.55);

        JLabel hint = styledLabel(
            "  Select a student → select a registration → click Enter / Edit Results",
            11, Font.PLAIN, Color.GRAY);

        panel.add(split,  BorderLayout.CENTER);
        panel.add(hint,   BorderLayout.SOUTH);
        return panel;
    }

    private void showEnterResultsDialog(Student s, Registration reg) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Results — " + s.getFullName() + " — Semester " + reg.getSemester(), true);
        dlg.setSize(760, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 8));
        dlg.getRootPane().setBorder(new EmptyBorder(12, 16, 12, 16));

        String[] cols = {"Code", "Course Title", "CA /40", "Exam /60", "Total /100", "Grade"};
        boolean[] updating = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 2 || c == 3; }
        };

        List<Result> existing = dataStore.findResults(reg.getRegistrationId());
        java.util.Map<String, Result> existingByCode = new java.util.HashMap<>();
        for (Result r : existing) existingByCode.put(r.getCourse().getCourseCode(), r);

        for (Course c : reg.getSelectedCourses()) {
            Result r = existingByCode.get(c.getCourseCode());
            double ca   = r != null ? r.getTestScore() : 0;
            double exam = r != null ? r.getExamScore()  : 0;
            model.addRow(new Object[]{c.getCourseCode(), c.getCourseTitle(), ca, exam, ca + exam, dialogGrade(ca + exam)});
        }

        model.addTableModelListener(ev -> {
            if (updating[0]) return;
            int col = ev.getColumn();
            if (col != 2 && col != 3) return;
            int row = ev.getFirstRow();
            updating[0] = true;
            try {
                double ca   = toDouble(model.getValueAt(row, 2));
                double exam = toDouble(model.getValueAt(row, 3));
                ca   = Math.max(0, Math.min(40, ca));
                exam = Math.max(0, Math.min(60, exam));
                double tot = ca + exam;
                model.setValueAt(ca, row, 2);   model.setValueAt(exam, row, 3);
                model.setValueAt(tot, row, 4);  model.setValueAt(dialogGrade(tot), row, 5);
            } finally { updating[0] = false; }
        });

        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(4).setPreferredWidth(75);
        table.getColumnModel().getColumn(5).setPreferredWidth(55);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        // Live GPA bar
        JLabel gpaLbl = styledLabel(computeDialogGPA(model, reg), 13, Font.BOLD, NAVY);
        gpaLbl.setBorder(new EmptyBorder(0, 4, 0, 0));
        model.addTableModelListener(ev -> gpaLbl.setText(computeDialogGPA(model, reg)));

        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(new Color(0xE8, 0xEA, 0xF6));
        infoBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDF, 0xE6, 0xE9)),
            new EmptyBorder(4, 8, 4, 8)));
        infoBar.add(styledLabel("CA = Continuous Assessment (max 40)   ·   Exam = Examination (max 60)",
            11, Font.PLAIN, Color.GRAY), BorderLayout.WEST);
        infoBar.add(gpaLbl, BorderLayout.EAST);
        dlg.add(infoBar, BorderLayout.NORTH);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btns.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 34));
        JButton save = new JButton("Save Results");
        styleButton(save, GREEN);
        save.setPreferredSize(new Dimension(130, 34));

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            List<Result> results = new ArrayList<>();
            List<Course> courses = new ArrayList<>(reg.getSelectedCourses());
            for (int i = 0; i < model.getRowCount(); i++) {
                double ca   = toDouble(model.getValueAt(i, 2));
                double exam = toDouble(model.getValueAt(i, 3));
                results.add(new Result(reg.getRegistrationId(), courses.get(i), ca, exam));
            }
            dataStore.saveResults(reg.getRegistrationId(), results);
            JOptionPane.showMessageDialog(dlg, "Results saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        btns.add(cancel); btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static String dialogGrade(double total) {
        if (total >= 70) return "A";
        if (total >= 60) return "B";
        if (total >= 50) return "C";
        if (total >= 45) return "D";
        if (total >= 40) return "E";
        return "F";
    }

    private static double toDouble(Object val) {
        if (val == null) return 0;
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private String computeDialogGPA(DefaultTableModel model, Registration reg) {
        List<Course> courses = new ArrayList<>(reg.getSelectedCourses());
        double wp = 0; int units = 0;
        for (int i = 0; i < model.getRowCount() && i < courses.size(); i++) {
            int u = courses.get(i).getUnits();
            if (u <= 0) continue;
            double tot = toDouble(model.getValueAt(i, 2)) + toDouble(model.getValueAt(i, 3));
            double gp  = tot >= 70 ? 5 : tot >= 60 ? 4 : tot >= 50 ? 3 : tot >= 45 ? 2 : tot >= 40 ? 1 : 0;
            wp += gp * u; units += u;
        }
        return units == 0 ? "Semester GPA: —"
            : String.format("Semester GPA: %.2f / 5.00", wp / units);
    }

    public void refreshStudentTable() {
        if (studentModel == null) return;
        studentModel.setRowCount(0);
        for (Student st : dataStore.getAllStudents()) {
            studentModel.addRow(new Object[]{
                st.getStudentId(), st.getFullName(), st.getProgramme().name(),
                st.getCurrentYear(), st.getCurrentSemester(), st.getEmail(), st.getPhone()
            });
        }
    }

    // ── Tab 4: Reports ─────────────────────────────────────────────────────────

    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(8, 8, 8, 8));
        reportArea.setText("Click 'Generate Report' to produce the student registration summary.");

        JButton generateBtn = new JButton("Generate Report");
        styleButton(generateBtn, NAVY);
        generateBtn.addActionListener(e -> {
            reportArea.setText(admin.generateStudentReport());
            reportArea.setCaretPosition(0);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(generateBtn);

        panel.add(toolbar,                     BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }

    private void showEditProfileDialog(Student s) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Student Profile — " + s.getStudentId(), true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 2, 16);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 0, 6, 0);

        int row = 0;

        // Read-only fields
        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Student ID  (read-only)", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JLabel idLbl = new JLabel(s.getStudentId());
        idLbl.setFont(new Font("Monospaced", Font.BOLD, 13));
        idLbl.setForeground(NAVY);
        form.add(idLbl, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Full Name  (read-only)", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        form.add(styledLabel(s.getFullName(), 13, Font.PLAIN, Color.DARK_GRAY), fc);

        // Editable fields
        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Address", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JTextField addressF = new JTextField(s.getAddress());
        form.add(addressF, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Email", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JTextField emailF = new JTextField(s.getEmail());
        form.add(emailF, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Phone", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JTextField phoneF = new JTextField(s.getPhone());
        form.add(phoneF, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Programme", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JComboBox<Programme> progC = new JComboBox<>(Programme.values());
        progC.setSelectedItem(s.getProgramme());
        form.add(progC, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Current Year", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JSpinner yearS = new JSpinner(new SpinnerNumberModel(s.getCurrentYear(), 1, 4, 1));
        form.add(yearS, fc);

        lc.gridy = row; fc.gridy = row++;
        form.add(styledLabel("Current Semester", 13, Font.BOLD, new Color(0x44, 0x44, 0x44)), lc);
        JSpinner semS = new JSpinner(new SpinnerNumberModel(s.getCurrentSemester(), 1, 2, 1));
        form.add(semS, fc);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btns.setOpaque(false);
        JButton cancel = new JButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 34));
        JButton save = new JButton("Save Changes");
        styleButton(save, GREEN);
        save.setPreferredSize(new Dimension(130, 34));

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            admin.updateStudentProfile(s,
                addressF.getText().trim(),
                emailF.getText().trim(),
                phoneF.getText().trim(),
                (Programme) progC.getSelectedItem(),
                (int) yearS.getValue(),
                (int) semS.getValue());
            refreshStudentTable();
            JOptionPane.showMessageDialog(dlg, "Profile updated successfully.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        btns.add(cancel);
        btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ── UI helpers ─────────────────────────────────────────────────────────────

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

    private JLabel styledLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }
}
