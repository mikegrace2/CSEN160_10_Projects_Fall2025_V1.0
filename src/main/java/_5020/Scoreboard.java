package _5020;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.prefs.Preferences; // for storing scores

public class Scoreboard {

    public Scoreboard(JFrame parent) {
        // Using Java Preferences API to store and retrieve scores
        Preferences prefs = Preferences.userNodeForPackage(MemoryGame.class);
        String history = prefs.get("scoreHistory", ""); // comma-separated scores

        String[] columnNames = { "Game", "Score" };

        String[][] tableData; // 2D array to store table data
        if (history.isEmpty()) {
            // default display message
            tableData = new String[][] { { "-", "No scores yet!" } };
        } else {
            String[] scores = history.split(","); // split into individual scores from the string
            tableData = new String[scores.length][2];
            for (int i = 0; i < scores.length; i++) {
                tableData[i][0] = String.valueOf(i + 1); // column 0 is the game number
                tableData[i][1] = scores[i]; // column 1 is the score
            }
        }

        // creating the table model to display
        DefaultTableModel model = new DefaultTableModel(tableData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // non-editable table
        };

        // creating the table itself
        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(Color.WHITE);

        // Center the text
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
        JTableHeader header = table.getTableHeader();
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Custom renderer to color cells
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (value != null && !value.toString().isEmpty()) {
                    c.setBackground(new Color(226, 108, 47)); // orange for filled cells
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE); // optional: empty cells white
                    c.setForeground(Color.BLACK);
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        // adds scrolling for a longer table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        // creating the main panel to hold everything
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(new Color(50, 50, 50)); // optional panel bg
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // title label to show at the top of the scoreboard popup
        JLabel titleLabel = new JLabel("Your Past Scores", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER); // adding the table scroll pane to center

        // creating the popup window to show the scoreboard
        JDialog dialog = new JDialog(parent, "Scoreboard", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(panel);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}