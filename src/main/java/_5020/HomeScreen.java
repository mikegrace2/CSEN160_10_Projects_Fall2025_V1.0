package _5020;

import javax.swing.*;
import java.awt.*;

public class HomeScreen extends JFrame {
    // persisted settings
    private int rows = 4;
    private int cols = 4;
    private int seconds = 60;
    private int moves = 32;

    public HomeScreen() {
        super("Memory Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Start maximized to fill the screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Prepare background image from classpath
        JLabel backgroundLabel = createBackgroundLabel("/kitchen-background.jpg");
        backgroundLabel.setLayout(new BorderLayout());
        setContentPane(backgroundLabel);

        // Top area: title centered, settings button at the right
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 24));

        FadeLabel title = new FadeLabel("Welcome to the Memory Game", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 42f));
        title.setForeground(Color.WHITE);
        title.setOpaque(false);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(title, BorderLayout.CENTER);

        RoundButton settingsButton = new RoundButton("Settings");
        JPanel settingsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        settingsWrapper.setOpaque(false);
        settingsButton.setPreferredSize(new Dimension(60, 20));
        settingsButton.setBackground(new Color(118, 48, 8)); // light pink
        settingsButton.setForeground(Color.white);
        settingsButton.setBorder(null); // ensure no border
        settingsWrapper.add(settingsButton);
        topPanel.add(settingsWrapper, BorderLayout.EAST);

        // Center area: large rounded, light-pink Start button centered
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        RoundButton startButton = new RoundButton("Start");
        startButton.setPreferredSize(new Dimension(360, 120));
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 28f));
        startButton.setBackground(new Color(188, 85, 49)); // light pink
        startButton.setForeground(Color.white);
        startButton.setBorder(null); // ensure no border

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(startButton, gbc);

        RoundButton scoreButton = new RoundButton("Scoreboard");
        scoreButton.setPreferredSize(new Dimension(250, 100));
        scoreButton.setFont(scoreButton.getFont().deriveFont(Font.BOLD, 20f));
        scoreButton.setBackground(new Color(118, 48, 8)); // light pink
        scoreButton.setForeground(Color.white);
        scoreButton.setBorder(null); // ensure no border

        gbc.gridy = 1;
        gbc.insets = new Insets(40, 0, 0, 0); //
        centerPanel.add(scoreButton, gbc);

        // Actions
        startButton.addActionListener(e -> startGame());
        settingsButton.addActionListener(e -> new Settings(this));
        scoreButton.addActionListener(e -> new Scoreboard(this));

        // Compose frame
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void startGame() {
        new MemoryGame(rows, cols, seconds, moves);
        dispose();
    }

    private JLabel createBackgroundLabel(String resourcePath) {
        java.net.URL imgUrl = getClass().getResource("/kitchen-background.jpg");
        if (imgUrl != null) {
            ImageIcon icon = new ImageIcon(imgUrl);
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Image scaled = icon.getImage().getScaledInstance(screen.width, screen.height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(scaled));
        } else {
            // fallback: plain dark background
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            lbl.setBackground(new Color(30, 30, 30));
            return lbl;
        }
    }

    // getters and setters for persisted settings
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    @SuppressWarnings("unused")
    public int getSeconds() { return seconds; }
    public void setSeconds(int seconds) { this.seconds = seconds; }

    @SuppressWarnings("unused")
    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }

    // Rounded button implementation that paints a rounded background color (no border)
    private static class RoundButton extends JButton {
        public RoundButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = Math.min(40, Math.min(width, height) / 4);

            // paint background rounded rect
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            g2.dispose();

            // Draw text and other standard decorations
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    // FadeLabel paints a translucent rounded bar behind the text for better contrast
    private static class FadeLabel extends JLabel {
        public FadeLabel(String text, int alignment) {
            super(text, alignment);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics(getFont());
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getHeight();

            int padX = 24;
            int padY = 10;
            int rectWidth = Math.min(getWidth() - 40, textWidth + padX * 2);
            int rectHeight = textHeight + padY * 2;

            int rectX = (getWidth() - rectWidth) / 2;
            int rectY = (getHeight() - rectHeight) / 2;

            // translucent rounded rectangle behind the text
            Color barColor = new Color(0, 0, 0, 150);
            g2.setColor(barColor);
            int arc = Math.min(24, rectHeight / 2);
            g2.fillRoundRect(rectX, rectY, rectWidth, rectHeight, arc, arc);

            g2.dispose();

            super.paintComponent(g);
        }
    }
}
