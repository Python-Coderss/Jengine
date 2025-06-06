package io.github.jengine;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Renderer performs ray tracing for each pixel in the image.
 * <p>
 * Uses recursive ray tracing:
 * - Traces primary rays from camera
 * - Checks for reflection and refraction using Snell's Law
 * - Combines colors based on material properties
 * Depth-limited recursion avoids infinite bounces.
 */
public class Renderer {
    private final Scene scene;
    private final Camera camera;

    public Renderer(Scene scene, Camera camera) {
        this.scene = scene;
        this.camera = camera;
    }

    public BufferedImage render(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = camera.generateRay(x, y, width, height);
                Color color = trace(ray, 0);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }

    private Color trace(Ray ray, int depth) {
        if (depth > 2) return Color.BLACK;

        Intersection hit = scene.intersect(ray);
        if (hit == null) {
            // No intersection - return skybox color
            return scene.getSkybox().getSkyColor(ray.direction);
        }

        Material m = hit.object.material;
        
        // Calculate lighting contribution
        Color lightColor = calculateLighting(hit, m);
        
        // Reflection
        Color reflectColor = Color.BLACK;
        if (m.reflectivity > 0) {
            Vector3 reflectDir = ray.direction.subtract(hit.normal.multiply(2 * ray.direction.dot(hit.normal))).normalize();
            Ray reflectRay = new Ray(hit.point.add(reflectDir.multiply(0.001)), reflectDir);
            reflectColor = trace(reflectRay, depth + 1);
        }

        // Refraction (Snell's Law)
        Color refractColor = Color.BLACK;
        if (m.refractivity > 0) {
            double n1 = 1.0; // air
            double n2 = m.refractiveIndex;
            Vector3 normal = hit.normal;
            Vector3 dir = ray.direction;
            double cosI = -normal.dot(dir);
            boolean entering = cosI > 0;
            if (!entering) {
                normal = normal.multiply(-1);
                double temp = n1;
                n1 = n2;
                n2 = temp;
                cosI = -normal.dot(dir);
            }

            double eta = n1 / n2;
            double k = 1 - eta * eta * (1 - cosI * cosI);
            if (k >= 0) {
                Vector3 refractDir = dir.multiply(eta).add(normal.multiply(eta * cosI - Math.sqrt(k))).normalize();
                Ray refractRay = new Ray(hit.point.add(refractDir.multiply(0.001)), refractDir);
                refractColor = trace(refractRay, depth + 1);
            }
        }

        // Combine lighting, reflection, and refraction
        double baseWeight = 1 - m.reflectivity - m.refractivity;
        int r = (int)(lightColor.getRed() * baseWeight
                  + reflectColor.getRed() * m.reflectivity
                  + refractColor.getRed() * m.refractivity);
        int g = (int)(lightColor.getGreen() * baseWeight
                  + reflectColor.getGreen() * m.reflectivity
                  + refractColor.getGreen() * m.refractivity);
        int b = (int)(lightColor.getBlue() * baseWeight
                  + reflectColor.getBlue() * m.reflectivity
                  + refractColor.getBlue() * m.refractivity);

        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
    
    /**
     * Calculate lighting contribution from all lights in the scene
     */
    private Color calculateLighting(Intersection hit, Material material) {
        Color totalLight = new Color(20, 20, 20); // Ambient light
        
        for (UnitLight light : scene.getLights()) {
            Color lightContrib = light.calculateLighting(hit.point, hit.normal, material, scene, 4);
            totalLight = addColors(totalLight, lightContrib);
        }
        
        return totalLight;
    }
    
    /**
     * Add two colors together (clamped to 255)
     */
    private Color addColors(Color a, Color b) {
        int r = Math.min(255, a.getRed() + b.getRed());
        int g = Math.min(255, a.getGreen() + b.getGreen());
        int blue = Math.min(255, a.getBlue() + b.getBlue());
        return new Color(r, g, blue);
    }
}