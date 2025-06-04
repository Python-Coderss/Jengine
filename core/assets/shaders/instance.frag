#version 300 es
precision mediump float;
in vec3 v_world_position;
in vec3 v_normal_worldspace;
// in vec3 v_color; // Removed
in float v_voxel_type; // Added
out vec4 FragColor;

// Add this function above main()
vec3 getVoxelColor(float typeFloat) {
    // Use a small epsilon for float comparisons
    if (abs(typeFloat - 1.0) < 0.01) return vec3(139.0/255.0, 69.0/255.0, 19.0/255.0); // DIRT
    if (abs(typeFloat - 2.0) < 0.01) return vec3(34.0/255.0, 139.0/255.0, 34.0/255.0);  // GRASS
    if (abs(typeFloat - 3.0) < 0.01) return vec3(0.5, 0.5, 0.5);                       // STONE
    // For type 0.0 (AIR) or others, could return black or a debug color.
    // Since AIR quads shouldn't be generated, this is mostly for unexpected values.
    return vec3(1.0, 0.0, 1.0); // Magenta for AIR/Unknown, for debugging
}

void main() {
    vec3 baseColor = getVoxelColor(v_voxel_type);
    vec3 lightDirection = normalize(vec3(0.5, 0.8, 0.2));
    float ambient = 0.3;
    float diffuse = max(0.0, dot(normalize(v_normal_worldspace), lightDirection));
    FragColor = vec4(baseColor * (ambient + diffuse), 1.0);
}
