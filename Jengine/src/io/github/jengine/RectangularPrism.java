package io.github.jengine;

/**
 * RectangularPrism represents a 3D rectangular box that can be ray traced.
 * Uses optimized ray-box intersection with backface culling.
 */
public class RectangularPrism extends Renderable {
    public final Vector3 position;  // Center of the prism
    public final Vector3 size;      // Width, height, depth
    
    public RectangularPrism(Vector3 position, Vector3 size, Material material) {
        super(material);
        this.position = position;
        this.size = size;
    }
    
    /**
     * Optimized ray-box intersection with backface culling
     */
    public Intersection intersect(Ray ray) {
        Vector3 halfSize = size.multiply(0.5);
        Vector3 min = position.subtract(halfSize);
        Vector3 max = position.add(halfSize);
        
        double tNear = Double.NEGATIVE_INFINITY;
        double tFar = Double.POSITIVE_INFINITY;
        Vector3 hitNormal = null;
        
        // Check each axis (X, Y, Z)
        for (int axis = 0; axis < 3; axis++) {
            double rayDir = getRayComponent(ray.direction, axis);
            double rayOrigin = getRayComponent(ray.origin, axis);
            double boxMin = getRayComponent(min, axis);
            double boxMax = getRayComponent(max, axis);
            
            if (Math.abs(rayDir) < 1e-8) {
                // Ray is parallel to the slab
                if (rayOrigin < boxMin || rayOrigin > boxMax) {
                    return null; // Ray misses the box
                }
            } else {
                double t1 = (boxMin - rayOrigin) / rayDir;
                double t2 = (boxMax - rayOrigin) / rayDir;
                
                Vector3 normal1 = createNormal(axis, rayDir > 0 ? -1 : 1);
                Vector3 normal2 = createNormal(axis, rayDir > 0 ? 1 : -1);
                
                if (t1 > t2) {
                    // Swap t1 and t2
                    double temp = t1;
                    t1 = t2;
                    t2 = temp;
                    
                    // Swap normals
                    Vector3 tempNormal = normal1;
                    normal1 = normal2;
                    normal2 = tempNormal;
                }
                
                // Update tNear (entry point)
                if (t1 > tNear) {
                    tNear = t1;
                    // Only consider front faces (backface culling optimization)
                    if (rayDir * getRayComponent(normal1, axis) < 0) {
                        hitNormal = normal1;
                    }
                }
                
                // Update tFar (exit point)
                if (t2 < tFar) {
                    tFar = t2;
                }
                
                // Early exit if no intersection possible
                if (tNear > tFar || tFar < 0) {
                    return null;
                }
            }
        }
        
        // Choose the appropriate intersection point
        double t = (tNear > 0) ? tNear : tFar;
        if (t < 0 || hitNormal == null) {
            return null; // No valid front-facing intersection
        }
        
        Vector3 hitPoint = ray.origin.add(ray.direction.multiply(t));
        return new Intersection(t, hitPoint, hitNormal, this);
    }
    
    /**
     * Get the component of a vector for a given axis (0=x, 1=y, 2=z)
     */
    private double getRayComponent(Vector3 vector, int axis) {
        switch (axis) {
            case 0: return vector.x;
            case 1: return vector.y;
            case 2: return vector.z;
            default: throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
    
    /**
     * Create a normal vector for a given axis and direction
     */
    private Vector3 createNormal(int axis, int direction) {
        switch (axis) {
            case 0: return new Vector3(direction, 0, 0);
            case 1: return new Vector3(0, direction, 0);
            case 2: return new Vector3(0, 0, direction);
            default: throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
}

