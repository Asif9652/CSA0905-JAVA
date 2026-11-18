import java.awt.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LibrarySearchScreen extends JFrame {
    private static final String[][] CATALOG = {
        {"Java: The Complete Reference", "Books"},
        {"Effective Java", "Books"},
        {"Introduction to Algorithms", "Books"},
        {"Database System Concepts", "Books"},
        {"Journal of Computer Science", "Journals"},
        {"Campus Technology Review", "Theses"}
    };
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<String> category =
            new JComboBox<>(new String[]{"All", "Books", "Journals", "Theses"});
    private final DefaultListModel<String> resultsModel = new DefaultListModel<>();
    private final JList<String> results = new JList<>(resultsModel);
    private final JLabel status = new JLabel("Ready");

    public LibrarySearchScreen() {
        super("Campus Library Search");
        setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Campus Library Search", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setBorder(new EmptyBorder(12, 8, 8, 8));
        add(title, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchRow.add(new JLabel("Search:"));
        searchRow.add(searchField);
        searchRow.add(category);
        JButton searchButton = new JButton("Search");
        searchRow.add(searchButton);

        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        results.setVisibleRowCount(8);
        resultsPanel.add(new JScrollPane(results), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);
        top.add(searchRow, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        JButton prev = new JButton("Prev");
        JButton next = new JButton("Next");
        JButton reserve = new JButton("Reserve");
        buttons.add(prev); buttons.add(next); buttons.add(reserve);
        bottom.add(buttons, BorderLayout.CENTER);
        bottom.add(status, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        prev.addActionListener(e -> status.setText("Previous page requested."));
        next.addActionListener(e -> status.setText("Next page requested."));
        reserve.addActionListener(e -> {
            String selected = results.getSelectedValue();
            status.setText(selected == null ? "Select a result first." : "Reserved: " + selected);
        });

        showCatalog("");
        setMinimumSize(new Dimension(520, 360));
        setSize(760, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void search() {
        String query = searchField.getText().trim();
        resultsModel.clear();
        if (query.isEmpty()) {
            status.setText("Enter a search term.");
            return;
        }
        showCatalog(query);
        status.setText(resultsModel.isEmpty() ? "No matching titles found."
                : resultsModel.size() + " result(s) for category " + category.getSelectedItem());
    }

    private void showCatalog(String query) {
        resultsModel.clear();
        String selectedCategory = (String) category.getSelectedItem();
        for (String[] item : CATALOG) {
            boolean categoryMatches = "All".equals(selectedCategory)
                    || item[1].equals(selectedCategory);
            boolean queryMatches = query.isEmpty()
                    || item[0].toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
            if (categoryMatches && queryMatches) resultsModel.addElement(item[0] + " [" + item[1] + "]");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearchScreen().setVisible(true));
    }
}
