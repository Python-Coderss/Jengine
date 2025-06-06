package io.github.jengine;

import java.util.ArrayList;
import java.util.List;

/**
 * TerrainGenerator creates Minecraft-style terrain using Perlin noise and Block subclasses.
 */
public class TerrainGenerator {
    private final PerlinNoise noise;
    private final double scale;
    private final double heightMultiplier;
    private final int seaLevel;
    
    public TerrainGenerator(long seed) {
        this.noise = new PerlinNoise(seed);
        this.scale = 0.01; // Controls terrain frequency
        this.heightMultiplier = 50; // Controls terrain height variation
        this.seaLevel = 64; // Base height level
    }
    
    /**
     * Get terrain height at given x,z coordinates
     */
    public double getTerrainHeight(double x, double z) {
        // Use multiple octaves for more natural terrain
        double height = noise.octaveNoise(x * scale, 0, z * scale, 6, 0.5);
        return seaLevel + height * heightMultiplier;
    }
    
    /**
     * Generate terrain blocks in a given area
     */
    public List<Block> generateTerrain(double minX, double maxX, double minZ, double maxZ, int resolution) {
        List<Block> blocks = new ArrayList<>();
        
        for (double x = minX; x <= maxX; x += resolution) {
            for (double z = minZ; z <= maxZ; z += resolution) {
                double height = getTerrainHeight(x, z);
                
                // Generate blocks from bedrock to surface
                for (int y = 0; y <= (int)height; y++) {
                    Block block = createBlock(x, y, z, y, (int)height);
                    if (block != null) {
                        blocks.add(block);
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Create the appropriate block type based on position
     */
    private Block createBlock(double x, int y, double z, int currentY, int surfaceHeight) {
        Vector3 position = new Vector3(x, y, z);
        
        if (currentY == 0) {
            return new BedrockBlock(position);
        } else if (currentY < surfaceHeight - 3) {
            return new StoneBlock(position);
        } else if (currentY < surfaceHeight) {
            return new DirtBlock(position);
        } else if (currentY == surfaceHeight) {
            return new GrassBlock(position);
        }
        
        return null; // Air
    }
    
    /**
     * Generate a simple flat terrain for testing
     */
    public List<Block> generateFlatTerrain(double minX, double maxX, double minZ, double maxZ) {
        List<Block> blocks = new ArrayList<>();
        int resolution = 2;
        
        for (double x = minX; x <= maxX; x += resolution) {
            for (double z = minZ; z <= maxZ; z += resolution) {
                Vector3 position = new Vector3(x, 0, z);
                blocks.add(new GrassBlock(position));
            }
        }
        
        return blocks;
    }
}

