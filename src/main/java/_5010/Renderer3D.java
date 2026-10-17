package _5010;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 3D Renderer with perspective projection
 */
public class Renderer3D {
    private int width;
    private int height;
    private Graphics2D g2d;

    public Renderer3D(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setGraphics(Graphics2D g2d) {
        this.g2d = g2d;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public void render(GameState gameState) {
        // Clear screen with dark background
        g2d.setColor(new Color(20, 20, 35));
        g2d.fillRect(0, 0, width, height);

        if (gameState.getMode() == GameState.GameMode.PLAYING) {
            renderGameplay(gameState);
        } else if (gameState.getMode() == GameState.GameMode.MENU) {
            renderMenu(gameState);
        } else if (gameState.getMode() == GameState.GameMode.GAME_OVER) {
            renderGameOver(gameState);
        }
    }

    private void renderGameplay(GameState gameState) {
        Camera camera = gameState.getCamera();

        // Draw tube with moving rings
        drawTube(camera, gameState);

        // Sort obstacles by distance (back to front) for proper rendering
        List<Obstacle> obstacles = gameState.getObstacles();
        List<Obstacle> sortedObstacles = new ArrayList<>(obstacles);
        sortedObstacles.sort((a, b) -> {
            double distA = camera.getPosition().distance(a.getPosition());
            double distB = camera.getPosition().distance(b.getPosition());
            return Double.compare(distB, distA); // Sort back to front
        });

        // Render obstacles
        for (Obstacle obstacle : sortedObstacles) {
            renderObstacle(obstacle, camera);
        }

        // Render chasers
        List<ChaserObstacle> chasers = gameState.getChasers();
        for (ChaserObstacle chaser : chasers) {
            renderObstacle(chaser, camera);
        }

        // Draw level end indicator
        drawLevelEnd(gameState, camera);

        // Check for danger and draw red tint if collision course detected
        if (gameState.isDangerAhead()) {
            drawDangerTint();
        }

        // Draw UI
        drawUI(gameState);
    }

    private void drawDangerTint() {
        // Draw semi-transparent red overlay
        g2d.setColor(new Color(255, 0, 0, 80)); // Red with transparency
        g2d.fillRect(0, 0, width, height);
    }

    private void drawTube(Camera camera, GameState gameState) {
        // Coordinate system: X=left/right, Y=forward, Z=up/down
        double tubeRadius = camera.getTubeRadius();
        double playerY = camera.getPosition().y;
        double levelEndY = gameState.getLevelEndY();
        int segments = 16; // Number of segments for tube circle
        List<Double> ringYPositions = gameState.getTunnelRingYPositions();
        
        // Draw tube rings at their actual positions (they move backward like obstacles)
        g2d.setColor(new Color(60, 100, 150, 120));
        g2d.setStroke(new BasicStroke(2));
        
        // Draw all rings from behind player to level end
        // Rings are circles in the X-Z plane (horizontal plane), positioned at Y (forward)
        for (Double ringY : ringYPositions) {
            // Only draw rings that are visible and before level end
            if (ringY > playerY - 10 && ringY < Math.min(playerY + 60, levelEndY + 5)) {
                // Draw circle at this Y position (in X-Z plane)
                List<Vector3D> ringPoints = new ArrayList<>();
                for (int i = 0; i <= segments; i++) {
                    double angle = (i * 2.0 * Math.PI) / segments;
                    double x = tubeRadius * Math.cos(angle);
                    double z = tubeRadius * Math.sin(angle);
                    ringPoints.add(new Vector3D(x, ringY, z));
                }
                
                // Draw lines connecting points
                for (int i = 0; i < ringPoints.size() - 1; i++) {
                    drawLine(ringPoints.get(i), ringPoints.get(i + 1), camera);
                }
            }
        }
        
        // Draw connecting lines along the tube - continuous from behind player to level end
        g2d.setColor(new Color(50, 80, 120, 80));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < segments; i++) {
            double angle = (i * 2.0 * Math.PI) / segments;
            double x = tubeRadius * Math.cos(angle);
            double z = tubeRadius * Math.sin(angle);
            
            // Draw continuous line from behind player to level end (or visible range)
            double startY = Math.max(playerY - 10, 0);
            double endY = Math.min(playerY + 60, levelEndY);
            
            // Draw continuous line along this segment
            Vector3D start = new Vector3D(x, startY, z);
            Vector3D end = new Vector3D(x, endY, z);
            drawLine(start, end, camera);
        }
    }

    private void drawLevelEnd(GameState gameState, Camera camera) {
        // Coordinate system: Y is forward, Z is up
        double levelEndY = gameState.getLevelEndY();
        double playerY = camera.getPosition().y;
        double tubeRadius = camera.getTubeRadius();
        
        // Draw green finish circle if it's ahead
        if (levelEndY > playerY && levelEndY < playerY + 70) {
            g2d.setColor(new Color(100, 255, 100, 200)); // Green for finish line
            g2d.setStroke(new BasicStroke(4));
            
            int segments = 32;
            List<Vector3D> ringPoints = new ArrayList<>();
            // Draw circle in X-Z plane at levelEndY
            for (int i = 0; i <= segments; i++) {
                double angle = (i * 2.0 * Math.PI) / segments;
                double x = tubeRadius * Math.cos(angle);
                double z = tubeRadius * Math.sin(angle);
                ringPoints.add(new Vector3D(x, levelEndY, z));
            }
            
            for (int i = 0; i < ringPoints.size() - 1; i++) {
                drawLine(ringPoints.get(i), ringPoints.get(i + 1), camera);
            }
        }
    }

    private void renderObstacle(Obstacle obstacle, Camera camera) {
        List<Vector3D> vertices = obstacle.getVertices();
        List<int[]> edges = obstacle.getEdges();

        // Project all vertices
        List<Vector3D> projected = new ArrayList<>();
        for (Vector3D vertex : vertices) {
            Vector3D proj = camera.projectPoint(vertex, width, height);
            projected.add(proj);
        }

        // Draw edges
        g2d.setColor(obstacle.getColor());
        g2d.setStroke(new BasicStroke(3));
        
        for (int[] edge : edges) {
            Vector3D v1 = projected.get(edge[0]);
            Vector3D v2 = projected.get(edge[1]);
            
            if (v1 != null && v2 != null) {
                // Calculate distance-based alpha for depth effect (Y is forward/depth)
                double avgY = (v1.z + v2.z) / 2.0; // v1.z and v2.z are depth (Y coordinate in projection)
                double alpha = Math.max(0.4, Math.min(1.0, 1.0 - (avgY - 3) / 25.0));
                
                Color edgeColor = new Color(
                    obstacle.getColor().getRed(),
                    obstacle.getColor().getGreen(),
                    obstacle.getColor().getBlue(),
                    (int)(255 * alpha)
                );
                g2d.setColor(edgeColor);
                
                g2d.drawLine(
                    (int)v1.x, (int)v1.y,
                    (int)v2.x, (int)v2.y
                );
            }
        }
    }

    private void drawLine(Vector3D start, Vector3D end, Camera camera) {
        Vector3D projStart = camera.projectPoint(start, width, height);
        Vector3D projEnd = camera.projectPoint(end, width, height);
        
        if (projStart != null && projEnd != null) {
            g2d.drawLine(
                (int)projStart.x, (int)projStart.y,
                (int)projEnd.x, (int)projEnd.y
            );
        }
    }

    private void drawUI(GameState gameState) {
        Camera camera = gameState.getCamera();
        
        // Draw semi-transparent background for UI
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(10, 10, 220, 170);
        
        // Score
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 255, 100));
        g2d.drawString("Score: " + gameState.getScore(), 20, 40);
        
        // Speed
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(Color.WHITE);
        int speed = (int)camera.getForwardSpeed();
        g2d.drawString("Speed: " + speed, 20, 70);
        
        // Time
        int minutes = (int)(gameState.getGameTime() / 60);
        int seconds = (int)(gameState.getGameTime() % 60);
        g2d.drawString(String.format("Time: %02d:%02d", minutes, seconds), 20, 100);
        
        // Fuel bar
        double fuelPercent = camera.getFuel() / camera.getMaxFuel();
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRect(20, 115, 180, 20);
        g2d.setColor(fuelPercent > 0.3 ? new Color(100, 200, 255) : new Color(255, 100, 100));
        g2d.fillRect(20, 115, (int)(180 * fuelPercent), 20);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Fuel", 20, 110);
        
        // Level progress bar
        double progress = Math.min(1.0, Math.max(0.0, gameState.getLevelProgress()));
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRect(20, 155, 180, 15);
        g2d.setColor(new Color(100, 255, 100));
        g2d.fillRect(20, 155, (int)(180 * progress), 15);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Level " + gameState.getCurrentLevel() + " Progress", 20, 150);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString(String.format("%.0f%%", progress * 100), 105, 166);
        
        // Instructions panel
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(width - 260, height - 100, 250, 90);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("A/D: Move Left/Right", width - 250, height - 80);
        g2d.drawString("SPACE: Fly (uses fuel)", width - 250, height - 60);
        g2d.drawString("R: Restart", width - 250, height - 40);
    }

    private void renderMenu(GameState gameState) {
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.setColor(new Color(100, 200, 255));
        
        String title = "TUBE RUNNER 3D";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 2 - 100);
        
        // Instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        String instruction = "Move through the tube and avoid obstacles!";
        int instWidth = g2d.getFontMetrics().stringWidth(instruction);
        g2d.drawString(instruction, (width - instWidth) / 2, height / 2 - 40);
        
        String instruction2 = "WASD or Arrow Keys: Move Left/Right/Up/Down";
        int instWidth2 = g2d.getFontMetrics().stringWidth(instruction2);
        g2d.drawString(instruction2, (width - instWidth2) / 2, height / 2 - 10);
        
        String instruction3 = "You move forward automatically";
        int instWidth3 = g2d.getFontMetrics().stringWidth(instruction3);
        g2d.drawString(instruction3, (width - instWidth3) / 2, height / 2 + 20);
        
        String instruction4 = "Speed increases over time - survive as long as you can!";
        int instWidth4 = g2d.getFontMetrics().stringWidth(instruction4);
        g2d.drawString(instruction4, (width - instWidth4) / 2, height / 2 + 50);
        
        // Best score and level
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(255, 200, 100));
        String bestScoreText = "Best Score: " + gameState.getBestScore();
        int bestScoreWidth = g2d.getFontMetrics().stringWidth(bestScoreText);
        g2d.drawString(bestScoreText, (width - bestScoreWidth) / 2, height / 2 + 90);
        
        String bestLevelText = "Best Level: " + gameState.getBestLevel();
        int bestLevelWidth = g2d.getFontMetrics().stringWidth(bestLevelText);
        g2d.drawString(bestLevelText, (width - bestLevelWidth) / 2, height / 2 + 120);
        
        // Start prompt
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 255, 100));
        String startText = "Press ENTER to Start";
        int startWidth = g2d.getFontMetrics().stringWidth(startText);
        g2d.drawString(startText, (width - startWidth) / 2, height / 2 + 160);
        
        // Animated background cubes
        drawAnimatedBackground();
    }

    private void drawAnimatedBackground() {
        // Draw some rotating cubes in the background for visual appeal
        double time = System.currentTimeMillis() / 1000.0;
        g2d.setColor(new Color(100, 100, 150, 50));
        
        for (int i = 0; i < 5; i++) {
            double angle = time + i * Math.PI * 2 / 5;
            int x = (int)(width / 2 + Math.cos(angle) * 150);
            int y = (int)(height / 2 + Math.sin(angle) * 100);
            int size = 30 + i * 5;
            g2d.drawRect(x - size/2, y - size/2, size, size);
        }
    }

    private void renderGameOver(GameState gameState) {
        // Game Over text
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.setColor(new Color(255, 100, 100));
        
        String gameOver = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int gameOverWidth = fm.stringWidth(gameOver);
        g2d.drawString(gameOver, (width - gameOverWidth) / 2, height / 2 - 100);
        
        // Final score
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(Color.WHITE);
        String scoreText = "Final Score: " + gameState.getScore();
        int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
        g2d.drawString(scoreText, (width - scoreWidth) / 2, height / 2 - 30);
        
        // Level reached
        String levelText = "Level Reached: " + gameState.getCurrentLevel();
        int levelWidth = g2d.getFontMetrics().stringWidth(levelText);
        g2d.drawString(levelText, (width - levelWidth) / 2, height / 2 + 5);
        
        int minutes = (int)(gameState.getGameTime() / 60);
        int seconds = (int)(gameState.getGameTime() % 60);
        String timeText = String.format("Time Survived: %02d:%02d", minutes, seconds);
        int timeWidth = g2d.getFontMetrics().stringWidth(timeText);
        g2d.drawString(timeText, (width - timeWidth) / 2, height / 2 + 40);
        
        // Best score and level
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(255, 200, 100));
        String bestScoreText = "Best Score: " + gameState.getBestScore();
        int bestScoreWidth = g2d.getFontMetrics().stringWidth(bestScoreText);
        g2d.drawString(bestScoreText, (width - bestScoreWidth) / 2, height / 2 + 75);
        
        String bestLevelText = "Best Level: " + gameState.getBestLevel();
        int bestLevelWidth = g2d.getFontMetrics().stringWidth(bestLevelText);
        g2d.drawString(bestLevelText, (width - bestLevelWidth) / 2, height / 2 + 100);
        
        // Restart prompt
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(new Color(255, 255, 100));
        String restartText = "Press ENTER or R to Restart";
        int restartWidth = g2d.getFontMetrics().stringWidth(restartText);
        g2d.drawString(restartText, (width - restartWidth) / 2, height / 2 + 140);
    }
}

