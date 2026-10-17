package _5010;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an obstacle in the tube
 */
public class Obstacle {
    private Vector3D position;
    private double size;
    private Color color;
    private boolean passed;

    public Obstacle(Vector3D position, double size, Color color) {
        this.position = position;
        this.size = size;
        this.color = color;
        this.passed = false;
    }

    public void update(double deltaTime, double forwardSpeed) {
        // Coordinate system: Y is forward, Z is up
        // Obstacle moves backward in Y (toward player) as player moves forward
        position = new Vector3D(position.x, position.y - forwardSpeed * deltaTime, position.z);
    }

    public List<Vector3D> getVertices() {
        List<Vector3D> vertices = new ArrayList<>();
        double half = size / 2.0;
        
        // Generate 8 vertices of a cube
        Vector3D[] corners = {
            new Vector3D(-half, -half, -half),
            new Vector3D(half, -half, -half),
            new Vector3D(half, half, -half),
            new Vector3D(-half, half, -half),
            new Vector3D(-half, -half, half),
            new Vector3D(half, -half, half),
            new Vector3D(half, half, half),
            new Vector3D(-half, half, half)
        };
        
        // Add position offset
        for (Vector3D corner : corners) {
            vertices.add(corner.add(position));
        }
        
        return vertices;
    }

    public List<int[]> getEdges() {
        // Define edges of a cube (12 edges connecting 8 vertices)
        return List.of(
            new int[]{0, 1}, new int[]{1, 2}, new int[]{2, 3}, new int[]{3, 0}, // Front face
            new int[]{4, 5}, new int[]{5, 6}, new int[]{6, 7}, new int[]{7, 4}, // Back face
            new int[]{0, 4}, new int[]{1, 5}, new int[]{2, 6}, new int[]{3, 7}  // Connecting edges
        );
    }

    public Vector3D getPosition() {
        return position;
    }

    public double getSize() {
        return size;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean collidesWith(Vector3D playerPos, double playerRadius) {
        double distance = position.distance(playerPos);
        return distance < (size / 2.0 + playerRadius);
    }

    public boolean isBehind(double zThreshold) {
        return position.z < zThreshold;
    }

    public void setPosition(Vector3D newPosition) {
        this.position = newPosition;
    }
}

