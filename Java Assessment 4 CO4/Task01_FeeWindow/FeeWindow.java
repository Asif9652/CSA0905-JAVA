import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(16);
    private final JTextField amount = new JTextField(12);
    private final JLabel status = new JLabel("Enter fee details.");
    private final FeeService service = new FeeService();
    private String receiptId;

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new BorderLayout(8, 8));

        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 8));
        fields.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        fields.add(new JLabel("Registration number:")); fields.add(regNo);
        fields.add(new JLabel("Amount (Rs):")); fields.add(amount);

        JButton pay = new JButton("Pay");
        JButton clear = new JButton("Clear");
        JButton receipt = new JButton("Receipt");
        JPanel actions = new JPanel(new FlowLayout());
        actions.add(pay); actions.add(clear); actions.add(receipt);

        add(fields, BorderLayout.NORTH);
        add(actions, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 12, 12, 12));
        add(status, BorderLayout.SOUTH);

        pay.addActionListener(e -> handlePay());
        clear.addActionListener(e -> {
            regNo.setText(""); amount.setText(""); receiptId = null;
            status.setText("Fields cleared.");
        });
        receipt.addActionListener(e -> showReceipt());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(FeeWindow.this, "Close the fee window?",
                        "Confirm exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) dispose();
            }
        });
        pack(); setLocationRelativeTo(null);
    }

    private void handlePay() {
        PaymentResult result = service.pay(regNo.getText(), amount.getText());
        status.setText(result.getMessage());
        receiptId = result.getReceipt();
        JOptionPane.showMessageDialog(this, result.getMessage(),
                result.isSuccess() ? "Payment" : "Validation",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    private void showReceipt() {
        if (receiptId == null) {
            JOptionPane.showMessageDialog(this, "No successful payment is available.");
        } else {
            JOptionPane.showMessageDialog(this, "Receipt: " + receiptId);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
