package com.nimet.rcrs.gui;

import com.nimet.rcrs.exception.RegistrationException;
import com.nimet.rcrs.model.*;
import com.nimet.rcrs.repository.DataStore;
import com.nimet.rcrs.service.AuthenticationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StudentPanel extends JPanel {

    private final Student student;
    private final DataStore dataStore;
    private final AuthenticationService authService;
    private final Runnable onLogout;

    // Register-courses tab state
    private DefaultTableModel selectedModel;
    private Registration activeReg;

    // My-registrations tab
    private DefaultTableModel myRegModel;
    private DefaultTableModel resultsModel;
    private JLabel sgpaLabel, cgpaLabel, honoursLabel;

    public StudentPanel(Student student, DataStore dataStore,
                        AuthenticationService authService, Runnable onLogout) {
        this.student     = student;
        this.dataStore   = dataStore;
        this.authService = authService;
        this.onLogout    = onLogout;
        buildUI();
    }

    // ── Shell ─────────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PAGE);
        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_LABEL);
        tabs.addTab("  My Profile  ",        buildProfileTab());
        tabs.addTab("  Available Courses  ", buildAvailableCoursesTab());
        tabs.addTab("  Register Courses  ",  buildRegisterCoursesTab());
        tabs.addTab("  My Registrations  ",  buildMyRegistrationsTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.SECONDARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("NiMet RCRS  ·  " + student.getFullName());
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(
            student.getStudentId() + "  ·  "
            + student.getProgramme().name() + "  ·  Year "
            + student.getCurrentYear() + "  ·  Semester " + student.getCurrentSemester());
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(200, 230, 255));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UITheme.SECONDARY);
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

    // ── Tab 1 · Profile ────────────────────────────────────────────────────────

    private JScrollPane buildProfileTab() {
        JPanel card = UITheme.card();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(500, 420));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 8, 6, 20);

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor = GridBagConstraints.WEST;
        vc.fill = GridBagConstraints.HORIZONTAL;
        vc.weightx = 1;
        vc.gridwidth = GridBagConstraints.REMAINDER;
        vc.insets = new Insets(6, 0, 6, 8);

        Object[][] rows = {
            {"Student ID",       student.getStudentId()},
            {"Full Name",        student.getFullName()},
            {"Programme",        student.getProgramme().name() + "  —  " + student.getProgramme().getFullName()},
            {"Year / Semester",  "Year " + student.getCurrentYear() + "  ·  Semester " + student.getCurrentSemester()},
            {"Year Admitted",    String.valueOf(student.getYearOfAdmission())},
            {"Address",          blank(student.getAddress())},
            {"Email",            blank(student.getEmail())},
            {"Phone",            blank(student.getPhone())},
            {"Password Status",  student.isTemporaryPassword() ? "Temporary — please change" : "Permanent"},
        };

        for (int i = 0; i < rows.length; i++) {
            lc.gridy = i; vc.gridy = i;
            card.add(UITheme.label((String) rows[i][0]), lc);
            JLabel val = new JLabel((String) rows[i][1]);
            val.setFont(UITheme.FONT_BODY);
            val.setForeground(i == rows.length - 1 && student.isTemporaryPassword()
                ? UITheme.ERROR : UITheme.TEXT);
            card.add(val, vc);
        }

        // Change-password button
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = rows.length;
        bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.anchor = GridBagConstraints.EAST;
        bc.insets = new Insets(16, 0, 0, 8);
        JButton changePwdBtn = UITheme.primaryButton("Change Password");
        changePwdBtn.setPreferredSize(new Dimension(180, 38));
        changePwdBtn.addActionListener(e -> showChangePasswordDialog());
        card.add(changePwdBtn, bc);

        JPanel anchor = new JPanel(new BorderLayout());
        anchor.setBackground(UITheme.BG_PAGE);
        anchor.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
        anchor.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(anchor,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        fc.weightx = 1;
        fc.gridwidth = GridBagConstraints.REMAINDER;
        fc.insets = new Insets(0, 16, 8, 16);

        JPasswordField currentF = UITheme.styledPasswordField();
        JPasswordField newF     = UITheme.styledPasswordField();
        JPasswordField confirmF = UITheme.styledPasswordField();
        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(UITheme.ERROR);
        errorLbl.setFont(UITheme.FONT_SMALL);

        int row = 0;
        lc.gridy = row; fc.gridy = row++; dlg.add(UITheme.label("Current Password"), lc); dlg.add(currentF, fc);
        lc.gridy = row; fc.gridy = row++; dlg.add(UITheme.label("New Password (min 6 chars)"), lc); dlg.add(newF, fc);
        lc.gridy = row; fc.gridy = row++; dlg.add(UITheme.label("Confirm New Password"), lc); dlg.add(confirmF, fc);

        GridBagConstraints ec = new GridBagConstraints();
        ec.gridy = row++;
        ec.gridwidth = GridBagConstraints.REMAINDER;
        ec.insets = new Insets(0, 16, 0, 16);
        dlg.add(errorLbl, ec);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setOpaque(false);
        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(90, 34));
        JButton save   = UITheme.primaryButton("Save");
        save.setPreferredSize(new Dimension(90, 34));

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String cur  = new String(currentF.getPassword()).trim();
            String np   = new String(newF.getPassword()).trim();
            String conf = new String(confirmF.getPassword()).trim();
            if (np.length() < 6)   { errorLbl.setText("Password must be at least 6 characters."); return; }
            if (!np.equals(conf))  { errorLbl.setText("Passwords do not match."); return; }
            if (!authService.changePassword(student, cur, np)) {
                errorLbl.setText("Current password is incorrect.");
                return;
            }
            JOptionPane.showMessageDialog(dlg, "Password changed successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        btns.add(cancel);
        btns.add(save);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridy = row;
        bc.gridwidth = GridBagConstraints.REMAINDER;
        bc.fill = GridBagConstraints.HORIZONTAL;
        dlg.add(btns, bc);
        dlg.setVisible(true);
    }

    // ── Tab 2 · Available Courses ─────────────────────────────────────────────

    private JPanel buildAvailableCoursesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Code", "Title", "Units"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UITheme.FONT_LABEL);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);

        for (Course c : student.viewAvailableCourses())
            model.addRow(new Object[]{c.getCourseCode(), c.getCourseTitle(), c.getUnits()});

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(UITheme.muted("  Available for Year " + student.getCurrentYear()
            + " · Semester " + student.getCurrentSemester()
            + " · " + model.getRowCount() + " course(s)"), BorderLayout.SOUTH);
        return panel;
    }

    // ── Tab 3 · Register Courses ──────────────────────────────────────────────

    private JPanel buildRegisterCoursesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        // Header label
        JLabel info = UITheme.label(
            "Select courses for Semester "
            + (student.getYearOfAdmission() + student.getCurrentYear() - 1)
            + "/" + student.getCurrentSemester()
            + "   (min " + Registration.MIN_COURSES + "  ·  max " + Registration.MAX_COURSES + ")");
        panel.add(info, BorderLayout.NORTH);

        // ── Available list (left) / Selected list (right) ──────────────────
        List<Course> available = student.viewAvailableCourses();

        String[] cols = {"Code", "Title", "Units"};

        DefaultTableModel availModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable availTable = new JTable(availModel);
        styleTable(availTable);
        for (Course c : available)
            availModel.addRow(new Object[]{c.getCourseCode(), c.getCourseTitle(), c.getUnits()});

        selectedModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable selTable = new JTable(selectedModel);
        styleTable(selTable);

        JPanel availPanel = namedPanel("Available Courses", new JScrollPane(availTable));
        JPanel selPanel   = namedPanel("Selected Courses",  new JScrollPane(selTable));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, availPanel, selPanel);
        split.setDividerLocation(0.5);
        split.setResizeWeight(0.5);
        panel.add(split, BorderLayout.CENTER);

        // ── Buttons ────────────────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        btns.setBackground(UITheme.BG_PAGE);

        JButton addBtn = UITheme.primaryButton("Add →");
        addBtn.setPreferredSize(new Dimension(110, 34));
        addBtn.addActionListener(e -> {
            int row = availTable.getSelectedRow();
            if (row < 0) return;
            if (activeReg == null) activeReg = student.submitRegistration();
            String code = (String) availModel.getValueAt(row, 0);
            Course course = available.stream()
                .filter(c -> c.getCourseCode().equals(code)).findFirst().orElse(null);
            if (course == null) return;
            if (activeReg.getCourse(course)) {
                JOptionPane.showMessageDialog(this, "'" + code + "' is already selected.",
                    "Duplicate", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            activeReg.addCourse(course);
            selectedModel.addRow(new Object[]{course.getCourseCode(), course.getCourseTitle(), course.getUnits()});
        });

        JButton removeBtn = UITheme.dangerButton("← Remove");
        removeBtn.setPreferredSize(new Dimension(110, 34));
        removeBtn.addActionListener(e -> {
            if (activeReg == null) return;
            int row = selTable.getSelectedRow();
            if (row < 0) return;
            String code = (String) selectedModel.getValueAt(row, 0);
            available.stream().filter(c -> c.getCourseCode().equals(code))
                .findFirst().ifPresent(activeReg::removeCourse);
            selectedModel.removeRow(row);
        });

        JButton submitBtn = UITheme.successButton("Submit Registration");
        submitBtn.setPreferredSize(new Dimension(190, 34));
        submitBtn.addActionListener(e -> {
            if (activeReg == null || activeReg.getSelectedCourses().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least " + Registration.MIN_COURSES + " courses first.",
                    "No Courses Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                activeReg.submit();
                dataStore.saveRegistration(activeReg);
                JOptionPane.showMessageDialog(this,
                    "Registration submitted successfully!\n"
                    + "Semester: " + activeReg.getSemester() + "\n"
                    + "Courses:  " + activeReg.getSelectedCourses().size(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                selectedModel.setRowCount(0);
                activeReg = null;
                refreshMyRegistrations();
            } catch (RegistrationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btns.add(addBtn);
        btns.add(removeBtn);
        btns.add(Box.createHorizontalStrut(16));
        btns.add(submitBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ── Tab 4 · My Registrations & Results ───────────────────────────────────

    private JPanel buildMyRegistrationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UITheme.BG_PAGE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Top: registrations list ───────────────────────────────────────────
        String[] rCols = {"Semester", "Courses", "Submitted At", "Status"};
        myRegModel = new DefaultTableModel(rCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable regTable = new JTable(myRegModel);
        styleTable(regTable);
        regTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        regTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        regTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        regTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        regTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        refreshMyRegistrations();

        // ── Bottom: results for selected registration ─────────────────────────
        String[] resCols = {"Code", "Course Title", "CA /40", "Exam /60", "Total /100", "Grade"};
        resultsModel = new DefaultTableModel(resCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultsTable = new JTable(resultsModel);
        styleTable(resultsTable);
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(340);
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(55);

        // ── GPA summary bar ───────────────────────────────────────────────────
        sgpaLabel    = gpaStatLabel("—");
        cgpaLabel    = gpaStatLabel("—");
        honoursLabel = gpaStatLabel("—");

        JPanel gpaBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        gpaBar.setBackground(new Color(0xe8eaf6));
        gpaBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        gpaBar.add(gpaGroup("Semester GPA", sgpaLabel));
        gpaBar.add(separator());
        gpaBar.add(gpaGroup("Cumulative GPA", cgpaLabel));
        gpaBar.add(separator());
        gpaBar.add(gpaGroup("Class of Degree", honoursLabel));

        JPanel resultsPanel = new JPanel(new BorderLayout(0, 4));
        resultsPanel.setBackground(UITheme.BG_PAGE);
        resultsPanel.add(UITheme.label("Course Results"), BorderLayout.NORTH);
        resultsPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        resultsPanel.add(gpaBar, BorderLayout.SOUTH);

        // When a registration row is selected, load results and refresh GPA
        regTable.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting()) return;
            int row = regTable.getSelectedRow();
            resultsModel.setRowCount(0);
            sgpaLabel.setText("—");
            cgpaLabel.setText("—");
            honoursLabel.setText("—");
            if (row < 0) return;
            List<Registration> regs = dataStore.findRegistration(student);
            if (row >= regs.size()) return;
            String regId = regs.get(row).getRegistrationId();
            List<Result> semResults = dataStore.findResults(regId);
            for (Result res : semResults) {
                resultsModel.addRow(new Object[]{
                    res.getCourse().getCourseCode(),
                    res.getCourse().getCourseTitle(),
                    String.format("%.1f", res.getTestScore()),
                    String.format("%.1f", res.getExamScore()),
                    String.format("%.1f", res.getTotal()),
                    res.getGrade()
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
        panel.add(UITheme.muted("  Select a semester above to view your course results"),
            BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMyRegistrations() {
        if (myRegModel == null) return;
        myRegModel.setRowCount(0);
        for (Registration r : dataStore.findRegistration(student)) {
            myRegModel.addRow(new Object[]{
                r.getSemester(),
                r.getSelectedCourses().size() + " course(s)",
                r.isSubmitted()
                    ? r.getSubmittedAt().toString().substring(0, 16)
                    : "—",
                r.isSubmitted() ? "Submitted" : "Pending"
            });
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String blank(String s) {
        return (s == null || s.isEmpty()) ? "—" : s;
    }

    private static void styleTable(JTable t) {
        t.setFont(UITheme.FONT_BODY);
        t.setRowHeight(24);
        t.getTableHeader().setFont(UITheme.FONT_LABEL);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private static JPanel namedPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(UITheme.BG_PAGE);
        JLabel lbl = UITheme.label(title);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        p.add(lbl, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // ── GPA helpers ───────────────────────────────────────────────────────────

    private double computeGPA(List<Result> results) {
        double weightedPoints = 0;
        int totalUnits = 0;
        for (Result r : results) {
            int units = r.getCourse().getUnits();
            if (units > 0) {
                weightedPoints += r.getGradePoint() * units;
                totalUnits     += units;
            }
        }
        return totalUnits > 0 ? weightedPoints / totalUnits : 0;
    }

    private double computeCGPA() {
        double weightedPoints = 0;
        int totalUnits = 0;
        for (Registration reg : dataStore.findRegistration(student)) {
            for (Result r : dataStore.findResults(reg.getRegistrationId())) {
                int units = r.getCourse().getUnits();
                if (units > 0) {
                    weightedPoints += r.getGradePoint() * units;
                    totalUnits     += units;
                }
            }
        }
        return totalUnits > 0 ? weightedPoints / totalUnits : 0;
    }

    private static String honours(double cgpa) {
        if (cgpa >= 4.50) return "First Class";
        if (cgpa >= 3.50) return "Second Class Upper";
        if (cgpa >= 2.40) return "Second Class Lower";
        if (cgpa >= 1.50) return "Third Class";
        if (cgpa >= 1.00) return "Pass";
        return "Fail";
    }

    private static JLabel gpaStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setForeground(UITheme.PRIMARY);
        return l;
    }

    private static JPanel gpaGroup(String caption, JLabel valueLabel) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(0xe8eaf6));
        JLabel cap = UITheme.muted(caption);
        cap.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cap);
        p.add(valueLabel);
        return p;
    }

    private static JSeparator separator() {
        JSeparator s = new JSeparator(JSeparator.VERTICAL);
        s.setPreferredSize(new Dimension(1, 36));
        s.setForeground(UITheme.BORDER);
        return s;
    }
}
