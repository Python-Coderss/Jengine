package io.github.jengine;

/**
 * Camera represents the viewer's position and orientation.
 */
public class Camera {
    private Vector3 position = Vector3.of(0, 0, 0);
    private Vector3 direction = Vector3.of(0, 0, -1); // Look direction
    private Vector3 up = Vector3.of(0, 1, 0);         // World up vector
    private double fov = Math.toRadians(60);

    // Cached basis vectors
    private Vector3 forward;
    private Vector3 right;
    private Vector3 trueUp;

    public Camera() {
        updateCameraBasis(); // Initialize basis vectors
    }

    public void updateCameraBasis() {
        forward = direction.normalize();
        right = forward.cross(up).normalize(); 
        trueUp = right.cross(forward).normalize();
    }

    public Ray generateRay(int x, int y, int width, int height) {
        double aspect = (double) width / height;
        double px = (2 * ((x + 0.5) / width) - 1) * Math.tan(fov / 2) * aspect;
        double py = (1 - 2 * ((y + 0.5) / height)) * Math.tan(fov / 2);

        Vector3 rayDir = forward.add(right.multiply(px)).add(trueUp.multiply(py)).normalize();
        return new Ray(position, rayDir);
    }

    // Setters with auto-update of basis
    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public void setDirection(Vector3 direction) {
        this.direction = direction.normalize();
        updateCameraBasis();
    }

    public void setUp(Vector3 up) {
        this.up = up.normalize();
        updateCameraBasis();
    }

    public void setFovDegrees(double fovDegrees) {
        this.fov = Math.toRadians(fovDegrees);
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getDirection() {
        return direction;
    }

    public void moveForward(double distance) {
        position = position.add(forward.multiply(distance));
    }

    public void moveBackward(double distance) {
        position = position.subtract(forward.multiply(distance));
    }

    public void moveLeft(double distance) {
        position = position.subtract(right.multiply(distance));
    }

    public void moveRight(double distance) {
        position = position.add(right.multiply(distance));
    }

    public void rotateHorizontal(double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        Vector3 newDirection = new Vector3(
            direction.x * cos - direction.z * sin,
            direction.y,
            direction.x * sin + direction.z * cos
        );

        setDirection(newDirection);
    }

    public void rotateVertical(double angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        Vector3 newDirection = direction.multiply(cos).add(trueUp.multiply(sin));
        if (Math.abs(newDirection.y) < 0.99) {
            setDirection(newDirection);
        }
    }
}
