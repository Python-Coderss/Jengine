package io.github.jengine;

import java.util.*;

/**
 * UnitGrid provides a 3D unit grid for efficient spatial partitioning.
 * Everything is placed on integer coordinates with unit spacing.
 * Supports negative coordinates and optimizes rendering by culling hidden faces.
 */
public class UnitGrid {
    private final Map<GridPosition, Renderable> grid;
    
    public UnitGrid() {
        this.grid = new HashMap<>();
    }
    
    /**
     * Add an object to the grid at integer coordinates
     */
    public void addObject(Renderable object, int x, int y, int z) {
        GridPosition pos = new GridPosition(x, y, z);
        grid.put(pos, object);
    }
    
    /**
     * Add a block to the grid (using its position)
     */
    public void addBlock(Block block) {
        GridPosition pos = worldToGrid(block.position);
        grid.put(pos, block);
    }
    
    /**
     * Remove an object from the grid
     */
    public void removeObject(int x, int y, int z) {
        GridPosition pos = new GridPosition(x, y, z);
        grid.remove(pos);
    }
    
    /**
     * Check if an object exists at the given grid position
     */
    public boolean hasObjectAt(int x, int y, int z) {
        GridPosition pos = new GridPosition(x, y, z);
        return grid.containsKey(pos);
    }
    
    /**
     * Get an object at the given grid position
     */
    public Renderable getObjectAt(int x, int y, int z) {
        GridPosition pos = new GridPosition(x, y, z);
        return grid.get(pos);
    }
    
    /**
     * Get all visible objects (blocks with at least one exposed face, and all non-blocks)
     */
    public List<Renderable> getVisibleObjects() {
        List<Renderable> visibleObjects = new ArrayList<>();
        
        for (Map.Entry<GridPosition, Renderable> entry : grid.entrySet()) {
            Renderable object = entry.getValue();
            
            if (object instanceof Block) {
                Block block = (Block) object;
                if (hasExposedFace(entry.getKey())) {
                    visibleObjects.add(block);
                }
            } else {
                // Non-blocks (spheres, lights) are always visible
                visibleObjects.add(object);
            }
        }
        
        return visibleObjects;
    }
    
    /**
     * Check if a grid position has at least one exposed face
     */
    private boolean hasExposedFace(GridPosition pos) {
        // Check all 6 adjacent positions
        GridPosition[] adjacentPositions = {
            new GridPosition(pos.x + 1, pos.y, pos.z),  // +X
            new GridPosition(pos.x - 1, pos.y, pos.z),  // -X
            new GridPosition(pos.x, pos.y + 1, pos.z),  // +Y
            new GridPosition(pos.x, pos.y - 1, pos.z),  // -Y
            new GridPosition(pos.x, pos.y, pos.z + 1),  // +Z
            new GridPosition(pos.x, pos.y, pos.z - 1)   // -Z
        };
        
        for (GridPosition adjacentPos : adjacentPositions) {
            Renderable adjacent = grid.get(adjacentPos);
            if (adjacent == null || !(adjacent instanceof Block)) {
                return true; // At least one face is exposed (air or non-block)
            }
        }
        
        return false; // All faces are covered by blocks
    }
    
    /**
     * Get objects within a certain distance from a point (for frustum culling)
     */
    public List<Renderable> getObjectsInRadius(Vector3 center, double radius) {
        List<Renderable> nearbyObjects = new ArrayList<>();
        
        int gridRadius = (int) Math.ceil(radius) + 1;
        GridPosition centerGrid = worldToGrid(center);
        
        for (int x = centerGrid.x - gridRadius; x <= centerGrid.x + gridRadius; x++) {
            for (int y = centerGrid.y - gridRadius; y <= centerGrid.y + gridRadius; y++) {
                for (int z = centerGrid.z - gridRadius; z <= centerGrid.z + gridRadius; z++) {
                    GridPosition pos = new GridPosition(x, y, z);
                    Renderable object = grid.get(pos);
                    
                    if (object != null) {
                        Vector3 objectPos = gridToWorld(pos);
                        double distance = center.subtract(objectPos).length();
                        if (distance <= radius) {
                            nearbyObjects.add(object);
                        }
                    }
                }
            }
        }
        
        return nearbyObjects;
    }
    
    /**
     * Convert world coordinates to grid coordinates (rounds to nearest integer)
     */
    private GridPosition worldToGrid(Vector3 worldPos) {
        int x = (int) Math.round(worldPos.x);
        int y = (int) Math.round(worldPos.y);
        int z = (int) Math.round(worldPos.z);
        return new GridPosition(x, y, z);
    }
    
    /**
     * Convert grid coordinates to world coordinates
     */
    private Vector3 gridToWorld(GridPosition gridPos) {
        return new Vector3(gridPos.x, gridPos.y, gridPos.z);
    }
    
    /**
     * Get all objects in the grid
     */
    public Collection<Renderable> getAllObjects() {
        return grid.values();
    }
    
    /**
     * Clear all objects from the grid
     */
    public void clear() {
        grid.clear();
    }
    
    /**
     * Get grid statistics for debugging
     */
    public String getStats() {
        int totalObjects = grid.size();
        int visibleObjects = getVisibleObjects().size();
        return String.format("Grid: %d total, %d visible (%d%% culled)", 
                           totalObjects, visibleObjects, 
                           totalObjects > 0 ? (100 * (totalObjects - visibleObjects) / totalObjects) : 0);
    }
    
    /**
     * Grid position class for efficient hashing and comparison
     */
    private static class GridPosition {
        final int x, y, z;
        
        GridPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GridPosition that = (GridPosition) obj;
            return x == that.x && y == that.y && z == that.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
        
        @Override
        public String toString() {
            return String.format("GridPos(%d, %d, %d)", x, y, z);
        }
    }
}

