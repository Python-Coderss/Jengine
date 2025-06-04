package io.github.jengine.world; // Changed package

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;
import io.github.jengine.world.Voxel; // Changed import
import io.github.jengine.world.Chunk; // Changed import

public class World implements Disposable {
    private Map<String, Chunk> chunks;

    public World() {
        chunks = new HashMap<>();
        int worldRadius = 2;
        for(int x = -worldRadius; x <= worldRadius; x++){
            for(int z = -worldRadius; z <= worldRadius; z++){
                createChunk(x, 0, z);
            }
        }
    }

    private String getChunkKey(int x, int y, int z) { return x + "_" + y + "_" + z; }

    public void createChunk(int chunkX, int chunkY, int chunkZ) {
        String key = getChunkKey(chunkX, chunkY, chunkZ);
           if (!chunks.containsKey(key)) {
               Vector3 position = new Vector3(
                   chunkX * Chunk.CHUNK_SIZE_X * Chunk.VOXEL_SIZE,
                   chunkY * Chunk.CHUNK_SIZE_Y * Chunk.VOXEL_SIZE,
                   chunkZ * Chunk.CHUNK_SIZE_Z * Chunk.VOXEL_SIZE
               );
               Chunk chunk = new Chunk(position);
               chunks.put(key, chunk);
           }
    }
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) { return chunks.get(getChunkKey(chunkX, chunkY, chunkZ));}

    public Map<String, Chunk> getChunks() {
        return chunks;
    }

    public Voxel getVoxel(int worldX, int worldY, int worldZ) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE_X);
        int chunkY = Math.floorDiv(worldY, Chunk.CHUNK_SIZE_Y);
        int chunkZ = Math.floorDiv(worldZ, Chunk.CHUNK_SIZE_Z);

        Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            return null; // Or return a static AIR Voxel instance
        }

        int localX = Math.floorMod(worldX, Chunk.CHUNK_SIZE_X);
        int localY = Math.floorMod(worldY, Chunk.CHUNK_SIZE_Y);
        int localZ = Math.floorMod(worldZ, Chunk.CHUNK_SIZE_Z);

        return chunk.getVoxel(localX, localY, localZ);
    }

    public void setVoxel(int worldX, int worldY, int worldZ, Voxel.VoxelType type) {
        int chunkX = Math.floorDiv(worldX, Chunk.CHUNK_SIZE_X);
        int chunkY = Math.floorDiv(worldY, Chunk.CHUNK_SIZE_Y);
        int chunkZ = Math.floorDiv(worldZ, Chunk.CHUNK_SIZE_Z);

        // Ensure chunk exists, create if not.
        // The existing createChunk method in World.java can be used or adapted.
        // It currently takes chunk coordinates.
        String chunkKey = getChunkKey(chunkX, chunkY, chunkZ);
        if (!chunks.containsKey(chunkKey)) {
            createChunk(chunkX, chunkY, chunkZ);
        }

        Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
        // It's possible createChunk failed or returned null, though current implementation doesn't show that.
        // However, a null check is robust.
        if (chunk == null) {
            Gdx.app.error("World", "Failed to get or create chunk at " + chunkX + ", " + chunkY + ", " + chunkZ);
            return;
        }

        int localX = Math.floorMod(worldX, Chunk.CHUNK_SIZE_X);
        int localY = Math.floorMod(worldY, Chunk.CHUNK_SIZE_Y);
        int localZ = Math.floorMod(worldZ, Chunk.CHUNK_SIZE_Z);

        chunk.setVoxel(localX, localY, localZ, type);
    }

    public void clearVoxel(int worldX, int worldY, int worldZ) {
        setVoxel(worldX, worldY, worldZ, Voxel.VoxelType.AIR);
    }

    public void updateDirtyChunks() {
        for (Chunk chunk : chunks.values()) {
            if (chunk != null) { // Good practice to check for null, though map shouldn't store them.
                chunk.rebuildMeshAndDataTextureIfNeeded();
            }
        }
    }

    @Override
    public void dispose() {
        for (Chunk chunk : chunks.values()) {
            if (chunk != null) {
                chunk.dispose();
            }
        }
        chunks.clear();
         if(Gdx.app != null) Gdx.app.log("World", "Disposed all chunks.");
    }
}
