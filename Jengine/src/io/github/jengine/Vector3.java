package io.github.jengine;

import java.util.HashMap;
import java.util.Map;

/**
 * Vector3 represents a 3D vector and provides operations for vector math.
 */
public class Vector3 {
    public final double x, y, z;
    // Cache for all combinations of x,y,z in {-1,0,1}
    private static final Map<Integer, Vector3> CANONICAL = new HashMap<>();

    static {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Vector3 v = new Vector3(dx, dy, dz);
                    CANONICAL.put(hash(dx, dy, dz), v);
                }
            }
        }
    }
    public Vector3(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public Vector3 add(Vector3 o) {
        return new Vector3(x + o.x, y + o.y, z + o.z);
    }

    public Vector3 subtract(Vector3 o) {
        return new Vector3(x - o.x, y - o.y, z - o.z);
    } 

    public Vector3 multiply(double s) {
        return new Vector3(x * s, y * s, z * s);
    }

    public double dot(Vector3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vector3 cross(Vector3 o) {
        return new Vector3(
            y * o.z - z * o.y,
            z * o.x - x * o.z,
            x * o.y - y * o.x
        );
    }

    public Vector3 normalize() {
        double len = Math.sqrt(x * x + y * y + z * z);
        return new Vector3(x / len, y / len, z / len);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    // Factory method: returns cached vector if components are in {-1,0,1}
    public static Vector3 of(int x, int y, int z) {
        if ((x == -1 || x == 1 || x == 0) && (y == -1 || y == 1 || y == 0) && (z == 0 || z >= -1 || z == 1)) {
            return CANONICAL.get(hash(x, y, z));
        }
        return new Vector3(x, y, z);
    }
    // Hash function for indexing the cache map
    private static int hash(int x, int y, int z) {
        return (x + 1) * 9 + (y + 1) * 3 + (z + 1);
    }
}