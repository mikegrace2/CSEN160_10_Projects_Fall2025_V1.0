package _5040;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The WelcomePage is the first page displayed when the application starts.
 * It provides an overview of the Spectral Layers tool, including a basic
 * guide and game description. Users may navigate between tabs and press the
 * "Get Started" button to begin using the application.
 */
public class WelcomePage extends JDialog {
    public WelcomePage(JFrame parent) {
        super(parent, "Welcome to Spectral Layers", true);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        createContent();
    }

    /**
     * createContent ceates and arranges the main content of the welcome dialog, including:
     * - a header with the application title and image
     * - a tab format containing information about the application
     * - a button that allows the user to proceed to using the application
     */
    private void createContent() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel header = new JLabel("Spectral Layers - Digital Art Application");
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(new Color(86, 3, 25));
        header.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel headerImage = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon("src/images/Front_Page.png");
            int scaledWidth = originalIcon.getIconWidth() / 6;
            int scaledHeight = originalIcon.getIconHeight() / 6;
            Image scaledImage = originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            headerImage.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            headerImage.setText("Logo");
            headerImage.setForeground(Color.GRAY);
        }
        headerImage.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(header, BorderLayout.CENTER);
        headerPanel.add(headerImage, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Quick Start", createQuickStartPanel());

        tabbedPane.addTab("About the Game", createFeaturesPanel());

        add(tabbedPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Get Started!");
        closeButton.addActionListener((ActionEvent e) -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * The createQuickStartPanel function builds the intro tab, which provides a description of
     * features the application offers. The content is wrapped in
     * a scrollable panel for readability.
     *
     * @return a JComponent containing the Intro instructions
     */
    private JComponent createQuickStartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel textLabel = new JLabel(
                "<html><div style='font-family: Segoe UI; font-size: 14px; color: #323232; padding: 20px; background-color: #f8f8f8; border: 1px solid #c8c8c8; width: 100%;'>" +
                        "<b style='font-size: 16px; color: #0064c8;'>QUICK START GUIDE</b><br><br>" +
                        "Welcome to Spectral Layers! This educational app teaches <br>" +
                        "you about Painter's Algorithm through hands-on experience <br>" +
                        "with layer blending.<br><br>" +

                        "<b>GETTING STARTED</b><br>" +
                        "<span style='font-weight: normal;'>• Sandbox Mode: Create your own artistic compositions<br>" +
                        "• Challenge Mode: Test your skills against pre-made designs<br>" +
                        "• Save & Load: Preserve your work and continue later</span><br><br>" +

                        "<b>BASIC CONTROLS</b><br>" +
                        "<span style='font-weight: normal;'>• Toolbar: Add layers with colors and blend modes<br>" +
                        "• Click & Drag: Move layers around the canvas<br>" +
                        "• Corner Handles: Resize selected layers</span><br><br>" +

                        "<b>TIPS</b><br>" +
                        "<span style='font-weight: normal;'>• Experiment with different blend modes for unique effects<br>" +
                        "• Overlap layers to see color interactions<br>" +
                        "• Use challenge mode to improve your spatial reasoning</span>" +
                        "</div></html>"
        );

        JScrollPane scrollPane = new JScrollPane(textLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * The createFeaturesPanel creates a new tab explaining the game mode, its features,
     * and how users can interact with challenge-based tasks. This content is also
     * scrollable.
     *
     * @return a JComponent containing the game feature description
     */
    private JComponent createFeaturesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel textLabel = new JLabel(
                "<html><div style='font-family: Segoe UI; font-size: 14px; color: #323232; padding: 20px; background-color: #f8f8f8; border: 1px solid #c8c8c8; width: 100%; min-height: 100%;'>" +
                        "<b style='font-size: 16px; color: #0064c8;'>GAME FEATURES</b><br><br>" +
                        "This game can help you test how well you are able to <br>" +
                        "understand labeling by trying to match a preexisting drawing. <br><br>" +
                        "Hints will be given and an accuracy of how close you are <br>" +
                        "to that drawing will also be given." +
                        "</div></html>"
        );

        textLabel.setVerticalAlignment(SwingConstants.TOP);

        JScrollPane scrollPane = new JScrollPane(textLabel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setPreferredSize(new Dimension(0, 0)); // Let it expand
        JViewport viewport = scrollPane.getViewport();
        viewport.setBackground(new Color(248, 248, 248)); // Match your background color

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}