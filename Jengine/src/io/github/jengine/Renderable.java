package io.github.jengine;

/**
 * Renderable is the abstract base class for all objects that can be ray traced.
 * Provides a common interface for ray-object intersection testing.
 */
public abstract class Renderable {
    public final Material material;
    
    public Renderable(Material material) {
        this.material = material;
    }
    
    /**
     * Test for ray intersection with this object.
     * Returns null if no intersection, otherwise returns intersection data.
     */
    public abstract Intersection intersect(Ray ray);
}

