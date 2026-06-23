package com.nimet.rcrs.gui;

import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminPanel extends JPanel {

    private final Admin admin;
    private final DataStore dataStore;
    private final Catalogue catalogue;
    private final Runnable onLogout;

    // Register-student tab
    private JTextField nameField, addressField, emailField, phoneField;
    private JComboBox<Programme> programmeCombo;
    private JPanel resultCard;
    private JLabel resultIdLabel, resultLoginLabel, resultPwdLabel;

    // Course catalogue tab
    private DefaultTableModel courseModel;

    // Students tab
    private DefaultTableModel studentModel;
    private DefaultTableModel regModel;
    private List<Registration> currentStudentRegs = new ArrayList<>();
    private Registration selectedReg = null;
    private Student selectedStudentForResults = null;
    private JButton enterResultsBtn;
    private JButton editProfileBtn;

    public AdminPanel(Admin admin, DataStore dataStore, Catalogue catalogue,
                      AuthenticationService authService, Runnable onLogout) {
        this.admin     = admin;
        this.dataStore = dataStore;
        this.catalogue = catalogue;
        this.onLogout  = onLogout;
        buildUI();
    }

    // ── Shell ─────────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PAGE);
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_LABEL);
        tabs.addTab("  Register Student  ", buildRegisterStudentTab());
        tabs.addTab("  Course Catalogue  ", buildCatalogueTab());
        tabs.addTab("  Students  ",         buildStudentsTab());
        tabs.addTab("  Report  ",           buildReportTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(UITheme.PRIMARY);
        JLabel title = new JLabel("NiMet RCRS  ·  Admin Dashboard");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("   —   " + admin.getFullName() + "  (" + admin.getStaffId() + ")");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(180, 210, 240));
        left.add(title);
        left.add(sub);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setBackground(UITheme.ERROR);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> onLogout.run());

        header.add(left, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    // ── Tab 1 · Register Student ──────────────────────────────────────────────

    private JScrollPane buildRegisterStudentTab() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(UITheme.BG_PAGE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        // ── Heading ───────────────────────────────────────────────────────────
        JLabel heading = new JLabel("Register New Student");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = UITheme.muted(
            "Fill in the student's details. A unique student ID and temporary password will be generated.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        wrapper.add(heading);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(sub);
        wrapper.add(Box.createVerticalStrut(20));

        // ── Form card ─────────────────────────────────────────────────────────
        JPanel formCard = UITheme.card();
        formCard.setLayout(new GridBagLayout());
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
        formCard.add(UITheme.label("Full Name  *"), lc);
        nameField = UITheme.styledField();
        formCard.add(nameField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(UITheme.label("Address"), lc);
        addressField = UITheme.styledField();
        formCard.add(addressField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(UITheme.label("Contact Email"), lc);
        emailField = UITheme.styledField();
        formCard.add(emailField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(UITheme.label("Phone Number"), lc);
        phoneField = UITheme.styledField();
        formCard.add(phoneField, fc);

        lc.gridy = row; fc.gridy = row++;
        formCard.add(UITheme.label("Programme  *"), lc);
        programmeCombo = new JComboBox<>(Programme.values());
        programmeCombo.setFont(UITheme.FONT_BODY);
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

        // Register button (right-aligned)
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = row;
        bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.anchor = GridBagConstraints.EAST;
        bc.insets = new Insets(10, 0, 0, 0);
        JButton registerBtn = UITheme.primaryButton("Register Student");
        registerBtn.setPreferredSize(new Dimension(200, 42));
        registerBtn.addActionListener(e -> doRegister());
        formCard.add(registerBtn, bc);

        wrapper.add(formCard);
        wrapper.add(Box.createVerticalStrut(20));

        // ── Result card (hidden until registration) ───────────────────────────
        resultCard = buildResultCard();
        resultCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultCard.setVisible(false);
        wrapper.add(resultCard);

        // Anchor wrapper to top so it doesn't stretch vertically
        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setBackground(UITheme.BG_PAGE);
        anchor.add(wrapper, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(anchor,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.SUCCESS_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.SUCCESS, 2),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel header = new JLabel("Student Registered Successfully");
        header.setFont(UITheme.FONT_HEADING);
        header.setForeground(new Color(0x1b5e20));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = UITheme.muted(
            "Share these credentials with the student. They will be prompted to change their password on first login.");
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(header);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(16));

        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 8));
        grid.setBackground(UITheme.SUCCESS_BG);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(520, 90));

        grid.add(UITheme.label("Student ID :"));
        resultIdLabel = new JLabel();
        resultIdLabel.setFont(UITheme.FONT_MONO);
        resultIdLabel.setForeground(UITheme.PRIMARY);
        grid.add(resultIdLabel);

        grid.add(UITheme.label("Login ID :"));
        resultLoginLabel = new JLabel();
        resultLoginLabel.setFont(UITheme.FONT_MONO);
        resultLoginLabel.setForeground(UITheme.PRIMARY);
        grid.add(resultLoginLabel);

        grid.add(UITheme.label("Temporary Password :"));
        resultPwdLabel = new JLabel();
        resultPwdLabel.setFont(UITheme.FONT_MONO);
        resultPwdLabel.setForeground(new Color(0xc0392b));
        grid.add(resultPwdLabel);

        card.add(grid);
        card.add(Box.createVerticalStrut(14));

        JButton again = UITheme.secondaryButton("Register Another Student");
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
            JOptionPane.showMessageDialog(this, "Full name is required.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }
        String address  = addressField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        Programme prog  = (Programme) programmeCombo.getSelectedItem();

        String tempPwd = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase();

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

    // ── Tab 2 · Course Catalogue ──────────────────────────────────────────────

    private JPanel buildCatalogueTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Code", "Title", "Programme", "Level", "Sem", "Units"};
        courseModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(courseModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(45);
        table.getColumnModel().getColumn(4).setPreferredWidth(38);
        table.getColumnModel().getColumn(5).setPreferredWidth(46);
        loadCourseTable();
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(UITheme.BG_PAGE);

        JButton addBtn = UITheme.primaryButton("Add Course");
        addBtn.setPreferredSize(new Dimension(130, 34));
        addBtn.addActionListener(e -> showAddCourseDialog());

        JButton removeBtn = UITheme.dangerButton("Remove Selected");
        removeBtn.setPreferredSize(new Dimension(150, 34));
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a course to remove first.");
                return;
            }
            String code = (String) courseModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Remove course '" + code + "'? This cannot be undone.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                admin.removeCourse(code);
                courseModel.removeRow(row);
            }
        });

        toolbar.add(addBtn);
        toolbar.add(removeBtn);
        toolbar.add(UITheme.muted("  " + catalogue.getAllCourses().size() + " courses in catalogue"));
        panel.add(toolbar, BorderLayout.NORTH);
        return panel;
    }

    private void loadCourseTable() {
        courseModel.setRowCount(0);
        for (Course c : catalogue.getAllCourses()) {
            courseModel.addRow(new Object[]{
                c.getCourseCode(), c.getCourseTitle(),
                c.getProgramme().name(), c.getLevel(), c.getSemester(), c.getUnits()
            });
        }
    }

    private void showAddCourseDialog() {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Add Course", true);
        dlg.setLayout(new GridBagLayout());
        dlg.setSize(440, 400);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 16, 2, 8);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 16, 8, 16);

        JTextField codeF   = UITheme.styledField();
        JTextField titleF  = UITheme.styledField();
        JComboBox<Programme> progC = new JComboBox<>(Programme.values());
        progC.setFont(UITheme.FONT_BODY);
        JSpinner levelS = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        JSpinner semS   = new JSpinner(new SpinnerNumberModel(1, 1, 2, 1));
        JSpinner unitsS = new JSpinner(new SpinnerNumberModel(3, 0, 12, 1));

        String[] labels = {"Course Code", "Course Title", "Programme", "Level (Year)", "Semester", "Credit Units"};
        Component[] comps = {codeF, titleF, progC, levelS, semS, unitsS};

        for (int i = 0; i < comps.length; i++) {
            lc.gridy = i; fc.gridy = i;
            dlg.add(UITheme.label(labels[i]), lc);
            dlg.add(comps[i], fc);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setOpaque(false);
        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(90, 34));
        JButton save = UITheme.primaryButton("Add");
        save.setPreferredSize(new Dimension(90, 34));

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String code  = codeF.getText().trim().toUpperCase();
            String title = titleF.getText().trim();
            if (code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Code and Title are required.");
                return;
            }
            Course c = new Course(code, title, (Programme) progC.getSelectedItem(),
                (int) levelS.getValue(), (int) semS.getValue(), (int) unitsS.getValue());
            dataStore.saveCourse(c);
            catalogue.addCourse(c);
            loadCourseTable();
            dlg.dispose();
        });

        btns.add(cancel);
        btns.add(save);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = comps.length;
        bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.insets = new Insets(0, 0, 0, 0);
        bc.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(btns, bc);
        dlg.setVisible(true);
    }

    // ── Tab 3 · Students ──────────────────────────────────────────────────────

    private JPanel buildStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 4, 16));

        // ── Students table (top) ──────────────────────────────────────────────
        String[] sCols = {"Student ID", "Full Name", "Programme", "Year", "Sem", "Email", "Phone"};
        studentModel = new DefaultTableModel(sCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable studentTable = new JTable(studentModel);
        studentTable.setFont(UITheme.FONT_BODY);
        studentTable.setRowHeight(24);
        studentTable.getTableHeader().setFont(UITheme.FONT_LABEL);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(35);

        // ── Registrations table + toolbar (bottom) ────────────────────────────
        String[] rCols = {"Semester", "Courses", "Submitted At", "Status"};
        regModel = new DefaultTableModel(rCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable regTable = new JTable(regModel);
        regTable.setFont(UITheme.FONT_BODY);
        regTable.setRowHeight(24);
        regTable.getTableHeader().setFont(UITheme.FONT_LABEL);
        regTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        enterResultsBtn = UITheme.primaryButton("Enter / Edit Results");
        enterResultsBtn.setPreferredSize(new Dimension(190, 34));
        enterResultsBtn.setEnabled(false);
        enterResultsBtn.addActionListener(e -> {
            if (selectedReg != null && selectedStudentForResults != null)
                showEnterResultsDialog(selectedStudentForResults, selectedReg);
        });

        editProfileBtn = UITheme.secondaryButton("View / Edit Profile");
        editProfileBtn.setPreferredSize(new Dimension(175, 34));
        editProfileBtn.setEnabled(false);
        editProfileBtn.addActionListener(e -> {
            if (selectedStudentForResults != null)
                showEditProfileDialog(selectedStudentForResults);
        });

        JPanel regToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        regToolbar.setBackground(UITheme.BG_PAGE);
        regToolbar.add(editProfileBtn);
        regToolbar.add(enterResultsBtn);

        JPanel regSection = new JPanel(new BorderLayout(0, 4));
        regSection.setBackground(UITheme.BG_PAGE);
        regSection.add(new JScrollPane(regTable), BorderLayout.CENTER);
        regSection.add(regToolbar, BorderLayout.SOUTH);

        // When a student row is selected: populate registration list and enable profile button
        studentTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = studentTable.getSelectedRow();
            regModel.setRowCount(0);
            selectedReg = null;
            selectedStudentForResults = null;
            enterResultsBtn.setEnabled(false);
            editProfileBtn.setEnabled(false);
            if (row < 0) return;
            String sid = (String) studentModel.getValueAt(row, 0);
            selectedStudentForResults = dataStore.loadStudent(sid);
            if (selectedStudentForResults == null) return;
            editProfileBtn.setEnabled(true);
            currentStudentRegs = new ArrayList<>(dataStore.findRegistration(selectedStudentForResults));
            for (Registration r : currentStudentRegs) {
                regModel.addRow(new Object[]{
                    r.getSemester(),
                    r.getSelectedCourses().size() + " course(s)",
                    r.isSubmitted() ? r.getSubmittedAt().toString().substring(0, 16) : "—",
                    r.isSubmitted() ? "Submitted" : "Pending"
                });
            }
        });

        // When a registration row is selected: enable Enter Results button
        regTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = regTable.getSelectedRow();
            if (row < 0 || row >= currentStudentRegs.size()) {
                selectedReg = null;
                enterResultsBtn.setEnabled(false);
            } else {
                selectedReg = currentStudentRegs.get(row);
                enterResultsBtn.setEnabled(true);
            }
        });

        refreshStudentTable();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(studentTable), regSection);
        split.setDividerLocation(260);
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);
        panel.add(UITheme.muted("  Select a student → select a registration → click Enter / Edit Results"),
            BorderLayout.SOUTH);
        return panel;
    }

    private void showEnterResultsDialog(Student s, Registration reg) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Results — " + s.getFullName() + " — Semester " + reg.getSemester(), true);
        dlg.setSize(760, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 8));
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Build editable table: CA and Exam columns are editable; Total and Grade auto-update
        String[] cols = {"Code", "Course Title", "CA /40", "Exam /60", "Total /100", "Grade"};
        boolean[] updating = {false};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 2 || c == 3; }
        };

        // Pre-populate with existing results or zeroes
        List<Result> existing = dataStore.findResults(reg.getRegistrationId());
        java.util.Map<String, Result> existingByCode = new java.util.HashMap<>();
        for (Result r : existing) existingByCode.put(r.getCourse().getCourseCode(), r);

        for (Course c : reg.getSelectedCourses()) {
            Result r = existingByCode.get(c.getCourseCode());
            double ca   = r != null ? r.getTestScore() : 0;
            double exam = r != null ? r.getExamScore()  : 0;
            double tot  = ca + exam;
            model.addRow(new Object[]{
                c.getCourseCode(), c.getCourseTitle(),
                ca, exam, tot, grade(tot)
            });
        }

        // Auto-recompute Total + Grade when CA or Exam changes
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
                model.setValueAt(ca,        row, 2);
                model.setValueAt(exam,       row, 3);
                model.setValueAt(tot,        row, 4);
                model.setValueAt(grade(tot), row, 5);
            } finally { updating[0] = false; }
        });

        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(340);
        table.getColumnModel().getColumn(2).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(4).setPreferredWidth(75);
        table.getColumnModel().getColumn(5).setPreferredWidth(55);
        dlg.add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Live GPA bar ──────────────────────────────────────────────────────
        JLabel gpaLbl = new JLabel(computeDialogGPA(model, reg));
        gpaLbl.setFont(UITheme.FONT_LABEL);
        gpaLbl.setForeground(UITheme.PRIMARY);
        gpaLbl.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        model.addTableModelListener(ev -> gpaLbl.setText(computeDialogGPA(model, reg)));

        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(new Color(0xe8eaf6));
        infoBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        infoBar.add(UITheme.muted("CA = Continuous Assessment (max 40)   ·   Exam = Examination (max 60)"),
            BorderLayout.WEST);
        infoBar.add(gpaLbl, BorderLayout.EAST);
        dlg.add(infoBar, BorderLayout.NORTH);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btns.setOpaque(false);
        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 34));
        JButton save   = UITheme.successButton("Save Results");
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
            JOptionPane.showMessageDialog(dlg, "Results saved successfully.",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        btns.add(cancel);
        btns.add(save);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static String grade(double total) {
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
        double weightedPoints = 0;
        int totalUnits = 0;
        for (int i = 0; i < model.getRowCount() && i < courses.size(); i++) {
            int units = courses.get(i).getUnits();
            if (units <= 0) continue;
            double tot = toDouble(model.getValueAt(i, 2)) + toDouble(model.getValueAt(i, 3));
            double gp  = tot >= 70 ? 5 : tot >= 60 ? 4 : tot >= 50 ? 3 : tot >= 45 ? 2 : tot >= 40 ? 1 : 0;
            weightedPoints += gp * units;
            totalUnits     += units;
        }
        if (totalUnits == 0) return "GPA: —";
        double gpa = weightedPoints / totalUnits;
        return String.format("Semester GPA: %.2f / 5.00", gpa);
    }

    public void refreshStudentTable() {
        if (studentModel == null) return;
        studentModel.setRowCount(0);
        for (Student s : dataStore.getAllStudents()) {
            studentModel.addRow(new Object[]{
                s.getStudentId(), s.getFullName(), s.getProgramme().name(),
                s.getCurrentYear(), s.getCurrentSemester(),
                s.getEmail(), s.getPhone()
            });
        }
    }

    private void showEditProfileDialog(Student s) {
        JDialog dlg = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Edit Student Profile", true);
        dlg.setSize(560, 580);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.setBackground(UITheme.BG_PAGE);

        // ── Scrollable content wrapper ────────────────────────────────────────
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(UITheme.BG_PAGE);
        page.setBorder(BorderFactory.createEmptyBorder(28, 48, 20, 48));

        // ── Page heading ──────────────────────────────────────────────────────
        JLabel heading = new JLabel("Edit Student Profile");
        heading.setFont(UITheme.FONT_TITLE);
        heading.setForeground(UITheme.PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subHeading = UITheme.muted("Student ID and Full Name cannot be changed.");
        subHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        page.add(heading);
        page.add(Box.createVerticalStrut(4));
        page.add(subHeading);
        page.add(Box.createVerticalStrut(20));

        // ── Identity card (read-only) ─────────────────────────────────────────
        JPanel identityCard = new JPanel(new GridBagLayout());
        identityCard.setBackground(new Color(0xF0, 0xF4, 0xFF));
        identityCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xC5, 0xD3, 0xF0), 1),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        identityCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        identityCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        GridBagConstraints il = new GridBagConstraints();
        il.anchor = GridBagConstraints.WEST;
        il.insets = new Insets(4, 0, 4, 20);

        GridBagConstraints iv = new GridBagConstraints();
        iv.fill = GridBagConstraints.HORIZONTAL;
        iv.weightx = 1;
        iv.gridwidth = GridBagConstraints.REMAINDER;
        iv.insets = new Insets(4, 0, 4, 0);

        il.gridy = 0; iv.gridy = 0;
        identityCard.add(UITheme.label("Student ID"), il);
        JLabel idLbl = new JLabel(s.getStudentId());
        idLbl.setFont(UITheme.FONT_MONO);
        idLbl.setForeground(UITheme.PRIMARY);
        identityCard.add(idLbl, iv);

        il.gridy = 1; iv.gridy = 1;
        identityCard.add(UITheme.label("Full Name"), il);
        JLabel nameLbl = new JLabel(s.getFullName());
        nameLbl.setFont(UITheme.FONT_BODY);
        identityCard.add(nameLbl, iv);

        page.add(identityCard);
        page.add(Box.createVerticalStrut(20));

        // ── Editable fields card ──────────────────────────────────────────────
        JPanel editCard = UITheme.card();
        editCard.setLayout(new GridBagLayout());
        editCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(8, 0, 2, 20);

        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 0, 10, 0);

        int row = 0;

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Address"), lc);
        JTextField addressF = UITheme.styledField();
        addressF.setText(s.getAddress());
        editCard.add(addressF, fc);

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Email"), lc);
        JTextField emailF = UITheme.styledField();
        emailF.setText(s.getEmail());
        editCard.add(emailF, fc);

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Phone"), lc);
        JTextField phoneF = UITheme.styledField();
        phoneF.setText(s.getPhone());
        editCard.add(phoneF, fc);

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Programme"), lc);
        JComboBox<Programme> progC = new JComboBox<>(Programme.values());
        progC.setSelectedItem(s.getProgramme());
        progC.setFont(UITheme.FONT_BODY);
        editCard.add(progC, fc);

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Current Year"), lc);
        JSpinner yearS = new JSpinner(new SpinnerNumberModel(s.getCurrentYear(), 1, 4, 1));
        editCard.add(yearS, fc);

        lc.gridy = row; fc.gridy = row++;
        editCard.add(UITheme.label("Current Semester"), lc);
        JSpinner semS = new JSpinner(new SpinnerNumberModel(s.getCurrentSemester(), 1, 2, 1));
        editCard.add(semS, fc);

        page.add(editCard);

        // Anchor page content to top inside the scroll pane
        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setBackground(UITheme.BG_PAGE);
        anchor.add(page, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(anchor,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(scroll, BorderLayout.CENTER);

        // ── Button bar ────────────────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btns.setBackground(new Color(0xF0, 0xF2, 0xF5));
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xDF, 0xE6, 0xE9)));
        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 36));
        JButton save = UITheme.primaryButton("Save Changes");
        save.setPreferredSize(new Dimension(140, 36));

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

    // ── Tab 4 · Report ────────────────────────────────────────────────────────

    private JPanel buildReportTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(0xfafafa));
        area.setText("Click 'Generate Report' to produce the student registration summary.");

        JButton genBtn = UITheme.primaryButton("Generate Report");
        genBtn.setPreferredSize(new Dimension(180, 36));
        genBtn.addActionListener(e -> area.setText(admin.generateStudentReport()));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(UITheme.BG_PAGE);
        toolbar.add(genBtn);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }
}
