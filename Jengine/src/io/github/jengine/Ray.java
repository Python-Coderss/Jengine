package io.github.jengine;

/**
 * Ray represents a light ray in 3D space with an origin and a direction.
 */
public class Ray {
    public final Vector3 origin;
    public final Vector3 direction;

    public Ray(Vector3 origin, Vector3 direction) {
        this.origin = origin;
        this.direction = direction.normalize();
    }
}