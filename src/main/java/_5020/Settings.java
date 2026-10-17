package _5020;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

public class Settings extends JFrame {
    private final JSlider rowsSlider;
    private final JSlider colsSlider;
    private final JSlider timeSlider;
    private final JSlider movesSlider;
    private final JLabel rowsLabel;
    private final JLabel colsLabel;
    private final JLabel pairsLabel;
    private final JLabel timeLabel;
    private final JLabel movesLabel;

    // appearance
    private final Color backgroundColor = new Color(233, 164, 0);
    private final Color controlBackground = new Color(118, 48, 8);
    private final Color controlForeground = new Color(239, 223, 160);

    // previous values + guard to avoid recursion while adjusting sliders
    private int lastRows;
    private int lastCols;
    private boolean adjusting = false;

    public Settings(HomeScreen parent) {
        super("Memory Game — Settings");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 420);
        setLocationRelativeTo(null);

        int initRows = parent != null ? parent.getRows() : 4;
        int initCols = parent != null ? parent.getCols() : 4;
        lastRows = initRows;
        lastCols = initCols;

        rowsSlider = new JSlider(2, 5, initRows);
        colsSlider = new JSlider(2, 5, initCols);

        rowsLabel = new JLabel();
        colsLabel = new JLabel();
        pairsLabel = new JLabel();
        timeLabel = new JLabel();
        movesLabel = new JLabel();

        timeSlider = new JSlider(); // configured in updateLimits()
        movesSlider = new JSlider();

        // apply custom rounded look & feel for sliders
        applyRoundSlider(rowsSlider);
        applyRoundSlider(colsSlider);
        applyRoundSlider(timeSlider);
        applyRoundSlider(movesSlider);

        ChangeListener rcListener = this::onGridChanged;
        rowsSlider.addChangeListener(rcListener);
        colsSlider.addChangeListener(rcListener);

        movesSlider.addChangeListener(e -> movesLabel.setText(Integer.toString(movesSlider.getValue())));
        timeSlider.addChangeListener(e -> timeLabel.setText(timeSlider.getValue() + " s"));

        movesSlider.setPaintTicks(true);
        movesSlider.setPaintLabels(true);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);

        RoundButton saveButton = new RoundButton("Save");
        RoundButton backButton = new RoundButton("Back");

        saveButton.addActionListener(e -> {
            int r = rowsSlider.getValue();
            int c = colsSlider.getValue();
            if ((r * c) % 2 != 0) {
                // should not happen because we snap, but guard anyway
                JOptionPane.showMessageDialog(this,
                        "Grid must have an even number of tiles. Adjust rows or columns.",
                        "Invalid grid", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (parent != null) {
                parent.setRows(r);
                parent.setCols(c);
                parent.setSeconds(timeSlider.getValue());
                parent.setMoves(movesSlider.getValue());
                parent.setVisible(true);
                parent.toFront();
                parent.requestFocus();
            }
            dispose();
        });

        backButton.addActionListener(e -> {
            dispose();
            if (parent != null) {
                parent.setVisible(true);
                parent.toFront();
                parent.requestFocus();
            }
        });

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.setLayout(new GridBagLayout());
        panel.setBackground(backgroundColor);
        panel.setOpaque(true);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Controls start at row 0 (no warning row)
        c.gridwidth = 1;
        c.gridy = 0; c.gridx = 0;
        panel.add(new JLabel("Rows:"), c);
        c.gridx = 1;
        panel.add(rowsSlider, c);
        c.gridx = 2;
        panel.add(rowsLabel, c);

        c.gridy = 1; c.gridx = 0;
        panel.add(new JLabel("Columns:"), c);
        c.gridx = 1;
        panel.add(colsSlider, c);
        c.gridx = 2;
        panel.add(colsLabel, c);

        c.gridy = 2; c.gridx = 0; c.gridwidth = 3;
        panel.add(pairsLabel, c);

        c.gridy = 3; c.gridwidth = 1; c.gridx = 0;
        panel.add(new JLabel("Time (s):"), c);
        c.gridx = 1;
        panel.add(timeSlider, c);
        c.gridx = 2;
        panel.add(timeLabel, c);

        c.gridy = 4; c.gridx = 0;
        panel.add(new JLabel("Move Limit:"), c);
        c.gridx = 1;
        panel.add(movesSlider, c);
        c.gridx = 2;
        panel.add(movesLabel, c);

        c.gridy = 5; c.gridx = 0; c.gridwidth = 3;
        panel.add(new JLabel(""), c); // spacer

        c.gridy = 6; c.anchor = GridBagConstraints.CENTER; c.gridwidth = 3;
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonsPanel.setOpaque(false);
        saveButton.setPreferredSize(new Dimension(100, 32));
        backButton.setPreferredSize(new Dimension(100, 32));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(backButton);
        panel.add(buttonsPanel, c);

        add(panel);
        updateLimits(); // initial configure (also sets slider values and labels)
        setVisible(true);
    }

    private void onGridChanged(ChangeEvent e) {
        if (adjusting) return;
        adjusting = true;
        Object src = e.getSource();

        int rows = rowsSlider.getValue();
        int cols = colsSlider.getValue();

        // If odd tile count, snap the changed slider to nearest value that makes tiles even.
        if ((rows * cols) % 2 != 0) {
            boolean adjusted = false;
            if (src == rowsSlider) {
                int tryUp = rows + 1;
                int tryDown = rows - 1;
                if (tryUp <= rowsSlider.getMaximum()) {
                    rowsSlider.setValue(tryUp);
                    adjusted = true;
                } else if (tryDown >= rowsSlider.getMinimum()) {
                    rowsSlider.setValue(tryDown);
                    adjusted = true;
                } else {
                    int tryColsUp = cols + 1;
                    int tryColsDown = cols - 1;
                    if (tryColsUp <= colsSlider.getMaximum()) {
                        colsSlider.setValue(tryColsUp);
                        adjusted = true;
                    } else if (tryColsDown >= colsSlider.getMinimum()) {
                        colsSlider.setValue(tryColsDown);
                        adjusted = true;
                    }
                }
            } else if (src == colsSlider) {
                int tryUp = cols + 1;
                int tryDown = cols - 1;
                if (tryUp <= colsSlider.getMaximum()) {
                    colsSlider.setValue(tryUp);
                    adjusted = true;
                } else if (tryDown >= colsSlider.getMinimum()) {
                    colsSlider.setValue(tryDown);
                    adjusted = true;
                } else {
                    int tryRowsUp = rows + 1;
                    int tryRowsDown = rows - 1;
                    if (tryRowsUp <= rowsSlider.getMaximum()) {
                        rowsSlider.setValue(tryRowsUp);
                        adjusted = true;
                    } else if (tryRowsDown >= rowsSlider.getMinimum()) {
                        rowsSlider.setValue(tryRowsDown);
                        adjusted = true;
                    }
                }
            } else {
                if (cols < colsSlider.getMaximum()) {
                    colsSlider.setValue(cols + 1);
                    adjusted = true;
                }
            }

            if (!adjusted) {
                // If we couldn't find a valid adjustment, revert to previous values
                rowsSlider.setValue(lastRows);
                colsSlider.setValue(lastCols);
            }
        }

        updateLimits();
        // store last valid values
        lastRows = rowsSlider.getValue();
        lastCols = colsSlider.getValue();

        adjusting = false;
    }

    private void updateLimits() {
        int rows = rowsSlider.getValue();
        int cols = colsSlider.getValue();
        int tiles = rows * cols;
        boolean even = tiles % 2 == 0;
        int pairs = tiles / 2;

        rowsLabel.setText(Integer.toString(rows));
        colsLabel.setText(Integer.toString(cols));
        pairsLabel.setText("Total tiles: " + tiles + (even ? " (" + pairs + " pairs)" : " (not even)"));

        int minMoves = Math.max(1, pairs);
        int maxMoves = Math.max(minMoves, pairs * 10);
        int defaultMoves = Math.min(maxMoves, Math.max(minMoves, pairs * 2));

        int minTime = Math.max(10, pairs * 5);
        int maxTime = Math.max(minTime, pairs * 30);
        int defaultTime = Math.min(maxTime, Math.max(minTime, pairs * 8));

        movesSlider.setMinimum(minMoves);
        movesSlider.setMaximum(maxMoves);
        int currentMoves = movesSlider.getValue() == 0 ? defaultMoves : movesSlider.getValue();
        movesSlider.setValue(Math.min(currentMoves, maxMoves));
        movesSlider.setMajorTickSpacing(Math.max(1, (maxMoves - minMoves) / 4));
        movesSlider.setLabelTable(movesSlider.createStandardLabels(Math.max(1, (maxMoves - minMoves) / 4)));
        movesLabel.setText(Integer.toString(movesSlider.getValue()));

        timeSlider.setMinimum(minTime);
        timeSlider.setMaximum(maxTime);
        int currentTime = timeSlider.getValue() == 0 ? defaultTime : timeSlider.getValue();
        timeSlider.setValue(Math.min(currentTime, maxTime));
        timeSlider.setMajorTickSpacing(Math.max(1, (maxTime - minTime) / 4));
        timeSlider.setLabelTable(timeSlider.createStandardLabels(Math.max(1, (maxTime - minTime) / 4)));
        timeLabel.setText(timeSlider.getValue() + " s");
    }

    // apply custom rounded slider UI and basic styling
    private void applyRoundSlider(JSlider slider) {
        slider.setUI(new RoundedSliderUI(slider));
        slider.setOpaque(false);
        slider.setBackground(new Color(0, 0, 0, 0));
        slider.setForeground(controlBackground);
        slider.putClientProperty("thumbColor", controlBackground);
    }

    // Rounded button implementation (uses Settings' control colors)
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
        public boolean isOpaque() {
            return false;
        }
    }

    // Small custom SliderUI that paints a round thumb and a rounded track
    private static class RoundedSliderUI extends BasicSliderUI {

        public RoundedSliderUI(JSlider b) {
            super(b);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(16, 16);
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color thumbColor = (Color) slider.getClientProperty("thumbColor");
            if (thumbColor == null) thumbColor = new Color(180, 180, 180);

            int x = thumbRect.x;
            int y = thumbRect.y;
            int w = thumbRect.width;
            int h = thumbRect.height;

            g2.setColor(thumbColor);
            g2.fillOval(x, y, w, h);

            g2.setColor(thumbColor.darker());
            g2.drawOval(x, y, w - 1, h - 1);

            g2.dispose();
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cy = trackRect.y + trackRect.height / 2;
            int x1 = trackRect.x;
            int x2 = trackRect.x + trackRect.width;

            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(200, 200, 200));
            g2.drawLine(x1, cy, x2, cy);

            int pos = thumbRect.x + thumbRect.width / 2;
            g2.setColor(new Color(188, 85, 49));
            g2.drawLine(x1, cy, pos, cy);

            g2.dispose();
        }
    }
}