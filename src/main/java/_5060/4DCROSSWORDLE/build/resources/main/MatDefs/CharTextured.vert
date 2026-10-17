attribute vec3 inPosition;
attribute vec2 inTexCoord; // Original texture coordinates of the plane
uniform mat4 g_WorldViewProjectionMatrix;

varying vec2 texCoord;
uniform vec4 m_ColorMap_UVs; // UVs from our material parameter

void main() {
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);

    // Remap the plane's UVs (0-1) to the specific character's UVs on the atlas
    texCoord.x = mix(m_ColorMap_UVs.x, m_ColorMap_UVs.z, inTexCoord.x); // uMin to uMax
    texCoord.y = mix(m_ColorMap_UVs.y, m_ColorMap_UVs.w, inTexCoord.y); // vMin to vMax
}