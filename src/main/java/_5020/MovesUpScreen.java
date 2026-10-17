package _5020;

import javax.swing.*;
import java.awt.*;

public class MovesUpScreen extends JFrame {
    private final Color backgroundColor = new Color(233, 164, 0);
    private final Color controlBackground = new Color(118, 48, 8);
    private final Color controlForeground = new Color(239, 223, 160);

    public MovesUpScreen(JFrame owner, int rows, int cols, int seconds, int moves) {
        super("Out of Moves");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        JLabel title = new JLabel("Out of Moves", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(Color.WHITE);
        panel.add(title, c);

        c.gridy = 1;
        JLabel msg = new JLabel("You have exhausted your move limit.", SwingConstants.CENTER);
        msg.setFont(msg.getFont().deriveFont(Font.PLAIN, 16f));
        msg.setForeground(Color.WHITE);
        panel.add(msg, c);

        RoundButton replay = new RoundButton("Replay");
        RoundButton home = new RoundButton("Home");

        replay.setPreferredSize(new Dimension(120, 36));
        home.setPreferredSize(new Dimension(120, 36));

        replay.addActionListener(e -> {
            if (owner != null) owner.dispose();
            new MemoryGame(rows, cols, seconds, moves);
            dispose();
        });

        home.addActionListener(e -> {
            if (owner != null) owner.dispose();
            new HomeScreen();
            dispose();
        });

        c.gridy = 2; c.gridwidth = 1; c.gridx = 0;
        panel.add(replay, c);
        c.gridx = 1;
        panel.add(home, c);

        add(panel);
        setVisible(true);
    }

    private class RoundButton extends JButton {
        public RoundButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(controlForeground);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            int arc = Math.min(40, Math.min(width, height) / 2);
            g2.setColor(controlBackground);
            g2.fillRoundRect(0, 0, width, height, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() { return false; }
    }
}
