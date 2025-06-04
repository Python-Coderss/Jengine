package com.example.engine.entities;

import com.badlogic.gdx.math.Vector3;

public class Player {
    private Vector3 position;
    // Potentially add a Camera field later if player manages its own camera
    // private com.badlogic.gdx.graphics.PerspectiveCamera playerCamera;

    public Player(float x, float y, float z) {
        this.position = new Vector3(x, y, z);
        // If we were to use a player-specific camera:
        // this.playerCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // this.playerCamera.position.set(x, y, z);
        // this.playerCamera.lookAt(x, y - 1, z -1 ); // Example lookAt
        // this.playerCamera.near = 0.1f;
        // this.playerCamera.far = 300f;
        // this.playerCamera.update();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        // if (playerCamera != null) {
        //     playerCamera.position.set(x,y,z);
        //     // Update lookAt direction or other camera params if needed
        //     playerCamera.update();
        // }
    }

    public void setPosition(Vector3 newPosition) {
        this.position.set(newPosition);
        // if (playerCamera != null) {
        //     playerCamera.position.set(newPosition);
        //     playerCamera.update();
        // }
    }

    // public com.badlogic.gdx.graphics.PerspectiveCamera getPlayerCamera() {
    //    return playerCamera;
    // }

    public void update(float deltaTime) {
        // Movement logic will go here in the future
        // For example:
        // if (Gdx.input.isKeyPressed(Input.Keys.W)) {
        //     position.add(0, 0, -PLAYER_SPEED * deltaTime);
        // }
        // ...etc.

        // if (playerCamera != null) {
        //     // Ensure camera follows player or is updated based on player's orientation
        //     playerCamera.update();
        // }
    }
}
