import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * SmartEventOrganizerSwing
 * - Single-file Swing app (Admin creates student accounts).
 * - Event-based result publishing and viewing.
 * - 5-second reminder popup after registering for an event.
 *
 * Usage:
 *  javac SmartEventOrganizerSwing.java
 *  java SmartEventOrganizerSwing
 */
public class SmartEventOrganizerSwing {

    // -------------------- Data models --------------------
    static class User {
        String username;
        String password;
        String role; // "Admin", "Faculty", "Student"
        String fullName; // optional for students
        String department; // optional for students

        User(String username, String password, String role) {
            this(username, password, role, "", "");
        }

        User(String username, String password, String role, String fullName, String department) {
            this.username = username;
            this.password = password;
            this.role = role;
            this.fullName = fullName;
            this.department = department;
        }

        @Override
        public String toString() {
            if (role.equals("Student")) {
                return fullName + " (" + username + ") - " + department;
            } else {
                return username + " (" + role + ")";
            }
        }
    }

    static class Event {
        String name;
        String location;
        LocalDate date;
        int totalSeats;
        int bookedSeats = 0;

        Event(String name, String location, LocalDate date, int totalSeats) {
            this.name = name;
            this.location = location;
            this.date = date;
            this.totalSeats = totalSeats;
        }

        @Override
        public String toString() {
            return name + " | " + date + " | " + location + " | Seats: " + bookedSeats + "/" + totalSeats;
        }
    }

    static class Result {
        String eventName;
        String studentName;
        String department;
        String position; // e.g., "1st", "2nd", "Participation"

        Result(String eventName, String studentName, String department, String position) {
            this.eventName = eventName;
            this.studentName = studentName;
            this.department = department;
            this.position = position;
        }
    }

    // -------------------- In-memory storage --------------------
    static ArrayList<User> users = new ArrayList<>();
    static ArrayList<Event> events = new ArrayList<>();
    static ArrayList<Result> results = new ArrayList<>();

    // -------------------- Theme --------------------
    static final Color PRIMARY = new Color(33, 97, 174);
    static final Color HOVER = new Color(52, 152, 219);
    static final Color BG = new Color(245, 247, 250);

    // -------------------- Main --------------------
    public static void main(String[] args) {
        // Seed Admin and sample accounts
        users.add(new User("admin", "admin", "Admin"));
        users.add(new User("faculty", "faculty", "Faculty"));
        // (No default student; admin will create students)

        // Seed sample events
        events.add(new Event("Tech Symposium", "Auditorium", LocalDate.of(2025, 11, 8), 100));
        events.add(new Event("AI Workshop", "Innovation Lab", LocalDate.of(2025, 11, 12), 50));
        events.add(new Event("Cultural Fest", "Main Hall", LocalDate.of(2025, 11, 15), 200));

        SwingUtilities.invokeLater(SmartEventOrganizerSwing::showLoginPage);
    }

    // ====================== LOGIN PAGE ======================
    static void showLoginPage() {
        JFrame frame = new JFrame("Smart Event Organizer - Academia Montes Flora");
        frame.setSize(520, 460);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(PRIMARY);
        JLabel title = new JLabel("Academia Montes Flora", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        top.add(title, BorderLayout.CENTER);
        frame.add(top, BorderLayout.NORTH);

        // Login form
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);

        JLabel heading = new JLabel("Smart Event Organizer");
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));
        heading.setForeground(PRIMARY);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(heading, c);

        c.gridwidth = 1; c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Username:"), c);
        JTextField userField = new JTextField(16);
        c.gridx = 1; panel.add(userField, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("Password:"), c);
        JPasswordField passField = new JPasswordField(16);
        c.gridx = 1; panel.add(passField, c);

        c.gridx = 0; c.gridy = 3; panel.add(new JLabel("Role:"), c);
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Faculty", "Student"});
        c.gridx = 1; panel.add(roleBox, c);

        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        panel.add(loginBtn, c);

        // helper: create student instruction
        JLabel info = new JLabel("<html><i>Note: Admin should create student accounts in Admin Dashboard.</i></html>");
        info.setForeground(Color.DARK_GRAY);
        c.gridy = 5; panel.add(info, c);

        frame.add(panel, BorderLayout.CENTER);

        // Login action
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String role = roleBox.getSelectedItem().toString();

            User found = null;
            for (User u : users) {
                if (u.username.equals(username) && u.password.equals(password) && u.role.equals(role)) {
                    found = u;
                    break;
                }
            }
            if (found != null) {
                frame.dispose();
                switch (role) {
                    case "Admin" -> adminDashboard();
                    case "Faculty" -> facultyDashboard();
                    case "Student" -> studentDashboard(found); // pass the student user
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid credentials or role. If you're a student ask the admin to create an account.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        frame.setVisible(true);
    }

    // ====================== ADMIN DASHBOARD ======================
    static void adminDashboard() {
        JFrame f = baseFrame("Admin Dashboard");
        f.setLayout(new BorderLayout());

        // Top header
        JPanel top = new JPanel(new BorderLayout());
        JLabel head = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        head.setOpaque(true); head.setBackground(PRIMARY); head.setForeground(Color.WHITE);
        head.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.add(head, BorderLayout.CENTER);

        JButton logout = new JButton("Logout"); styleButton(logout);
        logout.addActionListener(e -> { f.dispose(); showLoginPage(); });
        JPanel right = new JPanel(); right.setOpaque(false); right.add(logout);
        top.add(right, BorderLayout.EAST);
        f.add(top, BorderLayout.NORTH);

        // Center area: results text area
        JTextArea area = new JTextArea();
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        f.add(scroll, BorderLayout.CENTER);

        // Bottom actions
        JButton viewUsersBtn = new JButton("View Users"); styleButton(viewUsersBtn);
        JButton viewEventsBtn = new JButton("View Events"); styleButton(viewEventsBtn);
        JButton addStudentBtn = new JButton("Add Student"); styleButton(addStudentBtn);
        JButton addEventBtn = new JButton("Add Event"); styleButton(addEventBtn);
        JButton publishResultsBtn = new JButton("Publish Results (per event)"); styleButton(publishResultsBtn);
        JButton calendarBtn = new JButton("View Calendar"); styleButton(calendarBtn);

        JPanel bottom = new JPanel();
        bottom.add(viewUsersBtn);
        bottom.add(viewEventsBtn);
        bottom.add(addStudentBtn);
        bottom.add(addEventBtn);
        bottom.add(publishResultsBtn);
        bottom.add(calendarBtn);
        f.add(bottom, BorderLayout.SOUTH);

        // Actions
        viewUsersBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("=== USERS ===\n");
            for (User u : users) sb.append(u.toString()).append("\n");
            area.setText(sb.toString());
        });

        viewEventsBtn.addActionListener(e -> area.setText(eventList()));

        addStudentBtn.addActionListener(e -> {
            JTextField username = new JTextField();
            JTextField password = new JTextField();
            JTextField fullname = new JTextField();
            JTextField department = new JTextField();
            Object[] msg = {
                    "Username:", username,
                    "Password:", password,
                    "Full name:", fullname,
                    "Department:", department
            };
            int opt = JOptionPane.showConfirmDialog(f, msg, "Add Student", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                // check duplicate username
                for (User u : users) {
                    if (u.username.equals(username.getText().trim())) {
                        JOptionPane.showMessageDialog(f, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                users.add(new User(username.getText().trim(), password.getText().trim(), "Student", fullname.getText().trim(), department.getText().trim()));
                JOptionPane.showMessageDialog(f, "Student added successfully!");
            }
        });

        addEventBtn.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField location = new JTextField();
            JTextField date = new JTextField(LocalDate.now().toString());
            JTextField seats = new JTextField("50");
            Object[] msg = {
                    "Event name:", name,
                    "Location:", location,
                    "Date (YYYY-MM-DD):", date,
                    "Total seats:", seats
            };
            int opt = JOptionPane.showConfirmDialog(f, msg, "Add Event", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    LocalDate d = LocalDate.parse(date.getText().trim());
                    int s = Integer.parseInt(seats.getText().trim());
                    events.add(new Event(name.getText().trim(), location.getText().trim(), d, s));
                    JOptionPane.showMessageDialog(f, "Event added!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Invalid date or seats", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        publishResultsBtn.addActionListener(e -> {
            if (events.isEmpty()) {
                JOptionPane.showMessageDialog(f, "No events available to publish results for.");
                return;
            }
            // Choose event
            String[] evNames = events.stream().map(ev -> ev.name).toArray(String[]::new);
            JComboBox<String> evBox = new JComboBox<>(evNames);
            JTextField studName = new JTextField();
            JTextField dept = new JTextField();
            JTextField pos = new JTextField();
            Object[] msg = {
                    "Select Event:", evBox,
                    "Student Name:", studName,
                    "Department:", dept,
                    "Position (e.g., 1st, 2nd):", pos
            };
            int opt = JOptionPane.showConfirmDialog(f, msg, "Publish Result", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String eventName = (String) evBox.getSelectedItem();
                results.add(new Result(eventName, studName.getText().trim(), dept.getText().trim(), pos.getText().trim()));
                JOptionPane.showMessageDialog(f, "Result published for event: " + eventName);
            }
        });

        calendarBtn.addActionListener(e -> showCalendar());

        f.setSize(900, 560);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // ====================== FACULTY DASHBOARD ======================
    static void facultyDashboard() {
        JFrame f = baseFrame("Faculty Dashboard");
        f.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JLabel head = new JLabel("Faculty Dashboard", SwingConstants.CENTER);
        head.setOpaque(true); head.setBackground(PRIMARY); head.setForeground(Color.WHITE);
        head.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.add(head, BorderLayout.CENTER);

        JButton logout = new JButton("Logout"); styleButton(logout);
        logout.addActionListener(e -> { f.dispose(); showLoginPage(); });
        JPanel right = new JPanel(); right.setOpaque(false); right.add(logout);
        top.add(right, BorderLayout.EAST);
        f.add(top, BorderLayout.NORTH);

        JTextArea area = new JTextArea(); area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        f.add(scroll, BorderLayout.CENTER);

        JButton addEventBtn = new JButton("Add Event"); styleButton(addEventBtn);
        JButton viewEventsBtn = new JButton("View Events"); styleButton(viewEventsBtn);
        JButton addResultBtn = new JButton("Add/Edit Results"); styleButton(addResultBtn);
        JButton calendarBtn = new JButton("View Calendar"); styleButton(calendarBtn);

        JPanel bottom = new JPanel();
        bottom.add(addEventBtn); bottom.add(viewEventsBtn); bottom.add(addResultBtn); bottom.add(calendarBtn);
        f.add(bottom, BorderLayout.SOUTH);

        addEventBtn.addActionListener(e -> {
            JTextField name = new JTextField();
            JTextField location = new JTextField();
            JTextField date = new JTextField(LocalDate.now().toString());
            JTextField seats = new JTextField("50");
            Object[] msg = {
                    "Event name:", name,
                    "Location:", location,
                    "Date (YYYY-MM-DD):", date,
                    "Total seats:", seats
            };
            int opt = JOptionPane.showConfirmDialog(f, msg, "Add Event", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    LocalDate d = LocalDate.parse(date.getText().trim());
                    int s = Integer.parseInt(seats.getText().trim());
                    events.add(new Event(name.getText().trim(), location.getText().trim(), d, s));
                    JOptionPane.showMessageDialog(f, "Event added!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Invalid date or seats", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        viewEventsBtn.addActionListener(e -> area.setText(eventList()));

        addResultBtn.addActionListener(e -> {
            if (events.isEmpty()) {
                JOptionPane.showMessageDialog(f, "No events available.");
                return;
            }
            String[] evNames = events.stream().map(ev -> ev.name).toArray(String[]::new);
            JComboBox<String> evBox = new JComboBox<>(evNames);
            JTextField studName = new JTextField();
            JTextField dept = new JTextField();
            JTextField pos = new JTextField();
            Object[] msg = {
                    "Select Event:", evBox,
                    "Student Name:", studName,
                    "Department:", dept,
                    "Position (e.g., 1st):", pos
            };
            int opt = JOptionPane.showConfirmDialog(f, msg, "Add/Edit Result", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String eventName = (String) evBox.getSelectedItem();
                results.add(new Result(eventName, studName.getText().trim(), dept.getText().trim(), pos.getText().trim()));
                JOptionPane.showMessageDialog(f, "Result saved for " + eventName);
            }
        });

        calendarBtn.addActionListener(e -> showCalendar());

        f.setSize(900, 560);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // ====================== STUDENT DASHBOARD ======================
    // We pass the logged-in student user to personalize the dashboard
    static void studentDashboard(User student) {
        JFrame f = baseFrame("Student Dashboard - " + student.fullName);
        f.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JLabel head = new JLabel("Student Dashboard - " + student.fullName, SwingConstants.CENTER);
        head.setOpaque(true); head.setBackground(PRIMARY); head.setForeground(Color.WHITE);
        head.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.add(head, BorderLayout.CENTER);

        JButton logout = new JButton("Logout"); styleButton(logout);
        logout.addActionListener(e -> { f.dispose(); showLoginPage(); });
        JPanel right = new JPanel(); right.setOpaque(false); right.add(logout);
        top.add(right, BorderLayout.EAST);
        f.add(top, BorderLayout.NORTH);

        JTextArea area = new JTextArea(); area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        f.add(scroll, BorderLayout.CENTER);

        JButton viewEventsBtn = new JButton("View Events"); styleButton(viewEventsBtn);
        JButton registerBtn = new JButton("Register"); styleButton(registerBtn);
        JButton viewCalendarBtn = new JButton("View Calendar"); styleButton(viewCalendarBtn);
        JButton viewResultsBtn = new JButton("View Results by Event"); styleButton(viewResultsBtn);

        JPanel bottom = new JPanel();
        bottom.add(viewEventsBtn); bottom.add(registerBtn); bottom.add(viewCalendarBtn); bottom.add(viewResultsBtn);
        f.add(bottom, BorderLayout.SOUTH);

        viewEventsBtn.addActionListener(e -> area.setText(eventList()));

        viewCalendarBtn.addActionListener(e -> showCalendar());

        // Register with 5-second reminder popup using Swing Timer (non-blocking)
        registerBtn.addActionListener(e -> {
            if (events.isEmpty()) {
                JOptionPane.showMessageDialog(f, "No events available.");
                return;
            }
            String[] evNames = events.stream().map(ev -> ev.name + " (" + ev.date + ")").toArray(String[]::new);
            JComboBox<String> evBox = new JComboBox<>(evNames);
            Object[] msg = {"Select event to register:", evBox};
            int opt = JOptionPane.showConfirmDialog(f, msg, "Register for Event", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                int idx = evBox.getSelectedIndex();
                final Event ev = events.get(idx);
                if (ev.bookedSeats < ev.totalSeats) {
                    ev.bookedSeats++;
                    JOptionPane.showMessageDialog(f, "Registered for " + ev.name + "!");
                    // Ask for a reminder
                    int rem = JOptionPane.showConfirmDialog(f, "Do you want a reminder for this event in 5 seconds?", "Reminder", JOptionPane.YES_NO_OPTION);
                    if (rem == JOptionPane.YES_OPTION) {
                        // Show popup after 5 seconds
                        Timer t = new Timer(5000, evt -> showReminderPopup(ev, student));
                        t.setRepeats(false);
                        t.start();
                    }
                    area.setText(eventList());
                } else {
                    JOptionPane.showMessageDialog(f, "Sorry, event is full.");
                }
            }
        });

        // Student-view: select event and see all results for that event
        viewResultsBtn.addActionListener(e -> {
            if (events.isEmpty()) {
                JOptionPane.showMessageDialog(f, "No events available.");
                return;
            }
            String[] evNames = events.stream().map(ev -> ev.name).toArray(String[]::new);
            JComboBox<String> evBox = new JComboBox<>(evNames);
            Object[] msg = {"Select event to view results:", evBox};
            int opt = JOptionPane.showConfirmDialog(f, msg, "View Results", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                String eventName = (String) evBox.getSelectedItem();
                StringBuilder sb = new StringBuilder();
                sb.append("Results for: ").append(eventName).append("\n\n");
                int count = 0;
                for (Result r : results) {
                    if (r.eventName.equals(eventName)) {
                        count++;
                        sb.append(count).append(". ").append(r.studentName)
                          .append(" | ").append(r.department)
                          .append(" | ").append(r.position).append("\n");
                    }
                }
                if (count == 0) sb.append("No results published for this event yet.");
                else sb.append("\n🎉 Congratulations to all winners!");
                area.setText(sb.toString());
            }
        });

        f.setSize(900, 560);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // -------------------- Reminder Popup (after registration) --------------------
    // Simple animated popup with student's name and event details
    static void showReminderPopup(Event ev, User student) {
        JDialog dlg = new JDialog((Frame) null, "Reminder", false);
        dlg.setUndecorated(true);
        dlg.setSize(380, 160);

        // center on screen
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - dlg.getWidth()) / 2;
        int y = (screen.height - dlg.getHeight()) / 2;
        dlg.setLocation(x, y + 30);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 80), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("🔔 Reminder", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(250, 220, 120));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line1 = new JLabel("Hi " + (student.fullName.isEmpty() ? student.username : student.fullName) + ", you registered for:");
        line1.setForeground(Color.WHITE);
        line1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line2 = new JLabel(ev.name + "  •  " + ev.date + "  •  " + ev.location, SwingConstants.CENTER);
        line2.setForeground(Color.LIGHT_GRAY);
        line2.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(line1);
        panel.add(Box.createVerticalStrut(4));
        panel.add(line2);

        dlg.add(panel);
        try { dlg.setOpacity(0f); } catch (Exception ignored) {}
        dlg.setVisible(true);

        // fade in using Swing Timer
        final int steps = 20;
        final int interval = 20;
        final int[] s = {0};
        Timer fadeIn = new Timer(interval, null);
        fadeIn.addActionListener(ae -> {
            s[0]++;
            float alpha = Math.min(1f, s[0] / (float) steps);
            try { dlg.setOpacity(alpha); } catch (Exception ignored) {}
            int offset = (int) ((1 - alpha) * 30);
            dlg.setLocation(x, y + offset);
            if (s[0] >= steps) fadeIn.stop();
        });
        fadeIn.start();

        // auto fade out after 6 seconds
        Timer wait = new Timer(6000, null);
        wait.addActionListener(ae -> {
            wait.stop();
            final int[] tstep = {steps};
            Timer fadeOut = new Timer(interval, null);
            fadeOut.addActionListener(fae -> {
                tstep[0]--;
                float alpha = Math.max(0f, tstep[0] / (float) steps);
                try { dlg.setOpacity(alpha); } catch (Exception ignored) {}
                int offset = (int) ((1 - alpha) * 30);
                dlg.setLocation(x, y + offset);
                if (tstep[0] <= 0) {
                    fadeOut.stop();
                    dlg.dispose();
                }
            });
            fadeOut.start();
        });
        wait.setRepeats(false);
        wait.start();
    }

    // ====================== Calendar (simple with highlighted event days) ======================
    static void showCalendar() {
        JFrame f = new JFrame("Event Calendar");
        f.setSize(700, 520);
        f.setLocationRelativeTo(null);
        f.setLayout(new BorderLayout());

        JLabel head = new JLabel("Calendar View", SwingConstants.CENTER);
        head.setOpaque(true); head.setBackground(PRIMARY); head.setForeground(Color.WHITE);
        head.setFont(new Font("SansSerif", Font.BOLD, 18));
        f.add(head, BorderLayout.NORTH);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG);
        JPanel daysWrapper = new JPanel(new BorderLayout());
        daysWrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        daysWrapper.setBackground(BG);

        JPanel nav = new JPanel();
        nav.setBackground(BG);
        JButton prev = new JButton("<<"); styleButton(prev);
        JButton next = new JButton(">>"); styleButton(next);
        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        nav.add(prev); nav.add(lbl); nav.add(next);

        JPanel daysPanel = new JPanel(new GridLayout(0, 7, 6, 6));
        daysPanel.setBackground(BG);

        daysWrapper.add(daysPanel, BorderLayout.CENTER);
        container.add(daysWrapper, BorderLayout.CENTER);
        f.add(container, BorderLayout.CENTER);
        f.add(nav, BorderLayout.SOUTH);

        final LocalDate[] month = {LocalDate.now().withDayOfMonth(1)};

        Runnable render = () -> {
            daysPanel.removeAll();
            lbl.setText(month[0].getMonth() + " " + month[0].getYear());

            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String dn : dayNames) {
                JLabel lab = new JLabel(dn, SwingConstants.CENTER);
                lab.setFont(new Font("SansSerif", Font.BOLD, 13));
                lab.setForeground(PRIMARY);
                daysPanel.add(lab);
            }

            int start = month[0].getDayOfWeek().getValue() % 7;
            for (int i = 0; i < start; i++) daysPanel.add(new JLabel(""));

            int count = month[0].lengthOfMonth();
            for (int i = 1; i <= count; i++) {
                LocalDate date = month[0].withDayOfMonth(i);
                JButton btn = new JButton(String.valueOf(i));
                btn.setFocusPainted(false);
                btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                btn.setPreferredSize(new Dimension(46, 46));
                btn.setFont(new Font("SansSerif", Font.PLAIN, 13));

                int eventsOnDay = 0;
                for (Event ev : events) if (ev.date.equals(date)) eventsOnDay++;

                if (eventsOnDay > 0) {
                    btn.setBackground(new Color(255, 236, 179)); // gold-ish
                    btn.setForeground(Color.BLACK);
                    if (eventsOnDay == 1) btn.setText(i + " •");
                    else btn.setText(i + " (" + eventsOnDay + ")");
                }

                LocalDate finalDate = date;
                btn.addActionListener(ae -> showEventsForDate(finalDate));
                daysPanel.add(btn);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        };

        prev.addActionListener(e -> {
            month[0] = month[0].minusMonths(1);
            render.run();
        });
        next.addActionListener(e -> {
            month[0] = month[0].plusMonths(1);
            render.run();
        });

        render.run();
        f.setVisible(true);
    }

    // Show events details for a specific date
    static void showEventsForDate(LocalDate date) {
        StringBuilder sb = new StringBuilder("Events on " + date + ":\n\n");
        boolean found = false;
        for (Event ev : events) {
            if (ev.date.equals(date)) {
                sb.append(ev.name).append(" @ ").append(ev.location)
                  .append("\nSeats: ").append(ev.bookedSeats).append("/").append(ev.totalSeats).append("\n\n");
                found = true;
            }
        }
        if (!found) sb.append("No events scheduled on this date.");
        JOptionPane.showMessageDialog(null, sb.toString(), "Events", JOptionPane.INFORMATION_MESSAGE);
    }

    // ====================== Helpers ======================
    static String eventList() {
        StringBuilder sb = new StringBuilder("=== EVENTS ===\n\n");
        for (int i = 0; i < events.size(); i++) {
            sb.append((i + 1)).append(". ").append(events.get(i).toString()).append("\n");
        }
        return sb.toString();
    }

    static JFrame baseFrame(String title) {
        JFrame f = new JFrame(title);
        f.setSize(800, 520);
        f.setLayout(new BorderLayout());
        f.getContentPane().setBackground(BG);
        f.setLocationRelativeTo(null);
        return f;
    }

    static void styleButton(JButton b) {
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(HOVER); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(PRIMARY); }
        });
    }
}
