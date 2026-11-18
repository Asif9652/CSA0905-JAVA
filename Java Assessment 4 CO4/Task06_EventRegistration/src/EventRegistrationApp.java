import java.awt.*;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EventRegistrationApp extends JFrame {
    private final JTextField studentId = new JTextField(12);
    private final JTextField studentName = new JTextField(14);
    private final JComboBox<EventChoice> eventChoice = new JComboBox<>();
    private final JComboBox<EventChoice> filterChoice = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Student ID", "Student Name", "Event", "Registered At"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel status = new JLabel("Ready");
    private final RegistrationService service = new RegistrationService();
    private int page = 0;
    private static final int PAGE_SIZE = 25;

    private static class EventChoice {
        final int id; final String name;
        EventChoice(int id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    public EventRegistrationApp() {
        super("Campus Event Registration");
        setLayout(new BorderLayout(8, 8));
        setJMenuBar(createMenu());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Student registration"));
        form.add(new JLabel("Student ID")); form.add(studentId);
        form.add(new JLabel("Name")); form.add(studentName);
        form.add(new JLabel("Event")); form.add(eventChoice);
        JButton register = new JButton("Register");
        form.add(register);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Filter event:")); filters.add(filterChoice);
        JButton apply = new JButton("Apply filter");
        JButton prev = new JButton("Previous");
        JButton next = new JButton("Next");
        JButton export = new JButton("Export CSV");
        filters.add(apply); filters.add(prev); filters.add(next); filters.add(export);

        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.NORTH);
        north.add(filters, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(5, 8, 8, 8));
        add(status, BorderLayout.SOUTH);

        register.addActionListener(e -> registerStudent());
        apply.addActionListener(e -> { page = 0; loadPage(); });
        prev.addActionListener(e -> { if (page > 0) page--; loadPage(); });
        next.addActionListener(e -> { page++; loadPage(); });
        export.addActionListener(e -> exportCsv());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        loadEvents();
    }

    private JMenuBar createMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem refresh = new JMenuItem("Refresh");
        JMenuItem exit = new JMenuItem("Exit");
        refresh.addActionListener(e -> { page = 0; loadPage(); });
        exit.addActionListener(e -> dispose());
        file.add(refresh); file.addSeparator(); file.add(exit);
        bar.add(file);
        return bar;
    }

    private void loadEvents() {
        SwingWorker<Void, EventChoice> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                try (Connection c = Db.getConnection();
                     PreparedStatement ps = c.prepareStatement("SELECT event_id,event_name FROM events ORDER BY event_name");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) publish(new EventChoice(rs.getInt(1), rs.getString(2)));
                }
                return null;
            }
            @Override protected void process(List<EventChoice> chunks) {
                for (EventChoice choice : chunks) {
                    eventChoice.addItem(choice);
                    filterChoice.addItem(choice);
                }
                filterChoice.insertItemAt(new EventChoice(0, "All events"), 0);
                filterChoice.setSelectedIndex(0);
                loadPage();
            }
            @Override protected void done() {
                try { get(); } catch (Exception e) { showError("Could not load events: " + e.getMessage()); }
            }
        };
        worker.execute();
    }

    private Integer selectedFilterId() {
        EventChoice choice = (EventChoice) filterChoice.getSelectedItem();
        return choice == null || choice.id == 0 ? null : choice.id;
    }

    private void registerStudent() {
        EventChoice choice = (EventChoice) eventChoice.getSelectedItem();
        if (choice == null) { showError("No event is available."); return; }
        String id = studentId.getText();
        String name = studentName.getText();
        setBusy(true);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                service.register(id, name, choice.id);
                return null;
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    get();
                    status.setText("Registration successful.");
                    studentId.setText(""); studentName.setText("");
                    page = 0; loadPage();
                    JOptionPane.showMessageDialog(EventRegistrationApp.this,
                            "Registration completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { showError(rootMessage(ex)); }
            }
        }.execute();
    }

    private void loadPage() {
        Integer eventId = selectedFilterId();
        setBusy(true);
        new SwingWorker<List<Registration>, Void>() {
            @Override protected List<Registration> doInBackground() throws Exception {
                return service.page(eventId, page, PAGE_SIZE);
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    List<Registration> rows = get();
                    model.setRowCount(0);
                    for (Registration r : rows)
                        model.addRow(new Object[]{r.getId(), r.getStudentId(), r.getStudentName(),
                                r.getEventName(), r.getRegisteredAt()});
                    status.setText("Page " + (page + 1) + " – " + rows.size() + " registration(s)");
                } catch (Exception ex) { showError(rootMessage(ex)); }
            }
        }.execute();
    }

    private void exportCsv() {
        setBusy(true);
        Integer eventId = selectedFilterId();
        new SwingWorker<List<String[]>, Void>() {
            @Override protected List<String[]> doInBackground() throws Exception {
                return service.export(eventId);
            }
            @Override protected void done() {
                setBusy(false);
                try {
                    List<String[]> rows = get();
                    Path file = Path.of("registrations-export.csv");
                    try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                        writer.write("Student ID,Student Name,Event,Registered At");
                        writer.newLine();
                        for (String[] row : rows) {
                            for (int i = 0; i < row.length; i++) {
                                if (i > 0) writer.write(",");
                                writer.write("\"" + row[i].replace("\"", "\"\"") + "\"");
                            }
                            writer.newLine();
                        }
                    }
                    status.setText("Exported " + rows.size() + " rows to " + file.toAbsolutePath());
                } catch (Exception ex) { showError(rootMessage(ex)); }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        status.setText(busy ? "Working..." : "Ready");
    }

    private String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? t.toString() : t.getMessage();
    }

    private void showError(String message) {
        status.setText(message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventRegistrationApp().setVisible(true));
    }
}
