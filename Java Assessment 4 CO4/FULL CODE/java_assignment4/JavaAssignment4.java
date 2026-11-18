import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.List;

public class JavaAssignment4 extends JFrame {
    public JavaAssignment4() {
        super("Java Assignment 4 - CO4");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setLocationRelativeTo(null);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Fees", new FeePanel());
        tabs.addTab("2. Hospital", new HospitalPanel());
        tabs.addTab("3. Library", new LibraryPanel());
        tabs.addTab("4. Draw Pad", new DrawPanel());
        tabs.addTab("5. Converter", new ConverterPanel());
        tabs.addTab("6. Events + JDBC", new EventPanel());
        add(tabs);
    }

    static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // TASK 1: UI and business logic are separate; simulated payment has no bank/database integration.
    static class FeeService {
        String pay(String reg, String rawAmount) {
            if (reg == null || reg.trim().isEmpty()) throw new IllegalArgumentException("Register number is required");
            double amount;
            try { amount = Double.parseDouble(rawAmount.trim()); }
            catch (Exception ex) { throw new IllegalArgumentException("Enter a valid amount"); }
            if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Amount must be positive");
            return String.format("DEMO receipt: %s paid INR %.2f (not a real transaction)", reg.trim(), amount);
        }
    }
    static class FeePanel extends JPanel {
        final JTextField reg = new JTextField(15), amount = new JTextField(12);
        final JLabel status = new JLabel("Enter details to simulate a fee payment");
        String lastReceipt;
        FeePanel() {
            setLayout(new BorderLayout(10, 10));
            JPanel fields = new JPanel(new FlowLayout());
            fields.add(new JLabel("Register No:")); fields.add(reg);
            fields.add(new JLabel("Amount (INR):")); fields.add(amount);
            JButton pay = new JButton("Pay"), clear = new JButton("Clear"), receipt = new JButton("Receipt");
            fields.add(pay); fields.add(clear); fields.add(receipt);
            add(fields, BorderLayout.NORTH); add(status, BorderLayout.CENTER);
            pay.addActionListener(e -> {
                try { lastReceipt = new FeeService().pay(reg.getText(), amount.getText()); status.setText(lastReceipt); }
                catch (IllegalArgumentException ex) { error(this, ex.getMessage()); }
            });
            clear.addActionListener(e -> { reg.setText(""); amount.setText(""); status.setText("Cleared"); });
            receipt.addActionListener(e -> JOptionPane.showMessageDialog(this,
                lastReceipt == null ? "No payment receipt yet" : lastReceipt));
        }
    }

    // TASK 2: Dependent selection, validation and window-close confirmation.
    static class HospitalPanel extends JPanel {
        final Map<String, List<String>> doctors = new LinkedHashMap<>();
        final JTextField patient = new JTextField(14);
        final JComboBox<String> dept, doctor = new JComboBox<>(), slot = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00"});
        final JLabel result = new JLabel("No appointment booked");
        boolean bookingInProgress;
        HospitalPanel() {
            doctors.put("Cardiology", Arrays.asList("Dr Rao", "Dr Meera"));
            doctors.put("Neurology", Arrays.asList("Dr Kumar", "Dr Sana"));
            doctors.put("Orthopedics", Arrays.asList("Dr John", "Dr Priya"));
            dept = new JComboBox<>(doctors.keySet().toArray(new String[0]));
            setLayout(new BorderLayout());
            JPanel form = new JPanel(new FlowLayout());
            form.add(new JLabel("Patient ID:")); form.add(patient);
            form.add(new JLabel("Department:")); form.add(dept);
            form.add(new JLabel("Doctor:")); form.add(doctor);
            form.add(new JLabel("Slot:")); form.add(slot);
            JButton book = new JButton("Book Appointment"); form.add(book);
            add(form, BorderLayout.NORTH); add(result, BorderLayout.CENTER);
            dept.addActionListener(e -> refreshDoctors());
            patient.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { bookingInProgress = true; }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { bookingInProgress = true; }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { bookingInProgress = true; }
            });
            book.addActionListener(e -> {
                if (patient.getText().trim().isEmpty()) { error(this, "Patient ID is required"); return; }
                String confirmation = "Booked: " + patient.getText().trim() + " | " + dept.getSelectedItem()
                    + " | " + doctor.getSelectedItem() + " | " + slot.getSelectedItem();
                result.setText(confirmation); bookingInProgress = false;
                JOptionPane.showMessageDialog(this, confirmation);
            });
            refreshDoctors();
        }
        void refreshDoctors() {
            doctor.removeAllItems();
            for (String name : doctors.get((String) dept.getSelectedItem())) doctor.addItem(name);
        }
        boolean confirmClose() {
            return !bookingInProgress || JOptionPane.showConfirmDialog(this,
                "An appointment form is in progress. Close anyway?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
    }

    // TASK 3: BorderLayout expands center while FlowLayout keeps controls at preferred sizes.
    static class LibraryPanel extends JPanel {
        final JTextField query = new JTextField(22);
        final JComboBox<String> category = new JComboBox<>(new String[]{"All", "Programming", "Science", "Literature"});
        final JTextArea results = new JTextArea();
        final JLabel status = new JLabel("Ready");
        int page = 0;
        final String[] books = {"Java Programming", "Effective Java", "Data Structures", "Physics Basics",
            "Computer Networks", "Modern Literature", "Algorithms", "Database Systems"};
        LibraryPanel() {
            setLayout(new BorderLayout(8, 8));
            JLabel title = new JLabel("Campus Library Search", SwingConstants.CENTER);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
            JPanel top = new JPanel(new BorderLayout()); top.add(title, BorderLayout.NORTH);
            JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton find = new JButton("Search");
            search.add(new JLabel("Title:")); search.add(query); search.add(category); search.add(find);
            top.add(search, BorderLayout.SOUTH); add(top, BorderLayout.NORTH);
            results.setEditable(false); results.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
            add(new JScrollPane(results), BorderLayout.CENTER);
            JPanel bottom = new JPanel(new BorderLayout()); JPanel actions = new JPanel(new FlowLayout());
            JButton prev = new JButton("Prev"), next = new JButton("Next"), reserve = new JButton("Reserve");
            actions.add(prev); actions.add(next); actions.add(reserve);
            bottom.add(actions, BorderLayout.NORTH); bottom.add(status, BorderLayout.SOUTH); add(bottom, BorderLayout.SOUTH);
            find.addActionListener(e -> { page = 0; showBooks(); });
            query.addActionListener(e -> { page = 0; showBooks(); });
            prev.addActionListener(e -> { if (page > 0) page--; showBooks(); });
            next.addActionListener(e -> { if ((page + 1) * 3 < matching().size()) page++; showBooks(); });
            reserve.addActionListener(e -> JOptionPane.showMessageDialog(this, "Demo only: reservation not stored"));
            showBooks();
        }
        List<String> matching() {
            List<String> matches = new ArrayList<>();
            for (String book : books) if (book.toLowerCase().contains(query.getText().trim().toLowerCase())) matches.add(book);
            return matches;
        }
        void showBooks() {
            List<String> matches = matching(); StringBuilder sb = new StringBuilder();
            for (int i = page * 3; i < Math.min(matches.size(), (page + 1) * 3); i++) sb.append(i + 1).append(". ").append(matches.get(i)).append("\n");
            results.setText(sb.length() == 0 ? "No matching books" : sb.toString());
            status.setText("Results: " + matches.size() + " | Page " + (page + 1));
        }
    }

    // TASK 4: Correct mouseClicked spelling; points persist through paintComponent.
    static class DrawPanel extends JPanel {
        final List<Point> points = new ArrayList<>();
        final JLabel status = new JLabel("Clicks: 0");
        DrawPanel() {
            setLayout(new BorderLayout());
            JPanel canvas = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.BLUE);
                    for (Point p : points) g.fillOval(p.x - 3, p.y - 3, 6, 6);
                }
            };
            canvas.setBackground(Color.WHITE);
            canvas.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    points.add(e.getPoint()); status.setText("Clicks: " + points.size()); canvas.repaint();
                }
            });
            JButton clear = new JButton("Clear Dots");
            clear.addActionListener(e -> { points.clear(); status.setText("Clicks: 0"); canvas.repaint(); });
            JPanel bottom = new JPanel(new FlowLayout()); bottom.add(status); bottom.add(clear);
            add(canvas, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        }
    }

    // TASK 5: Switch between four listener registration styles at runtime.
    static class ConverterPanel extends JPanel implements ActionListener {
        final JTextField amount = new JTextField(10);
        final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
        final JComboBox<String> style = new JComboBox<>(new String[]{"Frame/Panel implements ActionListener", "Named inner class", "Anonymous inner class", "Lambda"});
        final JLabel result = new JLabel("Enter an INR amount");
        final JButton convert = new JButton("Convert");
        ActionListener active;
        ConverterPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 12, 16));
            add(new JLabel("INR:")); add(amount); add(target); add(convert);
            add(new JLabel("Listener style:")); add(style); add(result);
            style.addActionListener(e -> changeStyle()); changeStyle();
        }
        class NamedHandler implements ActionListener {
            @Override public void actionPerformed(ActionEvent e) { convert(); }
        }
        @Override public void actionPerformed(ActionEvent e) { convert(); }
        void changeStyle() {
            if (active != null) { convert.removeActionListener(active); amount.removeActionListener(active); }
            switch (style.getSelectedIndex()) {
                case 0: active = this; break;
                case 1: active = new NamedHandler(); break;
                case 2: active = new ActionListener() { @Override public void actionPerformed(ActionEvent e) { convert(); } }; break;
                default: active = e -> convert();
            }
            convert.addActionListener(active); amount.addActionListener(active);
        }
        void convert() {
            try {
                double n = Double.parseDouble(amount.getText().trim());
                if (!Double.isFinite(n) || n < 0) throw new NumberFormatException();
                String currency = (String) target.getSelectedItem();
                double rate = "USD".equals(currency) ? 0.012 : 0.011; // illustrative fixed rates
                String message = String.format("%.2f INR = %.2f %s (demo rate)", n, n * rate, currency);
                result.setText(message); JOptionPane.showMessageDialog(this, message);
            } catch (NumberFormatException ex) { result.setText("Invalid amount"); error(this, "Invalid amount"); }
        }
    }

    // TASK 6: SwingWorker + layered service/repository; MySQL URL from environment variables.
    static class Registration {
        final String student, event;
        Registration(String student, String event) { this.student = student; this.event = event; }
    }
    static class RegistrationRepository {
        Connection connect() throws SQLException {
            String url = System.getenv().getOrDefault("JAVA_ASSIGNMENT4_DB_URL", "jdbc:mysql://localhost:3306/campus_events");
            String user = System.getenv().getOrDefault("JAVA_ASSIGNMENT4_DB_USER", "root");
            String password = System.getenv().getOrDefault("JAVA_ASSIGNMENT4_DB_PASSWORD", "");
            return DriverManager.getConnection(url, user, password);
        }
        void insert(Registration r) throws SQLException {
            String sql = "INSERT INTO registrations(student_id, event_name) VALUES (?, ?)";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, r.student); ps.setString(2, r.event); ps.executeUpdate();
            }
        }
        List<Registration> page(String event, int limit, int offset) throws SQLException {
            List<Registration> rows = new ArrayList<>();
            String sql = "SELECT student_id, event_name FROM registrations WHERE (? = 'All' OR event_name = ?) ORDER BY id DESC LIMIT ? OFFSET ?";
            try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, event); ps.setString(2, event); ps.setInt(3, limit); ps.setInt(4, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) rows.add(new Registration(rs.getString(1), rs.getString(2)));
                }
            }
            return rows;
        }
    }
    static class RegistrationService {
        final RegistrationRepository repository = new RegistrationRepository();
        void register(String student, String event) throws SQLException {
            if (student == null || !student.trim().matches("[A-Za-z0-9_-]{3,30}"))
                throw new IllegalArgumentException("Student ID must be 3-30 letters, digits, _ or -");
            repository.insert(new Registration(student.trim(), event));
        }
    }
    static class EventPanel extends JPanel {
        final JTextField student = new JTextField(15);
        final JComboBox<String> event = new JComboBox<>(new String[]{"Coding", "Dance", "Music", "Quiz"});
        final JComboBox<String> filter = new JComboBox<>(new String[]{"All", "Coding", "Dance", "Music", "Quiz"});
        final DefaultTableModel model = new DefaultTableModel(new Object[]{"Student ID", "Event"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        final JTable table = new JTable(model);
        final JLabel status = new JLabel("Configure MySQL, then click Refresh");
        final JButton register = new JButton("Register"), refresh = new JButton("Refresh"), prev = new JButton("Prev"), next = new JButton("Next");
        final RegistrationService service = new RegistrationService();
        int page = 0, requestId = 0;
        static final int PAGE_SIZE = 20;
        EventPanel() {
            setLayout(new BorderLayout(8, 8));
            JMenuBar menu = new JMenuBar(); JMenu file = new JMenu("File");
            JMenuItem export = new JMenuItem("Export displayed page as CSV");
            export.addActionListener(e -> exportCSV()); file.add(export); menu.add(file);
            add(menu, BorderLayout.NORTH);
            JPanel input = new JPanel(new FlowLayout(FlowLayout.LEFT));
            input.add(new JLabel("Student ID:")); input.add(student);
            input.add(new JLabel("Event:")); input.add(event); input.add(register);
            input.add(new JLabel("Filter:")); input.add(filter); input.add(refresh);
            JPanel center = new JPanel(new BorderLayout()); center.add(input, BorderLayout.NORTH);
            center.add(new JScrollPane(table), BorderLayout.CENTER); add(center, BorderLayout.CENTER);
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            bottom.add(prev); bottom.add(next); bottom.add(status); add(bottom, BorderLayout.SOUTH);
            register.addActionListener(e -> registerAsync());
            refresh.addActionListener(e -> { page = 0; loadAsync(); });
            filter.addActionListener(e -> { page = 0; loadAsync(); });
            prev.addActionListener(e -> { if (page > 0) { page--; loadAsync(); } });
            next.addActionListener(e -> { if (model.getRowCount() == PAGE_SIZE) { page++; loadAsync(); } });
        }
        void registerAsync() {
            String id = student.getText().trim(), selectedEvent = (String) event.getSelectedItem();
            if (!id.matches("[A-Za-z0-9_-]{3,30}")) { error(this, "Enter a valid student ID (3-30 characters)"); return; }
            register.setEnabled(false); status.setText("Saving registration...");
            new SwingWorker<Void, Void>() {
                @Override protected Void doInBackground() throws Exception { service.register(id, selectedEvent); return null; }
                @Override protected void done() {
                    register.setEnabled(true);
                    try { get(); status.setText("Registration saved"); loadAsync(); }
                    catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        if (cause instanceof SQLIntegrityConstraintViolationException ||
                            (cause instanceof SQLException && "23000".equals(((SQLException) cause).getSQLState())))
                            error(EventPanel.this, "Duplicate registration: student already registered for this event");
                        else error(EventPanel.this, "Registration failed: " + cause.getMessage());
                        status.setText("Registration failed");
                    }
                }
            }.execute();
        }
        void loadAsync() {
            final int request = ++requestId, requestedPage = page;
            final String selected = (String) filter.getSelectedItem();
            status.setText("Loading page " + (page + 1) + "...");
            new SwingWorker<List<Registration>, Void>() {
                @Override protected List<Registration> doInBackground() throws Exception {
                    return service.repository.page(selected, PAGE_SIZE, requestedPage * PAGE_SIZE);
                }
                @Override protected void done() {
                    if (request != requestId) return;
                    try {
                        List<Registration> rows = get(); model.setRowCount(0);
                        for (Registration r : rows) model.addRow(new Object[]{r.student, r.event});
                        status.setText("Page " + (requestedPage + 1) + " | " + rows.size() + " records");
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        status.setText("Database error"); error(EventPanel.this, cause.getMessage());
                    }
                }
            }.execute();
        }
        void exportCSV() {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("registrations_page.csv"));
            if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()), StandardCharsets.UTF_8))) {
                out.println("Student ID,Event");
                for (int i = 0; i < model.getRowCount(); i++) {
                    out.println(csv(model.getValueAt(i, 0).toString()) + "," + csv(model.getValueAt(i, 1).toString()));
                }
                status.setText("Exported current page");
            } catch (IOException ex) { error(this, ex.getMessage()); }
        }
        String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JavaAssignment4 app = new JavaAssignment4();
            app.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            app.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    HospitalPanel hospital = (HospitalPanel) ((JTabbedPane) app.getContentPane().getComponent(0)).getComponentAt(1);
                    if (hospital.confirmClose()) app.dispose();
                }
            });
            app.setVisible(true);
        });
    }
}
