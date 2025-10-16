package ru.belisario.engine.client.render;

import ru.belisario.engine.client.render.r2d.Renderable;

public interface Renderer {
    void setProjection();
    void draw();
    void addRenderable(Renderable renderable);
    void removeRenderable(Renderable renderable);
}
