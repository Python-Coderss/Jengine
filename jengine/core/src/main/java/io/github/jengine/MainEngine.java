package io.github.jengine; // New package

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture; // For Chunk's dataTexture
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;

// Imports for the refactored classes
import io.github.jengine.level.Level;
import io.github.jengine.world.Chunk; // Used for Chunk.FLOATS_PER_INSTANCE, Chunk.CHUNK_SIZE constants
import io.github.jengine.entities.Player; // Used for getting player position for camera setup

public class MainEngine extends ApplicationAdapter {
    private PerspectiveCamera camera;
    private CameraInputController cameraController;
    private ShaderProgram instanceShader;
    private Level level; // Use the refactored Level class
    private Mesh unitQuadMesh;
    private VertexBufferObject instanceDataVBO;
    private final int MAX_INSTANCES_PER_DRAW = 30000; // Same as before
    private float[] tempInstanceDataArray = new float[MAX_INSTANCES_PER_DRAW * Chunk.FLOATS_PER_INSTANCE];
    private VertexAttributes instanceAttributes;

    @Override
    public void create() {
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Player and Level will be created first to get initial positions
        level = new Level();
        Player player = level.getPlayer();

        // Set camera position based on player's starting position (adjust as needed)
        camera.position.set(player.getPosition().x + Chunk.CHUNK_SIZE_X * 0.75f,
                            player.getPosition().y + Chunk.CHUNK_SIZE_Y * 0.5f,
                            player.getPosition().z + Chunk.CHUNK_SIZE_Z * 0.75f);
        camera.lookAt(player.getPosition().x, player.getPosition().y, player.getPosition().z);
        camera.near = 0.1f;
        camera.far = 500f; // Increased far plane for potentially larger worlds
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

        // Shader paths are relative to the assets directory (now jengine/core/assets/)
        String vertexShader = Gdx.files.internal("shaders/instance.vert").readString();
        String fragmentShader = Gdx.files.internal("shaders/instance.frag").readString();
        ShaderProgram.pedantic = false;
        instanceShader = new ShaderProgram(vertexShader, fragmentShader);
        if (!instanceShader.isCompiled()) {
            Gdx.app.error("Shader - Instance", "Compilation failed:\n" + instanceShader.getLog());
            Gdx.app.exit();
        } else {
            if(instanceShader.getLog().length() > 0 && !instanceShader.getLog().toLowerCase().contains("no errors"))
                Gdx.app.log("Shader Log - Instance", instanceShader.getLog());
            else
                Gdx.app.log("Shader - Instance", "Compiled successfully.");
        }

        createUnitQuadMesh();

        instanceAttributes = new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Generic, 3, "a_instance_offset", 0),
            new VertexAttribute(VertexAttributes.Usage.Generic, 2, "a_instance_size", 1),
            new VertexAttribute(VertexAttributes.Usage.Generic, 3, "a_voxel_tex_coord", 2),
            new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_instance_normal_idx", 3)
        );
        instanceDataVBO = new VertexBufferObject(true, MAX_INSTANCES_PER_DRAW, instanceAttributes);
    }

    private void createUnitQuadMesh() {
        float[] vertices = { 0,0,0,  1,0,0,  1,1,0,  0,1,0 };
        short[] indices = { 0, 1, 2, 2, 3, 0 };
        unitQuadMesh = new Mesh(Mesh.VertexDataType.VertexBufferObject, true, 4, 6,
                                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        unitQuadMesh.setVertices(vertices);
        unitQuadMesh.setIndices(indices);
    }

    @Override
    public void render() {
        cameraController.update();

        // Update game state
        level.update(Gdx.graphics.getDeltaTime());

        Gdx.gl.glClearColor(0.3f, 0.5f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);

        instanceShader.bind();
        instanceShader.setUniformMatrix("u_projectionViewMatrix", camera.combined);

        unitQuadMesh.bind(instanceShader);
        instanceDataVBO.bind(instanceShader);

        for (Chunk chunk : level.getWorld().getChunks().values()) {
            if (chunk == null || chunk.getNumInstances() == 0) continue;

            Texture dataTexture = chunk.getDataTexture();
            if (dataTexture != null) {
                dataTexture.bind(1); // Bind to texture unit 1
                instanceShader.setUniformi("u_voxelDataTexture", 1);
            }

            int numInstancesInChunk = chunk.getNumInstances();
            int numFloatsToCopy = numInstancesInChunk * Chunk.FLOATS_PER_INSTANCE;

            if (numFloatsToCopy > tempInstanceDataArray.length) {
                 Gdx.app.error("Instancing", "Chunk instance data (" + numInstancesInChunk + " instances) too large for temp array!");
                 continue;
            }

            System.arraycopy(chunk.getInstanceData().items, 0, tempInstanceDataArray, 0, numFloatsToCopy);
            instanceDataVBO.setVertices(tempInstanceDataArray, 0, numFloatsToCopy);

            for (int i = 0; i < instanceAttributes.size(); i++) {
                VertexAttribute attribute = instanceAttributes.get(i);
                final int location = instanceShader.getAttributeLocation(attribute.alias);
                if (location < 0) continue;
                instanceShader.enableVertexAttribute(location);
                instanceShader.setVertexAttribute(location, attribute.numComponents, attribute.type,
                                                 attribute.normalized, instanceAttributes.vertexSize, attribute.offset);
                Gdx.gl30.glVertexAttribDivisor(location, 1);
            }

            Gdx.gl30.glDrawElementsInstanced(GL20.GL_TRIANGLES, unitQuadMesh.getNumIndices(), GL20.GL_UNSIGNED_SHORT,
                                            0, numInstancesInChunk);

            for (int i = 0; i < instanceAttributes.size(); i++) {
                 VertexAttribute attribute = instanceAttributes.get(i);
                 final int location = instanceShader.getAttributeLocation(attribute.alias);
                 if (location < 0) continue;
                 Gdx.gl30.glVertexAttribDivisor(location, 0);
            }
        }
        instanceDataVBO.unbind(instanceShader);
        unitQuadMesh.unbind(instanceShader);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        if (level != null && level.getWorld() != null) {
            level.getWorld().dispose(); // World handles disposing its chunks' resources
        }
        if (instanceShader != null) instanceShader.dispose();
        if (unitQuadMesh != null) unitQuadMesh.dispose();
        if (instanceDataVBO != null) instanceDataVBO.dispose();
        if(Gdx.app != null) Gdx.app.log("MainEngine", "Disposed all resources.");
    }
}
