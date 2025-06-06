package io.github.jengine;

/**
 * UnitSphere represents a sphere with radius 0.5 centered on integer coordinates.
 * Designed to fit perfectly within unit grid cells.
 */
public class UnitSphere extends Sphere {
    
    private static final double UNIT_RADIUS = 0.5;
    
    public UnitSphere(int x, int y, int z, Material material) {
        super(new Vector3(x, y, z), UNIT_RADIUS, material);
    }
    
    public UnitSphere(Vector3 position, Material material) {
        super(position, UNIT_RADIUS, material);
    }
    
    /**
     * Get the grid position of this sphere
     */
    public Vector3 getGridPosition() {
        return new Vector3(
            Math.round(center.x),
            Math.round(center.y), 
            Math.round(center.z)
        );
    }
}

