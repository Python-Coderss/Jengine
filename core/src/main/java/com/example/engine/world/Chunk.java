package com.example.engine.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.GL30; // For GL_R32F etc.
import java.nio.FloatBuffer;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.Gdx; // Required for Gdx.gl30 calls

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 16;
    public static final int CHUNK_SIZE_Z = 16;
    public static final float VOXEL_SIZE = 1.0f; // Corrected line
    public static final int FLOATS_PER_INSTANCE = 9;

    private Voxel[][][] voxels;
    private Vector3 position;
    private FloatArray instanceData;
    boolean[][][][] faceMerged;
    private Texture dataTexture;
    private FloatBuffer voxelDataBuffer; // To hold data for the 3D texture

    public Chunk(Vector3 position) {
        this.position = position;
        voxels = new Voxel[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
        instanceData = new FloatArray(false, CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 3 * FLOATS_PER_INSTANCE);
        faceMerged = new boolean[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z][6];
        initializeVoxels();
        generateInstanceData();
        createDataTexture();
    }

    private void initializeVoxels() {
        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                    if (y < CHUNK_SIZE_Y / 2) {
                        voxels[x][y][z] = new Voxel(Voxel.VoxelType.STONE);
                    } else if (y == CHUNK_SIZE_Y / 2 && x % 2 == 0 && z % 2 == 0) {
                         voxels[x][y][z] = new Voxel(Voxel.VoxelType.DIRT);
                    } else {
                        voxels[x][y][z] = new Voxel(Voxel.VoxelType.AIR);
                    }
                }
            }
        }
        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int z = 0; z < CHUNK_SIZE_Z; z++) {
                 if (voxels[x][CHUNK_SIZE_Y / 2 - 1][z] != null && voxels[x][CHUNK_SIZE_Y / 2 - 1][z].type == Voxel.VoxelType.STONE) {
                    voxels[x][CHUNK_SIZE_Y / 2 - 1][z] = new Voxel(Voxel.VoxelType.GRASS);
                }
            }
        }
    }

    private Voxel.VoxelType getVoxelTypeOrAir(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return Voxel.VoxelType.AIR;
        }
        if (voxels[x][y][z] == null) return Voxel.VoxelType.AIR;
        return voxels[x][y][z].type;
    }

    private boolean isFaceProcessed(int x, int y, int z, int faceDirectionIndex) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return true; // Out of bounds voxels are considered processed to prevent quad extension.
        }
        return faceMerged[x][y][z][faceDirectionIndex];
    }

    private void markFaceAsProcessed(int x, int y, int z, int faceDirectionIndex) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return; // Out of bounds, nothing to mark.
        }
        faceMerged[x][y][z][faceDirectionIndex] = true;
    }

    private void generateInstanceData() {
        instanceData.clear();
        for(int i=0; i<CHUNK_SIZE_X; ++i)
            for(int j=0; j<CHUNK_SIZE_Y; ++j)
                for(int k=0; k<CHUNK_SIZE_Z; ++k)
                    for (int l=0; l<6; ++l)
                        faceMerged[i][j][k][l] = false;

        for (int x = 0; x < CHUNK_SIZE_X; x++) {
            for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                for (int z = 0; z < CHUNK_SIZE_Z; z++) {

                    if (getVoxelTypeOrAir(x,y,z) == Voxel.VoxelType.AIR ) {
                        continue;
                    }

                    Voxel.VoxelType currentType = getVoxelTypeOrAir(x,y,z);
                    Color baseColor = getColorForVoxelType(currentType);

                    for (int d = 0; d < 6; d++) {
                        int[] dir = DIRECTIONS[d];
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        int nz = z + dir[2];

                        if (getVoxelTypeOrAir(nx, ny, nz) == Voxel.VoxelType.AIR) {
                            // Check if the base voxel of this potential quad has already been merged.
                            // This is a key part of the greedy meshing optimization.
                            if (isFaceProcessed(x,y,z,d)) {
                                continue;
                            }


                            int quadPrimaryAxisLength = 1;
                            int quadSecondaryAxisLength = 1;
                            int plane = d / 2;

                            // Greedy expansion logic
                            if (plane == 0) { // YZ plane (face along X axis)
                                // Expand along Y (primary)
                                while (y + quadPrimaryAxisLength < CHUNK_SIZE_Y &&
                                       getVoxelTypeOrAir(x, y + quadPrimaryAxisLength, z) == currentType &&
                                       getVoxelTypeOrAir(x + dir[0], y + quadPrimaryAxisLength, z) == Voxel.VoxelType.AIR &&
                                       !isFaceProcessed(x,y+quadPrimaryAxisLength,z,d)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Z (secondary)
                                boolean canExpandSecondary = true;
                                while (z + quadSecondaryAxisLength < CHUNK_SIZE_Z && canExpandSecondary) {
                                    for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                        if (getVoxelTypeOrAir(x, y+k, z+quadSecondaryAxisLength) != currentType ||
                                            getVoxelTypeOrAir(x+dir[0], y+k, z+quadSecondaryAxisLength) != Voxel.VoxelType.AIR ||
                                            isFaceProcessed(x,y+k,z+quadSecondaryAxisLength,d)) {
                                            canExpandSecondary = false; break;
                                        }
                                    }
                                    if (canExpandSecondary) quadSecondaryAxisLength++; else break;
                                }
                            } else if (plane == 1) { // XZ plane (face along Y axis)
                                // Expand along X (primary)
                                while (x + quadPrimaryAxisLength < CHUNK_SIZE_X &&
                                       getVoxelTypeOrAir(x + quadPrimaryAxisLength, y, z) == currentType &&
                                       getVoxelTypeOrAir(x + quadPrimaryAxisLength, y + dir[1], z) == Voxel.VoxelType.AIR &&
                                       !isFaceProcessed(x+quadPrimaryAxisLength,y,z,d)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Z (secondary)
                                boolean canExpandSecondary = true;
                                while (z + quadSecondaryAxisLength < CHUNK_SIZE_Z && canExpandSecondary) {
                                    for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                        if (getVoxelTypeOrAir(x+k, y, z+quadSecondaryAxisLength) != currentType ||
                                            getVoxelTypeOrAir(x+k, y+dir[1], z+quadSecondaryAxisLength) != Voxel.VoxelType.AIR ||
                                            isFaceProcessed(x+k,y,z+quadSecondaryAxisLength,d)) {
                                            canExpandSecondary = false; break;
                                        }
                                    }
                                    if (canExpandSecondary) quadSecondaryAxisLength++; else break;
                                }
                            } else { // XY plane (face along Z axis)
                                // Expand along X (primary)
                                while (x + quadPrimaryAxisLength < CHUNK_SIZE_X &&
                                       getVoxelTypeOrAir(x + quadPrimaryAxisLength, y, z) == currentType &&
                                       getVoxelTypeOrAir(x + quadPrimaryAxisLength, y, z + dir[2]) == Voxel.VoxelType.AIR &&
                                       !isFaceProcessed(x+quadPrimaryAxisLength,y,z,d)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Y (secondary)
                                boolean canExpandSecondary = true;
                                while (y + quadSecondaryAxisLength < CHUNK_SIZE_Y && canExpandSecondary) {
                                     for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                         if (getVoxelTypeOrAir(x+k, y+quadSecondaryAxisLength, z) != currentType ||
                                             getVoxelTypeOrAir(x+k, y+quadSecondaryAxisLength, z+dir[2]) != Voxel.VoxelType.AIR ||
                                             isFaceProcessed(x+k,y+quadSecondaryAxisLength,z,d)) {
                                             canExpandSecondary = false; break;
                                         }
                                     }
                                     if (canExpandSecondary) quadSecondaryAxisLength++; else break;
                                }
                            }

                            // Calculate quad's bottom-left-front corner position in world space
                            float qx = this.position.x + x * VOXEL_SIZE;
                            float qy = this.position.y + y * VOXEL_SIZE;
                            float qz = this.position.z + z * VOXEL_SIZE;

                            // Adjust qx, qy, qz to be the actual corner of the quad depending on face direction 'd'
                            // For example, for a +X face (d=0), the quad is on the x+VOXEL_SIZE plane.
                            // The instance offset should be the corner from which the quad expands.
                            // The shader will then use primary_len and secondary_len to draw it.
                            // The current qx,qy,qz is the base voxel's corner.
                            // For +X face, qx needs to be x+VOXEL_SIZE.
                            // For +Y face, qy needs to be y+VOXEL_SIZE.
                            // For +Z face, qz needs to be z+VOXEL_SIZE.
                            if (dir[0] == 1) qx += VOXEL_SIZE; // If normal is +X, offset quad by VOXEL_SIZE on X
                            // No change for -X normal, qx is already correct (voxel's min x)
                            if (dir[1] == 1) qy += VOXEL_SIZE; // If normal is +Y, offset quad by VOXEL_SIZE on Y
                            // No change for -Y normal
                            if (dir[2] == 1) qz += VOXEL_SIZE; // If normal is +Z, offset quad by VOXEL_SIZE on Z
                            // No change for -Z normal


                            instanceData.add(qx); instanceData.add(qy); instanceData.add(qz);

                            // Store primary and secondary lengths based on face orientation
                            if (plane == 0) { // YZ plane (+X or -X face)
                                instanceData.add(quadPrimaryAxisLength * VOXEL_SIZE);   // Length along Y
                                instanceData.add(quadSecondaryAxisLength * VOXEL_SIZE); // Length along Z
                            } else if (plane == 1) { // XZ plane (+Y or -Y face)
                                instanceData.add(quadPrimaryAxisLength * VOXEL_SIZE);   // Length along X
                                instanceData.add(quadSecondaryAxisLength * VOXEL_SIZE); // Length along Z
                            } else { // XY plane (+Z or -Z face)
                                instanceData.add(quadPrimaryAxisLength * VOXEL_SIZE);   // Length along X
                                instanceData.add(quadSecondaryAxisLength * VOXEL_SIZE); // Length along Y
                            }

                            // instanceData.add(baseColor.r); instanceData.add(baseColor.g); instanceData.add(baseColor.b); // Removed color
                            // Add normalized texture coordinates for the original voxel (x,y,z) that this quad face belongs to
                            instanceData.add(x / (float)CHUNK_SIZE_X);
                            instanceData.add(y / (float)CHUNK_SIZE_Y);
                            instanceData.add(z / (float)CHUNK_SIZE_Z);

                            instanceData.add((float)d); // Normal index (0-5)

                            // Mark all voxels covered by this quad as processed
                            for (int i = 0; i < quadPrimaryAxisLength; i++) {
                                for (int j = 0; j < quadSecondaryAxisLength; j++) {
                                    if (plane == 0) { markFaceAsProcessed(x,y + i,z + j,d); }
                                    else if (plane == 1) { markFaceAsProcessed(x + i,y,z + j,d); }
                                    else { markFaceAsProcessed(x + i,y + j,z,d); }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Chunk at " + this.position + " generated " + getNumInstances() + " instances (quads).");
    }

    public FloatArray getInstanceData() { return instanceData; }
    public int getNumInstances() { return instanceData == null ? 0 : instanceData.size / FLOATS_PER_INSTANCE; }
    private static final int[][] DIRECTIONS = { {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1} };
    private Color getColorForVoxelType(Voxel.VoxelType type) {
        // Ensure colors are distinct enough for testing
        switch (type) {
            case DIRT: return new Color(139/255f, 69/255f, 19/255f, 1); // Brown
            case GRASS: return new Color(34/255f, 139/255f, 34/255f, 1); // Forest Green
            case STONE: return new Color(0.5f, 0.5f, 0.5f, 1); // Gray
            default: return Color.MAGENTA; // Should not happen for solid blocks
        }
    }
    public Vector3 getPosition() { return position; }

    private void createDataTexture() {
        // Prepare FloatBuffer for 3D texture (width, height, depth, 1 component per voxel for type)
        // CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z floats
        voxelDataBuffer = BufferUtils.newFloatBuffer(CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z);

        for (int z = 0; z < CHUNK_SIZE_Z; z++) { // Order: Z slices, then Y rows, then X columns for standard texture layout
            for (int y = 0; y < CHUNK_SIZE_Y; y++) {
                for (int x = 0; x < CHUNK_SIZE_X; x++) {
                    voxelDataBuffer.put(Voxel.getVoxelTypeAsFloat(voxels[x][y][z].type));
                }
            }
        }
        voxelDataBuffer.flip(); // Prepare buffer for reading by GL

        // Clean up old texture if any (e.g. during chunk regeneration)
        if (dataTexture != null) {
            dataTexture.dispose();
        }

        com.badlogic.gdx.graphics.glutils.GLOnlyTextureData texData = new com.badlogic.gdx.graphics.glutils.GLOnlyTextureData(
                CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z, // width, height, depth
                0, // mipmapLevel, set to 0 for base level
                GL30.GL_R32F, // internalFormat (one float per voxel)
                GL30.GL_RED,  // format (data is for red channel)
                GL30.GL_FLOAT // type (data is float)
        );

        dataTexture = new Texture(texData);
        // Now upload the data
        Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_3D, dataTexture.getTextureObjectHandle());
        Gdx.gl30.glTexImage3D(GL30.GL_TEXTURE_3D, 0, GL30.GL_R32F,
                CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z, 0,
                GL30.GL_RED, GL30.GL_FLOAT, voxelDataBuffer);

        // Set texture parameters
        dataTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        dataTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        Gdx.gl30.glBindTexture(GL30.GL_TEXTURE_3D, 0); // Unbind
    }

    public Texture getDataTexture() {
        return dataTexture;
    }

    @Override
    public void dispose() {
        if (dataTexture != null) {
            dataTexture.dispose();
        }
        // instanceData FloatArray is GC'd, no LibGDX disposables here
    }
}
