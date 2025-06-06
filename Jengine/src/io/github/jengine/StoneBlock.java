package io.github.jengine;

import java.awt.Color;

/**
 * StoneBlock represents a stone block in Minecraft style.
 */
public class StoneBlock extends Block {
    
    private static final Material STONE_MATERIAL = new Material(new Color(128, 128, 128), 0.0, 0.0, 1.0);
    
    public StoneBlock(Vector3 position) {
        super(position, STONE_MATERIAL);
    }
    
    @Override
    public String getBlockType() {
        return "stone";
    }
}

