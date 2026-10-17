package _5040;

import javax.swing.*;
import java.awt.*;
import java.util.List;
/**
 * The ChallengeWindow displays a target set of layers that the user must try
 * to recreate. It shows a live preview of the challenge image, provides a
 * hint system, and allows the user to check how closely their drawing
 * matches the challenge. Also cheater mode once all hints are exhausted.
 *
 * Features:
 *  - Renders the challenge layers using the same Renderer as the main canvas that is view only.
 *  - Offers progressively revealing hints, followed by "cheater hints" that
 *    reveal exact layer details.
 *  - Contains a scoring button that compares the challenge against the user's
 *    current drawing using ChallengeScorer.
 *
 * This window is opened when the user clicks "Test your
 * Drawing Skills" from the toolbar.
 */
public class ChallengeWindow extends JFrame {

    private JTextArea hintArea;
    private int hintIndex = 0;
    private List<Layer> challengeLayers;

    /**
     * Creates a new ChallengeWindow displaying the target layers for the user
     * to analyze and recreate. The window contains:
     *  - A preview panel showing the challenge image.
     *  - A scrollable text area for hints.
     *  - Buttons to request more hints or check the user's accuracy.
     *
     * @param challengeLayers the list of layers that define the challenge
     * @param playerManager   the player's LayerManager, used for scoring
     */
    public ChallengeWindow(List<Layer> challengeLayers, LayerManager playerManager) {
        this.challengeLayers = challengeLayers;

        setTitle("Challenge Viewer");
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Preview Panel, try to match this
        JPanel preview = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Renderer r = new Renderer((Graphics2D) g);
                r.draw(challengeLayers);
            }
        };
        add(preview, BorderLayout.CENTER);

        // Hint Area, Scrollable
        hintArea = new JTextArea(10, 40);
        hintArea.setEditable(false);
        hintArea.setLineWrap(true);
        hintArea.setWrapStyleWord(true);
        hintArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(hintArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.EAST);

        // Controls on the bottom to give next hint or check match
        JPanel bottom = new JPanel(new FlowLayout());
        JButton nextHint = new JButton("Next Hint");
        nextHint.addActionListener(e -> showNextHint());
        JButton clearHints = new JButton("Clear Hints");
        clearHints.addActionListener(e -> hintArea.setText(""));
        JButton checkButton = new JButton("Check My Drawing");
        checkButton.addActionListener(e -> {
            double score = ChallengeScorer.compare(challengeLayers, playerManager.getLayers());
            JOptionPane.showMessageDialog(this,
                    score >= 0.85 ? "Great match! (" + (int)(score * 100) + "%)" :
                            "Keep trying (" + (int)(score * 100) + "%)");
        });
        bottom.add(nextHint);
        bottom.add(clearHints);
        bottom.add(checkButton);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }
    private void showNextHint() {
        hintIndex++;
        int total = challengeLayers.size();
        StringBuilder sb = new StringBuilder();

        if (hintIndex <=  7) { // normal hints
            Layer l = challengeLayers.get(Math.min(hintIndex - 1, total - 1));
            Color c = l.getColor();
            Rectangle r = l.getShape();

            switch (hintIndex) {
                case 1 -> sb.append("Hint 1: There are ").append(total).append(" layers.\n");
                case 2 -> sb.append("Hint 2: First layer color ≈ ").append(colorToString(c)).append("\n");
                case 3 -> sb.append("Hint 3: One blend mode is ").append(l.getBlendMode()).append("\n");
                case 4 -> sb.append("Hint 4: Top-left layer starts near (").append(r.x).append(", ").append(r.y).append(")\n");
                case 5 -> sb.append("Hint 5: Smallest layer ≈ ").append(r.width).append("×").append(r.height).append("\n");
                case 6 -> sb.append("Hint 6: Average opacity ≈ ").append(String.format("%.2f", l.getOpacity())).append("\n");
                case 7 -> sb.append("Hint 7: Try overlapping layers to see blending!\n");
                default -> sb.append("Final normal hint: focus on color & blend accuracy.\n");
            }
        } else {
            // cheater mode
            int cheatIndex = hintIndex-8;

            if (cheatIndex < challengeLayers.size()) {
                Layer l = challengeLayers.get(cheatIndex);
                sb.append("Cheater Hint Because you Suck ").append(cheatIndex + 1).append(": Layer ")
                        .append(cheatIndex + 1)
                        .append(" → Color ").append(colorToString(l.getColor()))
                        .append(", Opacity ").append(String.format("%.2f", l.getOpacity()))
                        .append(", Blend ").append(l.getBlendMode())
                        .append(", Rect (").append(l.getShape().x).append(", ").append(l.getShape().y)
                        .append(", ").append(l.getShape().width).append(", ").append(l.getShape().height).append(")\n");
            } else {
                sb.append("Wow, you really suck! \n");
            }
        }

        hintArea.append(sb.toString());
        hintArea.append("--------------------------\n");
        hintArea.setCaretPosition(hintArea.getDocument().getLength()); // auto-scroll
    }
    private String colorToString(Color c) {
        return "RGB(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
    }
}
