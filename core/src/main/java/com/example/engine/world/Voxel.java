package com.example.engine.world;

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
}
