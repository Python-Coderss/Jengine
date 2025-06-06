package io.github.jengine;

import java.util.ArrayList;
import java.util.List;

/**
 * Scene holds all unit objects, lights, skybox and handles optimized ray intersections.
 * Only supports unit-sized objects that fit on integer grid coordinates.
 */
public class Scene {
    private final UnitGrid grid;
    private final List<UnitLight> lights = new ArrayList<>();
    private Skybox skybox;
    
    public Scene() {
        this.grid = new UnitGrid();
        this.skybox = new Skybox(); // Default skybox
    }
    
    /**
     * Add a block to the grid
     */
    public void addBlock(Block block) {
        grid.addBlock(block);
    } 
    
    /**
     * Add a unit sphere to the grid
     */
    public void addUnitSphere(UnitSphere sphere) {
        Vector3 pos = sphere.getGridPosition();
        grid.addObject(sphere, (int) pos.x, (int) pos.y, (int) pos.z);
    }
    
    /**
     * Add a unit light to the scene
     */
    public void addUnitLight(UnitLight light) {
        lights.add(light);
        Vector3 pos = light.getGridPosition();
        grid.addObject(light, (int) pos.x, (int) pos.y, (int) pos.z);
    }
    
    /**
     * Add multiple blocks at once (convenience method for terrain generation)
     */
    public void addBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            addBlock(block);
        }
    }
    
    /**
     * Remove an object from the grid
     */
    public void removeObject(int x, int y, int z) {
        grid.removeObject(x, y, z);
    }
    
    public void setSkybox(Skybox skybox) {
        this.skybox = skybox;
    }
    
    public Skybox getSkybox() {
        return skybox;
    }
    
    public List<UnitLight> getLights() {
        return lights;
    }
    
    /**
     * Optimized ray intersection using spatial grid
     */
    public Intersection intersect(Ray ray) {
        Intersection closest = null;
        
        // Use grid optimization - only check visible objects
        List<Renderable> visibleObjects = grid.getVisibleObjects();
        
        for (Renderable obj : visibleObjects) {
            Intersection hit = obj.intersect(ray);
            if (hit != null && (closest == null || hit.distance < closest.distance)) {
                closest = hit;
            }
        }
        return closest;
    }
    
    /**
     * Get grid statistics for debugging
     */
    public String getGridStats() {
        return grid.getStats();
    }
    
    /**
     * Get objects within a radius (for distance culling)
     */
    public List<Renderable> getObjectsInRadius(Vector3 center, double radius) {
        return grid.getObjectsInRadius(center, radius);
    }
}
