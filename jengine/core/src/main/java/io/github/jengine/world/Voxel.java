package io.github.jengine.world;

public class Voxel {
    public enum VoxelType {
        AIR,
        DIRT,
        GRASS,
        STONE
    }

    public VoxelType type;

    public Voxel(VoxelType type) {
        this.type = type;
    }

    public boolean isSolid() {
        return this.type != VoxelType.AIR;
    }

    public static float getVoxelTypeAsFloat(VoxelType type) {
        switch (type) {
            case AIR: return 0.0f;
            case DIRT: return 1.0f;
            case GRASS: return 2.0f;
            case STONE: return 3.0f;
            default: return 0.0f; // Default to AIR
        }
    }

    public static com.badlogic.gdx.graphics.Color getColorFromTypeFloat(float typeFloat) {
        if (typeFloat == 1.0f) return new com.badlogic.gdx.graphics.Color(139/255f, 69/255f, 19/255f, 1); // DIRT
        if (typeFloat == 2.0f) return new com.badlogic.gdx.graphics.Color(34/255f, 139/255f, 34/255f, 1); // GRASS
        if (typeFloat == 3.0f) return new com.badlogic.gdx.graphics.Color(0.5f, 0.5f, 0.5f, 1);          // STONE
        return com.badlogic.gdx.graphics.Color.BLACK; // AIR or unknown
    }
}
