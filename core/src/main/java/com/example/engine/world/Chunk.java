package com.example.engine.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;

public class Chunk implements Disposable {
    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 16;
    public static final int CHUNK_SIZE_Z = 16;
    public static final float VOXEL_SIZE = 1.0f; // Corrected line
    public static final int FLOATS_PER_INSTANCE = 9;

    private Voxel[][][] voxels;
    private Vector3 position;
    private FloatArray instanceData;
    boolean[][][] faceMerged;

    public Chunk(Vector3 position) {
        this.position = position;
        voxels = new Voxel[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
        instanceData = new FloatArray(false, CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z * 3 * FLOATS_PER_INSTANCE);
        faceMerged = new boolean[CHUNK_SIZE_X][CHUNK_SIZE_Y][CHUNK_SIZE_Z];
        initializeVoxels();
        generateInstanceData();
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

    private boolean isVoxelProcessed(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return true;
        }
        return faceMerged[x][y][z];
    }

    private void markVoxelAsProcessed(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE_X || y < 0 || y >= CHUNK_SIZE_Y || z < 0 || z >= CHUNK_SIZE_Z) {
            return;
        }
        faceMerged[x][y][z] = true;
    }

    private void generateInstanceData() {
        instanceData.clear();
        for(int i=0; i<CHUNK_SIZE_X; ++i) for(int j=0; j<CHUNK_SIZE_Y; ++j) for(int k=0; k<CHUNK_SIZE_Z; ++k) faceMerged[i][j][k] = false;

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
                            if (isVoxelProcessed(x,y,z)) { // This check should be more nuanced for faces.
                                                            // A voxel might have one face merged but others not.
                                                            // The current faceMerged flags the whole voxel.
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
                                       !isVoxelProcessed(x,y+quadPrimaryAxisLength,z)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Z (secondary)
                                boolean canExpandSecondary = true;
                                while (z + quadSecondaryAxisLength < CHUNK_SIZE_Z && canExpandSecondary) {
                                    for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                        if (getVoxelTypeOrAir(x, y+k, z+quadSecondaryAxisLength) != currentType ||
                                            getVoxelTypeOrAir(x+dir[0], y+k, z+quadSecondaryAxisLength) != Voxel.VoxelType.AIR ||
                                            isVoxelProcessed(x,y+k,z+quadSecondaryAxisLength)) {
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
                                       !isVoxelProcessed(x+quadPrimaryAxisLength,y,z)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Z (secondary)
                                boolean canExpandSecondary = true;
                                while (z + quadSecondaryAxisLength < CHUNK_SIZE_Z && canExpandSecondary) {
                                    for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                        if (getVoxelTypeOrAir(x+k, y, z+quadSecondaryAxisLength) != currentType ||
                                            getVoxelTypeOrAir(x+k, y+dir[1], z+quadSecondaryAxisLength) != Voxel.VoxelType.AIR ||
                                            isVoxelProcessed(x+k,y,z+quadSecondaryAxisLength)) {
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
                                       !isVoxelProcessed(x+quadPrimaryAxisLength,y,z)) {
                                    quadPrimaryAxisLength++;
                                }
                                // Expand along Y (secondary)
                                boolean canExpandSecondary = true;
                                while (y + quadSecondaryAxisLength < CHUNK_SIZE_Y && canExpandSecondary) {
                                     for (int k = 0; k < quadPrimaryAxisLength; k++) {
                                         if (getVoxelTypeOrAir(x+k, y+quadSecondaryAxisLength, z) != currentType ||
                                             getVoxelTypeOrAir(x+k, y+quadSecondaryAxisLength, z+dir[2]) != Voxel.VoxelType.AIR ||
                                             isVoxelProcessed(x+k,y+quadSecondaryAxisLength,z)) {
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

                            instanceData.add(baseColor.r); instanceData.add(baseColor.g); instanceData.add(baseColor.b);
                            instanceData.add((float)d); // Normal index (0-5)

                            // Mark all voxels covered by this quad as processed
                            for (int i = 0; i < quadPrimaryAxisLength; i++) {
                                for (int j = 0; j < quadSecondaryAxisLength; j++) {
                                    if (plane == 0) { markVoxelAsProcessed(x,y + i,z + j); }
                                    else if (plane == 1) { markVoxelAsProcessed(x + i,y,z + j); }
                                    else { markVoxelAsProcessed(x + i,y + j,z); }
                                }
                            }
                        }
                    }
                }
            }
        }
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
    @Override public void dispose() { /* instanceData FloatArray is GC'd, no LibGDX disposables here */ }
}
