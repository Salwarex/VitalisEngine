package ru.belisario.engine.client.render.r2d.buffers;

public class RenderBuffer {
    private int vao;
    private int vbo;
    private int ebo;

    RenderBuffer(int vao, int vbo, int ebo){
        this.vao = vao;
        this.vbo = vbo;
        this.ebo = ebo;
    }

    void setVao(int vao){
        this.vao = vao;
    }

    void setVbo(int vbo){
        this.vbo = vbo;
    }

    void setEbo(int ebo){
        this.ebo = ebo;
    }

    public int getVao() {
        return vao;
    }

    public int getVbo() {
        return vbo;
    }

    public int getEbo() {
        return ebo;
    }
}
