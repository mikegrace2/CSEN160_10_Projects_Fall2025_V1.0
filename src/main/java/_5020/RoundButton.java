package _5020;

import javax.swing.*;
import java.awt.*;

// A JButton subclass that renders as a circular button with custom background and foreground colors
public class RoundButton extends JButton {
    public RoundButton(String text, Color bg, Color fg) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(fg);
        setBackground(bg);
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setPreferredSize(new Dimension(44, 44));
        setBorder(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw circular background
        g2.setColor(getBackground());
        g2.fillOval(0, 0, w, h);

        // draw icon/text centered
        FontMetrics fm = g2.getFontMetrics(getFont());
        String txt = getText();
        int tx = (w - fm.stringWidth(txt)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(txt, tx, ty);

        g2.dispose();
        // do NOT call super.paintComponent(g) to avoid default focus/content artifacts
    }

    @Override
    public boolean contains(int x, int y) {
        int rx = getWidth() / 2;
        int ry = getHeight() / 2;
        int dx = x - rx;
        int dy = y - ry;
        int r = Math.min(rx, ry);
        return dx * dx + dy * dy <= r * r;
    }
}
