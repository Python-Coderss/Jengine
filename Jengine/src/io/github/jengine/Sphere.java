package io.github.jengine;

/**
 * Sphere represents a 3D sphere and provides ray intersection logic.
 */
public class Sphere extends Renderable {
    public final Vector3 center;
    public final double radius;

    public Sphere(Vector3 center, double radius, Material material) {
        super(material);
        this.center = center;
        this.radius = radius;
    }

    public Intersection intersect(Ray ray) {
        Vector3 oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return null;

        double t = (-b - Math.sqrt(discriminant)) / (2.0 * a);
        if (t < 0) t = (-b + Math.sqrt(discriminant)) / (2.0 * a);
        if (t < 0) return null;

        Vector3 point = ray.origin.add(ray.direction.multiply(t));
        Vector3 normal = point.subtract(center).normalize();
        return new Intersection(t, point, normal, this);
    }
}