#version 300 es
precision mediump float;

in vec3 a_position;

in vec3 a_instance_offset;
in vec2 a_instance_size;
// in vec3 a_instance_color; // Removed
in vec3 a_voxel_tex_coord; // Added
in float a_instance_normal_idx;

uniform mat4 u_projectionViewMatrix;
uniform sampler3D u_voxelDataTexture; // Added

out vec3 v_world_position;
out vec3 v_normal_worldspace;
// out vec3 v_color; // Removed
out float v_voxel_type; // Added

const vec3 normals[6] = vec3[](
    vec3(1.0,0.0,0.0), vec3(-1.0,0.0,0.0), vec3(0.0,1.0,0.0),
    vec3(0.0,-1.0,0.0), vec3(0.0,0.0,1.0), vec3(0.0,0.0,-1.0)
);

// Quad vertices for XY plane (0,0 to 1,1)
const vec2 quad_verts[4] = vec2[](
    vec2(0.0, 0.0),
    vec2(1.0, 0.0),
    vec2(1.0, 1.0),
    vec2(0.0, 1.0)
);

void main() {
    // v_color = a_instance_color; // Removed
    int normalIdx = int(a_instance_normal_idx + 0.01); // Add small epsilon for float to int conversion
    if (normalIdx < 0) normalIdx = 0; if (normalIdx > 5) normalIdx = 5; // Clamp index
    v_normal_worldspace = normals[normalIdx];
    vec3 normalized_tex_coord = a_voxel_tex_coord; // Assuming these are already 0-1
    v_voxel_type = texture(u_voxelDataTexture, normalized_tex_coord).r;

    vec3 quad_unit_vertex = a_position; // This is (0,0,0), (1,0,0), (1,1,0), or (0,1,0)
    vec3 transformed_pos_on_plane;
    float primary_len = a_instance_size.x;   // Length along the primary axis of the quad on its plane
    float secondary_len = a_instance_size.y; // Length along the secondary axis of the quad on its plane

    // Determine the orientation of the quad based on the normal index
    // The unit quad is on the XY plane (a_position.xy are its coords, a_position.z is 0)
    if (normalIdx == 0) {        // +X face: quad on YZ plane, primary=Y, secondary=Z
        transformed_pos_on_plane = vec3(0.0, quad_unit_vertex.y * primary_len, quad_unit_vertex.x * secondary_len);
    } else if (normalIdx == 1) { // -X face: quad on YZ plane, primary=Y, secondary=Z (flip secondary for CCW)
        transformed_pos_on_plane = vec3(0.0, quad_unit_vertex.y * primary_len, (1.0-quad_unit_vertex.x) * secondary_len);
    } else if (normalIdx == 2) { // +Y face: quad on XZ plane, primary=X, secondary=Z
        transformed_pos_on_plane = vec3(quad_unit_vertex.x * primary_len, 0.0, quad_unit_vertex.y * secondary_len);
    } else if (normalIdx == 3) { // -Y face: quad on XZ plane, primary=X, secondary=Z (flip secondary for CCW)
        transformed_pos_on_plane = vec3(quad_unit_vertex.x * primary_len, 0.0, (1.0-quad_unit_vertex.y) * secondary_len);
    } else if (normalIdx == 4) { // +Z face: quad on XY plane, primary=X, secondary=Y
        transformed_pos_on_plane = vec3(quad_unit_vertex.x * primary_len, quad_unit_vertex.y * secondary_len, 0.0);
    } else { // normalIdx == 5    // -Z face: quad on XY plane, primary=X, secondary=Y (flip primary for CCW)
        transformed_pos_on_plane = vec3((1.0-quad_unit_vertex.x) * primary_len, quad_unit_vertex.y * secondary_len, 0.0);
    }

    v_world_position = a_instance_offset + transformed_pos_on_plane;
    gl_Position = u_projectionViewMatrix * vec4(v_world_position, 1.0);
}
