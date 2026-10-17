uniform sampler2D m_ColorMap; // Our font texture atlas
varying vec2 texCoord;

void main() {
    gl_FragColor = texture2D(m_ColorMap, texCoord);
    // You could also multiply by a color if you wanted to tint the characters
    // gl_FragColor *= vec4(1.0, 1.0, 1.0, 1.0);
}