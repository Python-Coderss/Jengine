package io.github.jengine;

import java.awt.Color;

/**
 * BedrockBlock represents bedrock in Minecraft style.
 */
public class BedrockBlock extends Block {
    
    private static final Material BEDROCK_MATERIAL = new Material(new Color(50, 50, 50), 0.1, 0.0, 1.0);
    
    public BedrockBlock(Vector3 position) {
        super(position, BEDROCK_MATERIAL);
    }
    
    @Override
    public String getBlockType() {
        return "bedrock";
    }
}

