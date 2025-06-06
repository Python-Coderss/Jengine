package io.github.jengine;

import java.awt.Color;

/**
 * DirtBlock represents a dirt block in Minecraft style.
 */
public class DirtBlock extends Block {
    
    private static final Material DIRT_MATERIAL = new Material(new Color(139, 69, 19), 0.0, 0.0, 1.0);
    
    public DirtBlock(Vector3 position) {
        super(position, DIRT_MATERIAL);
    }
    
    @Override
    public String getBlockType() {
        return "dirt";
    }
}

