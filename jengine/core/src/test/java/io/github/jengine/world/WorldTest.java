package io.github.jengine.world; // Changed package

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

// Import the classes from the new package
import io.github.jengine.world.World;
import io.github.jengine.world.Voxel;
import io.github.jengine.world.Chunk;

// Basic GDX headless setup for tests
// This class definition remains as is, not moved to the new package.
class HeadlessGdxGame implements ApplicationListener {
    @Override public void create() {}
    @Override public void resize(int width, int height) {}
    @Override public void render() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
}

public class WorldTest {

    private static Application application;

    @BeforeAll
    public static void setUpGdx() {
        // Initialize a headless GDX environment.
        application = new HeadlessApplication(new HeadlessGdxGame());
        // Mock Gdx.gl and Gdx.gl30 if necessary for deeper engine parts,
        // For Chunk's createDataTexture, GL30 is used.
        Gdx.gl = Mockito.mock(GL20.class);
        // Mock Gdx.gl30. Important for Chunk.createDataTexture()
        Gdx.gl30 = Mockito.mock(com.badlogic.gdx.graphics.GL30.class);
    }

    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
        Gdx.gl = null;
        Gdx.gl30 = null;
    }

    @Test
    public void testSetAndGetVoxel() {
        World world = new World();
        // Test within the initial chunk area (e.g. 0,0,0 to 15,15,15 for a 16x16x16 chunk)
        // These coordinates should hit one of the default chunks created by World constructor.
        world.setVoxel(1, 1, 1, Voxel.VoxelType.DIRT);
        Voxel voxel = world.getVoxel(1, 1, 1);
        Assertions.assertNotNull(voxel, "Voxel should not be null after setting.");
        Assertions.assertEquals(Voxel.VoxelType.DIRT, voxel.type, "Voxel type should be DIRT.");

        world.setVoxel(1, 1, 1, Voxel.VoxelType.STONE);
        voxel = world.getVoxel(1, 1, 1);
        Assertions.assertNotNull(voxel);
        Assertions.assertEquals(Voxel.VoxelType.STONE, voxel.type, "Voxel type should be STONE after updating.");
    }

    @Test
    public void testClearAndGetVoxel() {
        World world = new World();
        world.setVoxel(2, 2, 2, Voxel.VoxelType.GRASS); // Set something first
        world.clearVoxel(2, 2, 2); // Clear it (sets to AIR)
        Voxel voxel = world.getVoxel(2, 2, 2);
        Assertions.assertNotNull(voxel, "Voxel should not be null after clearing.");
        Assertions.assertEquals(Voxel.VoxelType.AIR, voxel.type, "Voxel type should be AIR after clearing.");
    }

    @Test
    public void testSetAndGetVoxelNegativeCoordinates() {
        World world = new World();
        // This should fall into a new chunk, e.g., chunk (-1, -1, -1)
        world.setVoxel(-5, -5, -5, Voxel.VoxelType.STONE);
        Voxel voxel = world.getVoxel(-5, -5, -5);
        Assertions.assertNotNull(voxel, "Voxel at negative coordinates should not be null after setting.");
        Assertions.assertEquals(Voxel.VoxelType.STONE, voxel.type, "Voxel type at negative coordinates should be STONE.");
    }

    @Test
    public void testGetVoxelInUnloadedChunk() {
        World world = new World();
        // World constructor might create some initial chunks, but let's pick coords far away
        Voxel voxel = world.getVoxel(Chunk.CHUNK_SIZE_X * 100, Chunk.CHUNK_SIZE_Y * 100, Chunk.CHUNK_SIZE_Z * 100);
        // Based on current World.getVoxel, it returns null if chunk doesn't exist.
        Assertions.assertNull(voxel, "Voxel in an unloaded/non-existent chunk should be null.");
    }

    @Test
    public void testSetVoxelCreatesChunk() {
        World world = new World();
        // Calculate chunk coordinates for a voxel far away that wouldn't exist by default
        int farX = Chunk.CHUNK_SIZE_X * 10; // Beyond default worldRadius of 2
        int farY = Chunk.CHUNK_SIZE_Y * 10;
        int farZ = Chunk.CHUNK_SIZE_Z * 10;

        int chunkX = Math.floorDiv(farX, Chunk.CHUNK_SIZE_X);
        int chunkY = Math.floorDiv(farY, Chunk.CHUNK_SIZE_Y);
        int chunkZ = Math.floorDiv(farZ, Chunk.CHUNK_SIZE_Z);

        // Ensure chunk does not exist initially
        Assertions.assertNull(world.getChunk(chunkX, chunkY, chunkZ),
            "Chunk at (" + chunkX + "," + chunkY + "," + chunkZ + ") should not exist before setting voxel far away.");

        // Set a voxel there
        world.setVoxel(farX, farY, farZ, Voxel.VoxelType.DIRT);

        // Check that the chunk now exists
        Chunk newChunk = world.getChunk(chunkX, chunkY, chunkZ);
        Assertions.assertNotNull(newChunk,
            "Chunk at (" + chunkX + "," + chunkY + "," + chunkZ + ") should exist after setting voxel far away.");

        // Also check if the isDirty flag was set on the new chunk (it should be, by setVoxel)
        // This requires Chunk.isDirty to be accessible, or to test its effect (e.g. rebuildMesh is called)
        // For now, we'll assume Chunk.setVoxel correctly sets its internal isDirty flag.
        // We can also check the voxel type itself.
        Voxel voxel = world.getVoxel(farX, farY, farZ);
        Assertions.assertNotNull(voxel, "Voxel in newly created chunk should not be null.");
        Assertions.assertEquals(Voxel.VoxelType.DIRT, voxel.type, "Voxel type in newly created chunk should be DIRT.");
    }
}
