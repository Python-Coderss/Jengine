#ifdef GL_ES
    precision mediump float;
#endif

varying vec4 v_color;
varying vec3 v_normal_worldspace; // Normal in world space

void main() {
    vec3 lightDirection = normalize(vec3(0.5, 0.8, 0.2)); // Adjusted light direction slightly
    float ambient = 0.3;
    float diffuse = max(0.0, dot(normalize(v_normal_worldspace), lightDirection));

    gl_FragColor = v_color * (ambient + diffuse);
    // gl_FragColor = vec4(normalize(v_normal_worldspace) * 0.5 + 0.5, 1.0); // Visualize normals
}
