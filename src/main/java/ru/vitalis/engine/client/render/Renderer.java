package ru.vitalis.engine.client.render;

public interface Renderer {
    void setProjection();
    void draw();
    int getUnitSize();
}
