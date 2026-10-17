package _5010;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages high score data stored in JSON format
 */
public class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.json";
    private int bestScore;
    private int bestLevel;

    public HighScoreManager() {
        loadHighScores();
    }

    /**
     * Load high scores from JSON file
     */
    public void loadHighScores() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists()) {
            // Initialize with default values
            bestScore = 0;
            bestLevel = 0;
            saveHighScores();
            return;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(HIGH_SCORE_FILE)));
            // Simple JSON parsing for {"bestScore":X,"bestLevel":Y}
            content = content.trim().replaceAll("\\s+", "");

            // Extract bestScore
            int scoreStart = content.indexOf("\"bestScore\":") + 12;
            int scoreEnd = content.indexOf(",", scoreStart);
            if (scoreEnd == -1)
                scoreEnd = content.indexOf("}", scoreStart);
            if (scoreStart > 11 && scoreEnd > scoreStart) {
                String scoreStr = content.substring(scoreStart, scoreEnd);
                bestScore = Integer.parseInt(scoreStr);
            } else {
                bestScore = 0;
            }

            // Extract bestLevel
            int levelStart = content.indexOf("\"bestLevel\":") + 12;
            int levelEnd = content.indexOf("}", levelStart);
            if (levelStart > 11 && levelEnd > levelStart) {
                String levelStr = content.substring(levelStart, levelEnd);
                bestLevel = Integer.parseInt(levelStr);
            } else {
                bestLevel = 0;
            }
        } catch (Exception e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            bestScore = 0;
            bestLevel = 0;
        }
    }

    /**
     * Save high scores to JSON file
     */
    public void saveHighScores() {
        try {
            String json = String.format("{\"bestScore\":%d,\"bestLevel\":%d}", bestScore, bestLevel);
            Files.write(Paths.get(HIGH_SCORE_FILE), json.getBytes());
        } catch (Exception e) {
            System.err.println("Error saving high scores: " + e.getMessage());
        }
    }

    /**
     * Update high scores if current score/level is better
     */
    public void updateHighScores(int score, int level) {
        boolean updated = false;
        if (score > bestScore) {
            bestScore = score;
            updated = true;
        }
        if (level > bestLevel) {
            bestLevel = level;
            updated = true;
        }
        if (updated) {
            saveHighScores();
        }
    }

    public int getBestScore() {
        return bestScore;
    }

    public int getBestLevel() {
        return bestLevel;
    }
}
