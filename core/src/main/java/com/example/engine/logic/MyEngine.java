package com.example.engine.logic;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.example.engine.world.Chunk;
import com.example.engine.world.World;

public class MyEngine extends ApplicationAdapter {
    private PerspectiveCamera camera;
    private CameraInputController cameraController;
    private ShaderProgram instanceShader;
    private World world;
    private Mesh unitQuadMesh;
    private VertexBufferObject instanceDataVBO;
    private final int MAX_INSTANCES_PER_DRAW = 30000;
    private float[] tempInstanceDataArray = new float[MAX_INSTANCES_PER_DRAW * Chunk.FLOATS_PER_INSTANCE];
    private VertexAttributes instanceAttributes;

    @Override
    public void create() {
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Chunk.CHUNK_SIZE_X * 1.5f, Chunk.CHUNK_SIZE_Y * 2.5f, Chunk.CHUNK_SIZE_Z * 1.5f);
        camera.lookAt(Chunk.CHUNK_SIZE_X, Chunk.CHUNK_SIZE_Y / 2f, Chunk.CHUNK_SIZE_Z);
        camera.near = 0.1f;
        camera.far = 500f;
        camera.update();

        cameraController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraController);

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
            new VertexAttribute(VertexAttributes.Usage.Generic, 3, "a_voxel_tex_coord", 2), // Index 2 for attribute binding location
            new VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_instance_normal_idx", 3)
        );
        // Ensure VBO uses GL_STATIC_DRAW if data changes infrequently per frame, or GL_DYNAMIC_DRAW if it changes often.
        // For instance data that is rebuilt per frame or often, GL_DYNAMIC_DRAW is appropriate.
        instanceDataVBO = new VertexBufferObject(true, MAX_INSTANCES_PER_DRAW, instanceAttributes); // true for dynamic VBO

        world = new World();
    }

    private void createUnitQuadMesh() {
        // A simple 1x1 quad. The instance shader will scale and orient it.
        // Vertices are for a quad on XY plane, extending from (0,0,0) to (1,1,0)
        // This will be transformed by the shader based on normal_idx to face the correct direction.
        float[] vertices = {
            0,0,0,  // Bottom-left
            1,0,0,  // Bottom-right
            1,1,0,  // Top-right
            0,1,0   // Top-left
        };
        short[] indices = { 0, 1, 2, 2, 3, 0 }; // Standard quad indices (2 triangles)
        unitQuadMesh = new Mesh(Mesh.VertexDataType.VertexBufferObject, true, 4, 6,
                                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        unitQuadMesh.setVertices(vertices);
        unitQuadMesh.setIndices(indices);
    }

    @Override
    public void render() {
        cameraController.update();
        Gdx.gl.glClearColor(0.3f, 0.5f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK); // Cull back faces, assuming CCW winding for front faces.

        instanceShader.bind();
        instanceShader.setUniformMatrix("u_projectionViewMatrix", camera.combined);

        unitQuadMesh.bind(instanceShader);
        instanceDataVBO.bind(instanceShader);

        for (com.example.engine.world.Chunk chunk : world.getChunks().values()) {
            if (chunk.getNumInstances() == 0) continue;

            // Get and bind the chunk's data texture
            com.badlogic.gdx.graphics.Texture dataTexture = chunk.getDataTexture(); // Corrected import for Texture
            if (dataTexture != null) { // Should always exist if chunk is valid
                dataTexture.bind(1); // Bind to texture unit 1
                instanceShader.setUniformi("u_voxelDataTexture", 1); // Tell sampler u_voxelDataTexture to use texture unit 1
            }

            int numInstancesInChunk = chunk.getNumInstances();
            int numFloatsToCopy = numInstancesInChunk * Chunk.FLOATS_PER_INSTANCE;

            if (numFloatsToCopy > tempInstanceDataArray.length) {
                 Gdx.app.error("Instancing", "Chunk instance data (" + numInstancesInChunk + " instances) too large for temp array! Floats: "
                                + numFloatsToCopy + ", Max Floats in Temp: " + tempInstanceDataArray.length +
                                " (Configured Max Instances for VBO: " + MAX_INSTANCES_PER_DRAW +")");
                 continue;
            }

            // Copy data from chunk's FloatArray to the temp array
            System.arraycopy(chunk.getInstanceData().items, 0, tempInstanceDataArray, 0, numFloatsToCopy);
            // Set data into the VBO
            instanceDataVBO.setVertices(tempInstanceDataArray, 0, numFloatsToCopy);

            // Enable and set up instance attributes
            for (int i = 0; i < instanceAttributes.size(); i++) {
                VertexAttribute attribute = instanceAttributes.get(i);
                final int location = instanceShader.getAttributeLocation(attribute.alias);
                if (location < 0) continue;
                instanceShader.enableVertexAttribute(location);
                // numComponents, type, normalized, stride, offset
                instanceShader.setVertexAttribute(location, attribute.numComponents, attribute.type,
                                                 attribute.normalized, instanceAttributes.vertexSize, attribute.offset);
                Gdx.gl30.glVertexAttribDivisor(location, 1); // Advance per instance
            }

            // Draw instanced
            Gdx.gl30.glDrawElementsInstanced(GL20.GL_TRIANGLES, unitQuadMesh.getNumIndices(), GL20.GL_UNSIGNED_SHORT,
                                            0, // offset to indices
                                            numInstancesInChunk);

            // Disable instance attributes / reset divisor
            for (int i = 0; i < instanceAttributes.size(); i++) {
                 VertexAttribute attribute = instanceAttributes.get(i);
                 final int location = instanceShader.getAttributeLocation(attribute.alias);
                 if (location < 0) continue;
                 Gdx.gl30.glVertexAttribDivisor(location, 0); // Reset divisor
                 // instanceShader.disableVertexAttribute(location); // Optional: disable after drawing this set
            }
        }
        instanceDataVBO.unbind(instanceShader);
        unitQuadMesh.unbind(instanceShader);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width; camera.viewportHeight = height; camera.update();
    }
    @Override
    public void dispose() {
        if (instanceShader != null) instanceShader.dispose();
        if (unitQuadMesh != null) unitQuadMesh.dispose();
        if (instanceDataVBO != null) instanceDataVBO.dispose();
        if (world != null) world.dispose();
         if(Gdx.app != null) Gdx.app.log("MyEngine", "Disposed all resources.");
    }
}
