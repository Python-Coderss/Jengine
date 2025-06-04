package com.example.engine.world;

import com.example.engine.entities.Player; // Import the Player class
// No Gdx import needed directly unless for Gdx.app.log or similar, which we might add later.

public class Level {
    private World world;
    private Player player;

    public Level() {
        this.world = new World(); // Assumes World() constructor is suitable (it is, based on current World.java)

        // Sensible default starting position for the player
        // e.g., slightly above the center of a potential initial ground level in a chunk
        float playerStartX = Chunk.CHUNK_SIZE_X / 2.0f;
        float playerStartY = (Chunk.CHUNK_SIZE_Y / 2.0f) + 2.0f; // A bit above the default ground
        float playerStartZ = Chunk.CHUNK_SIZE_Z / 2.0f;
        this.player = new Player(playerStartX, playerStartY, playerStartZ);
    }

    public World getWorld() {
        return world;
    }

    public Player getPlayer() {
        return player;
    }

    public void update(float deltaTime) {
        // Update player (e.g., for future movement input)
        player.update(deltaTime);

        // Update world (e.g., rebuild meshes for chunks that have changed)
        world.updateDirtyChunks();
    }

    // Consider adding a dispose method if Level manages disposable resources directly in the future
    // For now, World and Player (if it had disposables) would be disposed by MyEngine or whoever owns Level.
    // public void dispose() {
    //     world.dispose();
    //     // if (player instanceof com.badlogic.gdx.utils.Disposable) {
    //     // ((com.badlogic.gdx.utils.Disposable)player).dispose();
    //     // }
    // }
}
