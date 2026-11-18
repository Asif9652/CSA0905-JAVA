import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class AppointmentDesk extends JFrame {
    private final JTextField patientId = new JTextField(18);
    private final JComboBox<String> department = new JComboBox<>(
            new String[]{"Cardiology", "Dermatology", "General Medicine"});
    private final JComboBox<String> doctor = new JComboBox<>();
    private final JComboBox<String> timeSlot = new JComboBox<>();
    private final JLabel status = new JLabel("Select a department and appointment time.");
    private final Map<String, List<String>> doctorsByDepartment = new LinkedHashMap<>();
    private final Map<String, List<String>> slotsByDoctor = new LinkedHashMap<>();

    public AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        doctorsByDepartment.put("Cardiology", List.of("Dr. A. Rahman", "Dr. S. Iyer"));
        doctorsByDepartment.put("Dermatology", List.of("Dr. M. Khan", "Dr. P. Das"));
        doctorsByDepartment.put("General Medicine", List.of("Dr. R. Sen", "Dr. N. Thomas"));
        for (String name : List.of("Dr. A. Rahman", "Dr. A. Rahman", "Dr. M. Khan",
                "Dr. M. Khan", "Dr. R. Sen", "Dr. R. Sen")) {
            slotsByDoctor.put(name, List.of("09:00", "10:30", "14:00"));
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));
        addRow(form, 0, "Patient ID:", patientId);
        addRow(form, 1, "Department:", department);
        addRow(form, 2, "Doctor:", doctor);
        addRow(form, 3, "Available time:", timeSlot);

        JButton book = new JButton("Book Appointment");
        JButton cancel = new JButton("Cancel");
        JPanel actions = new JPanel();
        actions.add(book);
        actions.add(cancel);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actions, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));
        bottom.add(status, BorderLayout.SOUTH);

        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        department.addActionListener(e -> refreshDoctors());
        doctor.addActionListener(e -> refreshSlots());
        book.addActionListener(e -> bookAppointment());
        cancel.addActionListener(e -> clearForm());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(AppointmentDesk.this, "Close the appointment desk?",
                        "Confirm exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    dispose();
                }
            }
        });
        refreshDoctors();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(component, c);
    }

    private void refreshDoctors() {
        doctor.removeAllItems();
        List<String> doctors = doctorsByDepartment.get((String) department.getSelectedItem());
        if (doctors != null) {
            for (String name : doctors) doctor.addItem(name);
        }
        refreshSlots();
    }

    private void refreshSlots() {
        timeSlot.removeAllItems();
        List<String> slots = slotsByDoctor.get((String) doctor.getSelectedItem());
        if (slots != null) {
            for (String slot : slots) timeSlot.addItem(slot);
        }
    }

    private void bookAppointment() {
        String id = patientId.getText().trim();
        if (id.isEmpty()) {
            showError("Enter a patient ID.");
            patientId.requestFocusInWindow();
            return;
        }
        String selectedDoctor = (String) doctor.getSelectedItem();
        String selectedSlot = (String) timeSlot.getSelectedItem();
        if (selectedDoctor == null || selectedSlot == null) {
            showError("Select an available doctor and time slot.");
            return;
        }
        String confirmation = "Appointment confirmed for patient " + id + " with "
                + selectedDoctor + " (" + department.getSelectedItem() + ") at " + selectedSlot + ".";
        status.setText(confirmation);
        JOptionPane.showMessageDialog(this, confirmation, "Appointment confirmed",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        patientId.setText("");
        department.setSelectedIndex(0);
        status.setText("Form cleared.");
        patientId.requestFocusInWindow();
    }

    private void showError(String message) {
        status.setText(message);
        JOptionPane.showMessageDialog(this, message, "Invalid appointment",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
