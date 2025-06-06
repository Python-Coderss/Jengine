package io.github.jengine;

import java.awt.Color;

/**
 * Skybox provides a procedural sky gradient similar to Minecraft.
 */
public class Skybox {
    private final Color horizonColor;
    private final Color zenithColor;
    private final Color sunColor;
    private final Vector3 sunDirection;
    
    public Skybox() {
        // Minecraft-like sky colors
        this.horizonColor = new Color(135, 206, 235); // Sky blue
        this.zenithColor = new Color(30, 144, 255);   // Deeper blue
        this.sunColor = new Color(255, 255, 200);     // Warm white
        this.sunDirection = new Vector3(0.3, 0.8, 0.5).normalize(); // Sun position
    }
    
    public Skybox(Color horizonColor, Color zenithColor, Color sunColor, Vector3 sunDirection) {
        this.horizonColor = horizonColor;
        this.zenithColor = zenithColor;
        this.sunColor = sunColor;
        this.sunDirection = sunDirection.normalize();
    }
    
    /**
     * Get the sky color for a given ray direction
     */
    public Color getSkyColor(Vector3 direction) {
        direction = direction.normalize();
        
        // Calculate the vertical gradient (horizon to zenith)
        double height = Math.max(0, direction.y); // 0 at horizon, 1 at zenith
        
        // Interpolate between horizon and zenith colors
        Color baseColor = interpolateColor(horizonColor, zenithColor, height);
        
        // Add sun glow effect
        double sunDot = Math.max(0, direction.dot(sunDirection));
        double sunIntensity = Math.pow(sunDot, 16); // Sharp sun disc
        double sunGlow = Math.pow(sunDot, 4) * 0.3; // Soft glow around sun
        
        // Blend sun color with base sky color
        if (sunIntensity > 0.1 || sunGlow > 0) {
            Color sunContrib = multiplyColor(sunColor, sunIntensity + sunGlow);
            baseColor = addColors(baseColor, sunContrib);
        }
        
        return baseColor;
    }
    
    /**
     * Linear interpolation between two colors
     */
    private Color interpolateColor(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t)); // Clamp t to [0,1]
        
        int r = (int) (a.getRed() * (1 - t) + b.getRed() * t);
        int g = (int) (a.getGreen() * (1 - t) + b.getGreen() * t);
        int blue = (int) (a.getBlue() * (1 - t) + b.getBlue() * t);
        
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, blue));
    }
    
    /**
     * Multiply a color by a scalar
     */
    private Color multiplyColor(Color color, double multiplier) {
        int r = (int) Math.min(255, color.getRed() * multiplier);
        int g = (int) Math.min(255, color.getGreen() * multiplier);
        int b = (int) Math.min(255, color.getBlue() * multiplier);
        return new Color(r, g, b);
    }
    
    /**
     * Add two colors together (clamped)
     */
    private Color addColors(Color a, Color b) {
        int r = Math.min(255, a.getRed() + b.getRed());
        int g = Math.min(255, a.getGreen() + b.getGreen());
        int blue = Math.min(255, a.getBlue() + b.getBlue());
        return new Color(r, g, blue);
    }
}

