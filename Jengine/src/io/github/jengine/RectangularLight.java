package io.github.jengine;

import java.awt.Color;
import java.util.Random;

/**
 * RectangularLight represents a rectangular prism area light source.
 * Provides soft shadows and more realistic lighting compared to point lights.
 */
public class RectangularLight extends Renderable {
    public final Vector3 position;    // Center of the rectangular light
    public final Vector3 size;        // Width, height, depth of the light
    public final Color color;
    public final double intensity;
    private final Random random;
    
    public RectangularLight(Vector3 position, Vector3 size, Color color, double intensity) {
    	super(new Material(color, 0, 0, 0));
        this.position = position;
        this.size = size;
        this.color = color;
        this.intensity = intensity;
        this.random = new Random();
    }
    
    /**
     * Calculate the light contribution at a given point using area sampling
     */
    public Color calculateLighting(Vector3 point, Vector3 normal, Material material, Scene scene, int samples) {
        double totalR = 0, totalG = 0, totalB = 0;
        int validSamples = 0;
        
        // Sample multiple points on the light surface for soft shadows
        for (int i = 0; i < samples; i++) {
            Vector3 lightPoint = sampleLightSurface();
            Vector3 lightDir = lightPoint.subtract(point).normalize();
            double distance = lightPoint.subtract(point).length();
            
            // Check if the light ray is blocked (shadow test)
            Ray shadowRay = new Ray(point.add(normal.multiply(0.001)), lightDir);
            Intersection shadowHit = scene.intersect(shadowRay);
            
            // If no obstruction or obstruction is farther than light
            if (shadowHit == null || shadowHit.distance > distance) {
                // Calculate attenuation based on distance
                double attenuation = 1.0 / (1.0 + 0.05 * distance + 0.005 * distance * distance);
                
                // Calculate diffuse lighting (Lambert's cosine law)
                double diffuse = Math.max(0, normal.dot(lightDir));
                
                // Calculate area light falloff (closer to center = brighter)
                double areaFalloff = calculateAreaFalloff(lightPoint);
                
                double lightContribution = diffuse * intensity * attenuation * areaFalloff;
                
                totalR += material.color.getRed() * lightContribution * color.getRed() / 255.0;
                totalG += material.color.getGreen() * lightContribution * color.getGreen() / 255.0;
                totalB += material.color.getBlue() * lightContribution * color.getBlue() / 255.0;
                
                validSamples++;
            }
        }
        
        if (validSamples == 0) return Color.BLACK;
        
        // Average the samples
        int r = (int) Math.min(255, totalR / validSamples);
        int g = (int) Math.min(255, totalG / validSamples);
        int b = (int) Math.min(255, totalB / validSamples);
        
        return new Color(r, g, b);
    }
    
    /**
     * Sample a random point on the light's surface
     */
    private Vector3 sampleLightSurface() {
        double x = position.x + (random.nextDouble() - 0.5) * size.x;
        double y = position.y + (random.nextDouble() - 0.5) * size.y;
        double z = position.z + (random.nextDouble() - 0.5) * size.z;
        return new Vector3(x, y, z);
    }
    
    /**
     * Calculate falloff based on distance from light center
     */
    private double calculateAreaFalloff(Vector3 lightPoint) {
        double distFromCenter = lightPoint.subtract(position).length();
        double maxDist = Math.max(size.x, Math.max(size.y, size.z)) * 0.5;
        return Math.max(0.1, 1.0 - (distFromCenter / maxDist));
    }
    
    /**
     * Check if a point is inside the light volume (for emission)
     */
    public boolean containsPoint(Vector3 point) {
        Vector3 diff = point.subtract(position);
        return Math.abs(diff.x) <= size.x * 0.5 &&
               Math.abs(diff.y) <= size.y * 0.5 &&
               Math.abs(diff.z) <= size.z * 0.5;
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

