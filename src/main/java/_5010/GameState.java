package _5010;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages the game state and entities for tube runner
 */
public class GameState {
    public enum GameMode {
        MENU, PLAYING, GAME_OVER
    }

    private GameMode mode;
    private List<Obstacle> obstacles;
    private List<ChaserObstacle> chasers;
    private List<Double> tunnelRingYPositions; // Y positions of tunnel rings (Y is forward)
    private Camera camera;
    private int score;
    private int currentLevel; // Current level number
    private double gameTime;
    private Random random;
    private double spawnTimer;
    private double cubeSpawnTimer; // Timer for spawning cubes every second
    private double chaserSpawnTimer;
    private double nextObstacleY;
    private double nextRingY; // Next ring Y position to spawn (Y is forward)
    private double levelEndY; // Y position of level end (Y is forward)
    private HighScoreManager highScoreManager;
    private static final double RING_SPACING = 4.0; // Distance between rings
    private static final double CUBE_SPAWN_INTERVAL = 0.7; // Spawn cube every 0.7 seconds (more frequent)
    private static final double CHASER_SPAWN_INTERVAL = 1.3; // Spawn chaser every 1.3 seconds (more frequent)
    private static final double OBSTACLE_SPACING = 25.0; // Distance between obstacles (increased)
    private static final double PLAYER_RADIUS = 0.4;
    private static final double INITIAL_SPAWN_DISTANCE = 50.0; // Start spawning obstacles much further ahead
    private static final double LEVEL_LENGTH = 500.0; // Level ends at 500 units

    public GameState() {
        this.mode = GameMode.MENU;
        this.obstacles = new ArrayList<>();
        this.chasers = new ArrayList<>();
        this.tunnelRingYPositions = new ArrayList<>();
        this.camera = new Camera();
        this.random = new Random();
        this.score = 0;
        this.currentLevel = 1;
        this.gameTime = 0;
        this.spawnTimer = 0;
        this.cubeSpawnTimer = 0;
        this.chaserSpawnTimer = 0;
        this.nextObstacleY = INITIAL_SPAWN_DISTANCE; // Start spawning obstacles ahead
        this.nextRingY = 0.0; // Start rings at player's starting position
        this.levelEndY = LEVEL_LENGTH;
        this.highScoreManager = new HighScoreManager();
    }

    public void startNewGame() {
        obstacles.clear();
        chasers.clear();
        tunnelRingYPositions.clear();
        camera = new Camera();
        score = 0;
        currentLevel = 1;
        gameTime = 0;
        spawnTimer = 0;
        cubeSpawnTimer = 0;
        chaserSpawnTimer = 0;
        nextObstacleY = INITIAL_SPAWN_DISTANCE;
        nextRingY = 0.0;
        levelEndY = LEVEL_LENGTH;
        
        // Initialize some rings ahead
        for (int i = 0; i < 40; i++) {
            tunnelRingYPositions.add(nextRingY);
            nextRingY += RING_SPACING;
        }
    }

    public void startNextLevel() {
        obstacles.clear();
        chasers.clear();
        tunnelRingYPositions.clear();
        camera = new Camera();
        currentLevel++;
        gameTime = 0;
        spawnTimer = 0;
        cubeSpawnTimer = 0;
        chaserSpawnTimer = 0;
        nextObstacleY = INITIAL_SPAWN_DISTANCE;
        nextRingY = 0.0;
        levelEndY = LEVEL_LENGTH * currentLevel; // Each level is longer
        
        // Initialize some rings ahead
        for (int i = 0; i < 40; i++) {
            tunnelRingYPositions.add(nextRingY);
            nextRingY += RING_SPACING;
        }
    }

    public void update(double deltaTime) {
        if (mode != GameMode.PLAYING) return;

        gameTime += deltaTime;
        spawnTimer += deltaTime;
        cubeSpawnTimer += deltaTime;
        chaserSpawnTimer += deltaTime;

        // Check if level is complete (Y is forward)
        if (camera.getPosition().y >= levelEndY) {
            startNextLevel();
            return;
        }

        // Update camera automatic forward movement (gravity, forward movement, etc)
        camera.updateAutomaticMovement(deltaTime);

        // Update tunnel rings - move them backward at same speed as obstacles
        // Coordinate system: Y is forward
        double forwardSpeed = camera.getForwardSpeed();
        double playerY = camera.getPosition().y;
        
        // Update existing rings - move them backward
        for (int i = 0; i < tunnelRingYPositions.size(); i++) {
            double ringY = tunnelRingYPositions.get(i);
            // Move ring backward (same as obstacles)
            double newRingY = ringY - forwardSpeed * deltaTime;
            
            // Remove rings that are far behind
            if (newRingY < playerY - 20) {
                tunnelRingYPositions.remove(i);
                i--; // Adjust index after removal
            } else {
                // Update ring position
                tunnelRingYPositions.set(i, newRingY);
            }
        }
        
        // Spawn new rings ahead - ensure rings go all the way to level end
        double maxY = Math.max(playerY + 60, levelEndY);
        while (nextRingY < maxY) {
            tunnelRingYPositions.add(nextRingY);
            nextRingY += RING_SPACING;
        }

        // Spawn cube every second at player's X and Y position, random Z
        if (cubeSpawnTimer >= CUBE_SPAWN_INTERVAL) {
            spawnCubeAtPlayerPosition();
            // Occasionally spawn an additional obstacle directly in path for extra challenge
            if (random.nextDouble() < 0.3) { // 30% chance
                spawnDirectPathObstacle();
            }
            cubeSpawnTimer = 0;
        }

        // Spawn chaser every 2 seconds
        if (chaserSpawnTimer >= CHASER_SPAWN_INTERVAL) {
            spawnChaser();
            chaserSpawnTimer = 0;
        }

        // Update obstacles (move them backward in Y)
        Iterator<Obstacle> it = obstacles.iterator();
        while (it.hasNext()) {
            Obstacle obstacle = it.next();
            obstacle.update(deltaTime, forwardSpeed);
            
            // Remove obstacles that are far behind (Y is forward)
            if (obstacle.getPosition().y < camera.getPosition().y - 30) {
                it.remove();
            } else if (obstacle.getPosition().y < camera.getPosition().y && !obstacle.isPassed()) {
                // Player passed obstacle
                obstacle.setPassed(true);
                score += 10;
            }
        }

        // Update chasers (they chase player)
        Iterator<ChaserObstacle> chaserIt = chasers.iterator();
        while (chaserIt.hasNext()) {
            ChaserObstacle chaser = chaserIt.next();
            chaser.updateChase(deltaTime, camera.getPosition(), forwardSpeed);
            
            // Remove chasers that are far behind (Y is forward)
            if (chaser.getPosition().y < camera.getPosition().y - 30) {
                chaserIt.remove();
            }
        }

        // Check for collisions
        checkCollisions();
    }

    private void spawnCubeAtPlayerPosition() {
        // Spawn cube at player's X and Y position, near player's Z (vertical)
        Vector3D playerPos = camera.getPosition();
        double tubeRadius = camera.getTubeRadius();
        
        // Spawn Z position close to player's current Z (more challenging)
        // 70% chance to spawn near player height, 30% chance random
        double z;
        if (random.nextDouble() < 0.7) {
            // Spawn close to player's Z position (within 1.5 units)
            double zVariation = (random.nextDouble() - 0.5) * 1.5; // -0.75 to +0.75
            z = playerPos.z + zVariation;
            // Clamp to tube bounds
            z = Math.max(-2.0, Math.min(2.0, z));
        } else {
            // Sometimes spawn at random height for variety
            z = -1.5 + random.nextDouble() * 1.5; // -1.5 to 0
        }
        
        // Use player's X position with some variation (closer to player path)
        double xVariation = (random.nextDouble() - 0.5) * 1.5; // -0.75 to +0.75
        double x = playerPos.x + xVariation;
        double y = playerPos.y + 25.0; // Spawn ahead of player (closer)
        
        Vector3D position = new Vector3D(x, y, z);
        
        // Larger obstacles for more challenge
        double size = 1.0 + random.nextDouble() * 0.8; // 1.0 to 1.8 (was 0.8 to 1.4)
        Color[] colors = {
            new Color(255, 80, 80),   // Red
            new Color(255, 150, 80),  // Orange
            new Color(255, 200, 80),  // Yellow-orange
        };
        Color color = colors[random.nextInt(colors.length)];
        
        obstacles.add(new Obstacle(position, size, color));
    }

    private void spawnRandomObstacle() {
        // Spawn obstacles within tube bounds, but closer to player's typical position
        Vector3D playerPos = camera.getPosition();
        double tubeRadius = camera.getTubeRadius();
        double maxRadius = tubeRadius * 0.6; // Slightly closer to center
        
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = random.nextDouble() * maxRadius;
        
        double x = radius * Math.cos(angle);
        double y = nextObstacleY; // Y is forward
        
        // Spawn Z closer to player's height (more challenging)
        double z;
        if (random.nextDouble() < 0.6) {
            // 60% chance to spawn near player's Z
            double zVariation = (random.nextDouble() - 0.5) * 1.2;
            z = playerPos.z + zVariation;
            z = Math.max(-2.0, Math.min(2.0, z));
        } else {
            // 40% chance random, but biased toward lower half (where player often is)
            z = -1.5 + random.nextDouble() * 1.5; // -1.5 to 0
        }
        
        Vector3D position = new Vector3D(x, y, z);
        
        // Larger obstacles
        double size = 1.0 + random.nextDouble() * 0.8; // 1.0 to 1.8
        Color[] colors = {
            new Color(255, 80, 80),   // Red
            new Color(255, 150, 80),  // Orange
            new Color(255, 200, 80),  // Yellow-orange
        };
        Color color = colors[random.nextInt(colors.length)];
        
        obstacles.add(new Obstacle(position, size, color));
    }

    private void spawnDirectPathObstacle() {
        // Spawn obstacle directly in player's path (same X, same Z, ahead in Y)
        Vector3D playerPos = camera.getPosition();
        
        // Spawn at player's exact X and Z position (direct threat)
        double x = playerPos.x + (random.nextDouble() - 0.5) * 0.5; // Very close to player X
        double y = playerPos.y + 20.0; // Close ahead
        double z = playerPos.z + (random.nextDouble() - 0.5) * 0.8; // Very close to player Z
        
        Vector3D position = new Vector3D(x, y, z);
        
        // Large obstacle for maximum challenge
        double size = 1.2 + random.nextDouble() * 0.6; // 1.2 to 1.8
        Color color = new Color(255, 100, 100); // Bright red for danger
        obstacles.add(new Obstacle(position, size, color));
    }

    private void spawnChaser() {
        // Coordinate system: Y is forward, Z is up
        Vector3D playerPos = camera.getPosition();
        double spawnY = playerPos.y - 12.0; // Closer behind player (was 15.0)
        double angle = random.nextDouble() * Math.PI * 2;
        double tubeRadius = camera.getTubeRadius();
        double x = tubeRadius * Math.cos(angle);
        double y = spawnY;
        
        // Spawn chaser near player's Z height for more threat
        double z;
        if (random.nextDouble() < 0.7) {
            // 70% chance to spawn near player's Z
            double zVariation = (random.nextDouble() - 0.5) * 1.0;
            z = playerPos.z + zVariation;
            z = Math.max(-2.0, Math.min(2.0, z));
        } else {
            z = -1.0 + random.nextDouble() * 1.5; // -1.0 to 0.5
        }
        
        Vector3D position = new Vector3D(x, y, z);
        Color color = new Color(255, 50, 50);
        chasers.add(new ChaserObstacle(position, 0.8, color)); // Larger chaser (was 0.6)
    }

    private void checkCollisions() {
        Vector3D playerPos = camera.getPosition();
        
        // Check collisions with regular obstacles
        for (Obstacle obstacle : obstacles) {
            if (!obstacle.isPassed() && obstacle.collidesWith(playerPos, PLAYER_RADIUS)) {
                // Game over! Record level reached and update high scores
                mode = GameMode.GAME_OVER;
                highScoreManager.updateHighScores(score, currentLevel);
                return;
            }
        }
        
        // Check collisions with chasers
        for (ChaserObstacle chaser : chasers) {
            if (chaser.collidesWith(playerPos, PLAYER_RADIUS)) {
                // Game over! Record level reached and update high scores
                mode = GameMode.GAME_OVER;
                highScoreManager.updateHighScores(score, currentLevel);
                return;
            }
        }
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<ChaserObstacle> getChasers() {
        return chasers;
    }

    public double getLevelEndY() {
        return levelEndY;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public double getLevelProgress() {
        return camera.getPosition().y / levelEndY;
    }

    public List<Double> getTunnelRingYPositions() {
        return tunnelRingYPositions;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getScore() {
        return score;
    }

    public double getGameTime() {
        return gameTime;
    }

    public int getBestScore() {
        return highScoreManager.getBestScore();
    }

    public int getBestLevel() {
        return highScoreManager.getBestLevel();
    }

    /**
     * Check if any obstacle is on a collision course with the player
     * Returns true if an obstacle is projected to hit the player
     */
    public boolean isDangerAhead() {
        // Coordinate system: Y is forward, Z is up
        Vector3D playerPos = camera.getPosition();
        double forwardSpeed = camera.getForwardSpeed();
        
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isPassed()) continue;
            
            Vector3D obsPos = obstacle.getPosition();
            
            // Check if obstacle is ahead of player (Y is forward)
            if (obsPos.y > playerPos.y) {
                // Calculate time until collision if player doesn't move
                double relativeSpeed = forwardSpeed; // Obstacle moves backward at player speed
                double distanceY = obsPos.y - playerPos.y;
                double timeToCollision = distanceY / relativeSpeed;
                
                // If collision would happen soon (within reasonable time)
                if (timeToCollision > 0 && timeToCollision < 2.0) {
                    // Check if obstacle is close to player's X,Z position (horizontal and vertical)
                    double dx = obsPos.x - playerPos.x;
                    double dz = obsPos.z - playerPos.z;
                    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                    double collisionRadius = obstacle.getSize() / 2.0 + PLAYER_RADIUS;
                    
                    // If obstacle is on collision course (use actual collision radius, not 1.5x)
                    // Only warn if actually going to collide
                    if (horizontalDistance < collisionRadius * 1.05) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}

