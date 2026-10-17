package _5040;

import java.awt.*;
import java.util.List;
/**
 * ChallengeScorer compares a player's drawing against the challenge drawing
 * by evaluating each layer in three categories:
 *
 *  - Color similarity
 *  - Position similarity
 *  - Blend mode accuracy
 *
 * Each category contributes to a score for that layer.
 * The final score is averaged across all target layers and
 * is a value between 0.0 and 1.0.
 *
 * The scoring uses smooth falloff functions rather than 100-0
 * which allows "Close enough" to receive high scores, and "slightly farther"
 * to get a decent score. Like a real grading system
 */
public class ChallengeScorer {
    /**
     * Big method for this class.
     * Compares the player's layers to the target layers and produces a score
     * between 0.0 and 1.0. Only overlapping indices are compared; if the
     * player has fewer layers, only the matching ones are scored.
     *
     * Each layer contributes a weighted score:
     *   - 45% from color similarity
     *   - 35% from position similarity
     *   - 20% from blend mode accuracy. This is the most all-or-nothing part
     *
     * @param target the list of layers that define the correct solution
     * @param player the list of layers drawn by the user
     * @return a normalized similarity score from 0.0 to 1.0
     */
    public static double compare(List<Layer> target, List<Layer> player) {
        if (player.isEmpty() || target.isEmpty()) return 0.0;

        int n = Math.min(target.size(), player.size());
        double totalScore = 0;

        for (int i = 0; i < n; i++) {
            Layer t = target.get(i);
            Layer p = player.get(i);

            double colorScore = colorSimilarity(t.getColor(), p.getColor());
            double posScore   = positionSimilarity(t.getShape(), p.getShape());
            double blendScore = (t.getBlendMode() == p.getBlendMode()) ? 1.0 : 0.0;

            // Weighted layer score
            double layerScore = (0.45 * colorScore) +
                    (0.35 * posScore) +
                    (0.20 * blendScore);

            totalScore += layerScore;
        }

        // Normalize by number of target layers
        return totalScore / target.size();
    }

    // Normal Falling off Function for Scoring, rather than pure 100 or 0
    private static double colorSimilarity(Color c1, Color c2) {
        double dist = colorDistance(c1, c2);  // 0 - 441
        return Math.max(0, 1 - (dist / 200.0));
    }
    private static double positionSimilarity(Rectangle r1, Rectangle r2) {
        double dist = posDistance(r1, r2);
        return Math.max(0, 1 - (dist / 80.0));
    }
    private static double colorDistance(Color c1, Color c2) {
        int dr = c1.getRed() - c2.getRed();
        int dg = c1.getGreen() - c2.getGreen();
        int db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
    private static double posDistance(Rectangle r1, Rectangle r2) {
        // Top-left corner distance
        int dx1 = r1.x - r2.x;
        int dy1 = r1.y - r2.y;
        double d1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);

        // Bottom-right corner distance
        int r1brX = r1.x + r1.width;
        int r1brY = r1.y + r1.height;
        int r2brX = r2.x + r2.width;
        int r2brY = r2.y + r2.height;
        int dx2 = r1brX - r2brX;
        int dy2 = r1brY - r2brY;
        double d2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

        // Average the two distances
        return (d1 + d2) / 2.0;
    }
}
