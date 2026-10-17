package _5010;

/**
 * Camera class for tube runner - fixed forward view, moves in tube
 */
public class Camera {
    private Vector3D position;
    private double forwardSpeed; // Automatic forward speed
    private double moveSpeed; // Lateral movement speed
    private double fuel; // Fuel for flying
    private double maxFuel; // Maximum fuel capacity
    private double fuelRechargeRate; // Fuel recharge rate when not flying
    private double fuelConsumptionRate; // Fuel consumption when flying
    private double gravity; // Gravity force pulling down
    private double verticalVelocity; // Current vertical velocity
    private boolean isFlying; // Whether player is currently flying
    private static final double TUBE_RADIUS = 3.5; // Radius of the tube
    private static final double BASE_FORWARD_SPEED = 12.0; // Increased base speed
    private static final double LATERAL_SPEED = 8.0;
    private static final double MAX_FUEL = 100.0;
    private static final double FUEL_CONSUMPTION = 30.0; // Per second
    private static final double FUEL_RECHARGE = 20.0; // Per second
    private static final double GRAVITY_FORCE = 30.0; // Increased gravity for faster falling
    private static final double FLYING_FORCE = 50.0; // Increased flying force for faster ascent
    private static final double GROUND_Y = -2.0; // Ground level (bottom of tube)

    public Camera() {
        this.position = new Vector3D(0, 0, 0); // Start at center of tube
        this.forwardSpeed = BASE_FORWARD_SPEED;
        this.moveSpeed = LATERAL_SPEED;
        this.fuel = MAX_FUEL;
        this.maxFuel = MAX_FUEL;
        this.fuelRechargeRate = FUEL_RECHARGE;
        this.fuelConsumptionRate = FUEL_CONSUMPTION;
        this.gravity = GRAVITY_FORCE;
        this.verticalVelocity = 0;
        this.isFlying = false;
    }

    public void moveForward(double deltaTime) {
        // Player moves forward automatically - this is handled in update
        // This method is for increasing speed (optional)
    }

    public void updateAutomaticMovement(double deltaTime) {
        // Coordinate system: X=left/right, Y=forward, Z=up/down
        // Automatically move forward in Y
        position = new Vector3D(position.x, position.y + forwardSpeed * deltaTime, position.z);
        
        // Apply gravity (downward in Z)
        if (!isFlying || fuel <= 0) {
            verticalVelocity -= gravity * deltaTime;
        } else if (fuel > 0) {
            // Flying - apply upward force in Z
            verticalVelocity += FLYING_FORCE * deltaTime;
            // Consume fuel
            fuel -= fuelConsumptionRate * deltaTime;
            if (fuel < 0) fuel = 0;
        }
        
        // Apply vertical velocity in Z
        double newZ = position.z + verticalVelocity * deltaTime;
        
        // Dampen velocity less for snappier controls
        verticalVelocity *= 0.92;
        
        // Check ground collision first
        double groundZ = GROUND_Y; // Ground level in Z
        if (newZ < groundZ) {
            // Hit ground
            newZ = groundZ;
            verticalVelocity = 0;
        }
        
        // Check tube bounds (tube cross-section is in X-Z plane, Y is forward)
        double tubeRadius = TUBE_RADIUS;
        double distanceFromCenter = Math.sqrt(position.x * position.x + newZ * newZ);
        
        // Clamp to tube bounds (tube cross-section is in X-Z plane)
        if (distanceFromCenter > tubeRadius) {
            // Push back inside tube
            double angle = Math.atan2(newZ, position.x);
            double clampedZ = Math.sin(angle) * tubeRadius;
            double clampedX = Math.cos(angle) * tubeRadius;
            newZ = clampedZ;
            position = new Vector3D(clampedX, position.y, clampedZ);
            verticalVelocity *= 0.5; // Dampen on wall collision
        } else {
            // Update position with new Z
            position = new Vector3D(position.x, position.y, newZ);
        }
        
        // Recharge fuel when not flying
        if (!isFlying && fuel < maxFuel) {
            fuel += fuelRechargeRate * deltaTime;
            if (fuel > maxFuel) fuel = maxFuel;
        }
        
        // Increase speed over time (faster acceleration)
        forwardSpeed += deltaTime * 1.0; // Faster speed increase for more challenge
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public double getFuel() {
        return fuel;
    }

    public double getMaxFuel() {
        return maxFuel;
    }

    public boolean canFly() {
        return fuel > 0;
    }

    public void moveLeft(double deltaTime) {
        double moveDistance = moveSpeed * deltaTime;
        double newX = position.x - moveDistance;
        // Clamp to tube bounds (tube cross-section is in X-Z plane, Y is forward)
        if (newX * newX + position.z * position.z <= TUBE_RADIUS * TUBE_RADIUS) {
            position = new Vector3D(newX, position.y, position.z);
        }
    }

    public void moveRight(double deltaTime) {
        double moveDistance = moveSpeed * deltaTime;
        double newX = position.x + moveDistance;
        // Clamp to tube bounds (tube cross-section is in X-Z plane, Y is forward)
        if (newX * newX + position.z * position.z <= TUBE_RADIUS * TUBE_RADIUS) {
            position = new Vector3D(newX, position.y, position.z);
        }
    }

    public void moveUp(double deltaTime) {
        // Up/down movement is now handled by flying/gravity
        // This method is kept for compatibility but does nothing
    }

    public void moveDown(double deltaTime) {
        // Up/down movement is now handled by flying/gravity
        // This method is kept for compatibility but does nothing
    }

    public double getForwardSpeed() {
        return forwardSpeed;
    }

    public double getTubeRadius() {
        return TUBE_RADIUS;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D projectPoint(Vector3D point, int screenWidth, int screenHeight) {
        // Coordinate system: X=left/right, Y=forward, Z=up/down
        // Camera looks forward in +Y direction
        Vector3D relative = point.subtract(position);
        
        // Perspective projection - Y is forward (depth)
        if (relative.y <= 0) {
            // Point is behind camera
            return null;
        }
        
        double fov = 75.0; // Field of view in degrees
        double f = 1.0 / Math.tan(Math.toRadians(fov / 2));
        double aspect = (double) screenWidth / screenHeight;
        
        // Project: X maps to screen X, Z maps to screen Y, Y is depth
        double x3d = relative.x / relative.y * f;
        double z3d = relative.z / relative.y * f;
        
        // Convert to screen coordinates
        double xScreen = (x3d * aspect + 1.0) * screenWidth / 2.0;
        double yScreen = (1.0 - z3d) * screenHeight / 2.0;
        
        return new Vector3D(xScreen, yScreen, relative.y);
    }
}

