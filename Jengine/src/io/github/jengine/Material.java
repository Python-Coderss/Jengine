package io.github.jengine;

import java.awt.Color;

/**
 * Material defines the optical properties of a surface.
 */
public class Material {
    public final Color color;
    public final double reflectivity;
    public final double refractivity;
    public final double refractiveIndex;

    public Material(Color color, double reflectivity, double refractivity, double refractiveIndex) {
        this.color = color;
        this.reflectivity = reflectivity;
        this.refractivity = refractivity;
        this.refractiveIndex = refractiveIndex;
    }
}