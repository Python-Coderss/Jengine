#version 300 es
precision mediump float;
in vec3 v_world_position;
in vec3 v_normal_worldspace;
in vec3 v_color;
out vec4 FragColor;
void main() {
    vec3 lightDirection = normalize(vec3(0.5, 0.8, 0.2));
    float ambient = 0.3;
    float diffuse = max(0.0, dot(normalize(v_normal_worldspace), lightDirection));
    FragColor = vec4(v_color * (ambient + diffuse), 1.0);
}
