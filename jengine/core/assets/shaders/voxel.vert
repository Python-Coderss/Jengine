attribute vec3 a_position;
// REMOVED: attribute vec3 a_normal;
attribute vec4 a_color; // RGBA: RGB for color, A for encoded normal index

uniform mat4 u_projectionViewMatrix;
uniform mat4 u_transform;

varying vec4 v_color;
varying vec3 v_normal_worldspace; // Normal in world space

// Predefined normals for quick lookup based on index
const vec3 normals[6] = vec3[](
    vec3(1.0, 0.0, 0.0),  // Index 0: +X
    vec3(-1.0, 0.0, 0.0), // Index 1: -X
    vec3(0.0, 1.0, 0.0),  // Index 2: +Y
    vec3(0.0, -1.0, 0.0), // Index 3: -Y
    vec3(0.0, 0.0, 1.0),  // Index 4: +Z
    vec3(0.0, 0.0, -1.0)  // Index 5: -Z
);

void main() {
    // Extract normal index from alpha channel (0-1 range) and scale back to 0-5
    // Multiply by 5.0 and round to nearest int to get index 0-5
    float normalIndexFloat = a_color.a * 5.0;
    int normalIndex = int(normalIndexFloat + 0.5); // Add 0.5 for rounding

    vec3 local_normal = normals[normalIndex];

    // Normals in u_transform are relative to the model. If u_transform has non-uniform scaling, this is not quite right.
    // (mat3(u_transform) * local_normal) would be more robust for normals if u_transform can scale non-uniformly.
    // However, our u_transform is currently identity as positions are world.
    // The normals passed to MeshBuilder are already world-space normals.
    // So, we actually want to use the local_normal directly as it represents the world normal of the face.
    v_normal_worldspace = local_normal; // Since u_transform is identity and normals are pre-calculated world normals

    v_color = vec4(a_color.rgb, 1.0); // Pass through RGB, set alpha to 1 for fragment
    gl_Position = u_projectionViewMatrix * u_transform * vec4(a_position, 1.0);
}
