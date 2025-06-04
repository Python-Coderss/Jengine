package com.example.engine.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

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
