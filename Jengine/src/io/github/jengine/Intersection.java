package io.github.jengine;

/**
 * Intersection stores data about a ray-object hit.
 */
public class Intersection {
    public final Vector3 point;
    public final Vector3 normal;
    public final double distance;
    public final Renderable object;

    public Intersection(double distance, Vector3 point, Vector3 normal, Renderable object) {
        this.distance = distance;
        this.point = point;
        this.normal = normal;
        this.object = object;
    }
}
