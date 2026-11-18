import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

public class ConverterAnonymous extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");

    public ConverterAnonymous() {
        super("Converter – Anonymous Inner Class");
        setLayout(new FlowLayout());
        JButton button = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target); add(button); add(result);
        add(new JLabel("Demo rates: 1 INR = 0.012 USD | 1 INR = 0.011 EUR"));
        ActionListener handler = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(amount.getText().trim());
                    if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
                    String c = (String) target.getSelectedItem();
                    double rate = c.equals("USD") ? 0.012 : 0.011;
                    String text = String.format("%.2f %s", value * rate, c);
                    result.setText(text);
                    JOptionPane.showMessageDialog(ConverterAnonymous.this, text);
                } catch (NumberFormatException ex) {
                    result.setText("Invalid amount");
                    JOptionPane.showMessageDialog(ConverterAnonymous.this, "Invalid amount",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        button.addActionListener(handler);
        amount.addActionListener(handler);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack(); setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterAnonymous().setVisible(true));
    }
}
