#version 330 compatibility // ← ключевое: compatibility

in vec2 aPos;
in vec2 aTexCoord;

out vec2 TexCoord;

void main() {
    TexCoord = aTexCoord;

    // Автоматически использует GL_PROJECTION и GL_MODELVIEW
    gl_Position = gl_ModelViewProjectionMatrix * vec4(aPos, 0.0, 1.0);
}