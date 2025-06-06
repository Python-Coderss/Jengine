package io.github.jengine;

import java.awt.Color;

/**
 * GrassBlock represents a grass block in Minecraft style.
 */
public class GrassBlock extends Block {
    
    private static final Material GRASS_MATERIAL = new Material(new Color(34, 139, 34), 0.0, 0.0, 1.0);
    
    public GrassBlock(Vector3 position) {
        super(position, GRASS_MATERIAL);
    }
    
    @Override
    public String getBlockType() {
        return "grass";
    }
}

