package io.github.jengine;

import java.awt.Color;

/**
 * UnitLight represents a 1x1x1 rectangular light source on integer coordinates.
 * Designed to fit perfectly within unit grid cells.
 */
public class UnitLight extends RectangularLight {
    
    private static final Vector3 UNIT_SIZE = new Vector3(1.0, 1.0, 1.0);
    
    public UnitLight(int x, int y, int z, Color color, double intensity) {
        super(new Vector3(x, y, z), UNIT_SIZE, color, intensity);
    }
    
    public UnitLight(Vector3 position, Color color, double intensity) {
        super(position, UNIT_SIZE, color, intensity);
    }
    
    /**
     * Get the grid position of this light
     */
    public Vector3 getGridPosition() {
        return new Vector3(
            Math.round(position.x),
            Math.round(position.y), 
            Math.round(position.z)
        );
    }
}

