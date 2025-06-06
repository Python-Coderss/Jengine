package io.github.jengine;

import java.util.Random;

/**
 * PerlinNoise implementation for generating natural-looking terrain heightmaps.
 * Based on Ken Perlin's improved noise function.
 */
public class PerlinNoise {
    private final int[] permutation;
    private final int[] p;
    
    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        permutation = new int[256];
        
        // Initialize permutation array
        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }
        
        // Shuffle the permutation array
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        
        // Duplicate the permutation array
        p = new int[512];
        for (int i = 0; i < 512; i++) {
            p[i] = permutation[i % 256];
        }
    }
    
    public double noise(double x, double y, double z) {
        // Find unit cube that contains point
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        
        // Find relative x,y,z of point in cube
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        
        // Compute fade curves for each of x,y,z
        double u = fade(x);
        double v = fade(y);
        double w = fade(z);
        
        // Hash coordinates of 8 cube corners
        int A = p[X] + Y;
        int AA = p[A] + Z;
        int AB = p[A + 1] + Z;
        int B = p[X + 1] + Y;
        int BA = p[B] + Z;
        int BB = p[B + 1] + Z;
        
        // Add blended results from 8 corners of cube
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),
                                      grad(p[BA], x - 1, y, z)),
                              lerp(u, grad(p[AB], x, y - 1, z),
                                      grad(p[BB], x - 1, y - 1, z))),
                      lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),
                                      grad(p[BA + 1], x - 1, y, z - 1)),
                              lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
                                      grad(p[BB + 1], x - 1, y - 1, z - 1))));
    }
    
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
    
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    /**
     * Generate layered noise for more natural terrain
     */
    public double octaveNoise(double x, double y, double z, int octaves, double persistence) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;
        
        for (int i = 0; i < octaves; i++) {
            total += noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2;
        }
        
        return total / maxValue;
    }
}

