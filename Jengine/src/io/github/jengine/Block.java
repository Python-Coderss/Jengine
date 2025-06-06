package io.github.jengine;

/**
 * Block represents a unit cube (1x1x1) positioned at integer coordinates.
 * This is the base class for all Minecraft-style blocks.
 */
public abstract class Block extends RectangularPrism {
    
    public Block(Vector3 position, Material material) {
        super(position, new Vector3(1.0, 1.0, 1.0), material);
    }
    
    /**
     * Get the block type name for identification
     */
    public abstract String getBlockType();
    
    /**
     * Whether this block is solid (affects collision and rendering)
     */
    public boolean isSolid() {
        return true;
    }
    
    /**
     * Whether this block emits light
     */
    public boolean isLightEmitting() {
        return false;
    }
    
    /**
     * Get the light emission intensity (0.0 to 1.0)
     */
    public double getLightEmission() {
        return 0.0;
    }
}

