package _5010;

import java.awt.Color;

/**
 * Obstacle that chases the player's position
 */
public class ChaserObstacle extends Obstacle {
    private double chaseSpeed;
    private static final double BASE_CHASE_SPEED = 18.0; // Increased chase speed for more difficulty

    public ChaserObstacle(Vector3D startPosition, double size, Color color) {
        super(startPosition, size, color);
        this.chaseSpeed = BASE_CHASE_SPEED;
    }

    @Override
    public void update(double deltaTime, double forwardSpeed) {
        // Coordinate system: Y is forward, Z is up
        // This is called by parent, but we override it for chasing behavior
        Vector3D currentPos = getPosition();

        // Move backward in Y like normal obstacles
        currentPos = new Vector3D(currentPos.x, currentPos.y - forwardSpeed * deltaTime, currentPos.z);

        // Also move toward player's X,Z position (horizontal and vertical, not forward)
        double dx = targetPosition.x - currentPos.x;
        double dz = targetPosition.z - currentPos.z;
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0.1) {
            // Normalize direction and move toward player
            double moveDist = chaseSpeed * deltaTime;
            double moveX = (dx / distance) * moveDist;
            double moveZ = (dz / distance) * moveDist;

            currentPos = new Vector3D(
                    currentPos.x + moveX,
                    currentPos.y,
                    currentPos.z + moveZ);
        }

        setPosition(currentPos);
    }

    private Vector3D targetPosition;

    public void updateChase(double deltaTime, Vector3D targetPosition, double forwardSpeed) {
        this.targetPosition = targetPosition;
        update(deltaTime, forwardSpeed);
    }

}
