package _5010;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D cube in the game
 */
public class Cube3D {
    private Vector3D position;
    private double size;
    private Color color;
    private double rotation;
    private double rotationSpeed;
    private boolean collected;
    private double floatOffset;
    private double floatSpeed;
    private double baseY; // Store original Y position for floating animation

    public Cube3D(Vector3D position, double size, Color color) {
        this.position = position;
        this.baseY = position.y;
        this.size = size;
        this.color = color;
        this.rotation = Math.random() * Math.PI * 2;
        this.rotationSpeed = (Math.random() - 0.5) * 2.0;
        this.collected = false;
        this.floatOffset = Math.random() * Math.PI * 2;
        this.floatSpeed = 0.5 + Math.random() * 0.5;
    }

    public void update(double deltaTime) {
        if (collected) return;
        
        rotation += rotationSpeed * deltaTime;
        
        // Floating animation
        floatOffset += floatSpeed * deltaTime;
        double currentY = baseY + Math.sin(floatOffset) * 0.3;
        position = new Vector3D(position.x, currentY, position.z);
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
        
        // Rotate around Y axis
        for (Vector3D corner : corners) {
            Vector3D rotated = corner.rotateY(rotation);
            vertices.add(rotated.add(position));
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

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public double distanceTo(Vector3D point) {
        return position.distance(point);
    }
}

