package ru.vitalis.engine.client.render.r2d;

import ru.vitalis.engine.client.render.r2d.buffers.RenderBuffer;
import ru.vitalis.engine.core.Coordinates;

public interface Renderable {
    int getTextureId();
    RenderableType getRenderType();
    RenderBuffer getRenderBuffers();
    double[] getSize();
    Coordinates getCentreScreenPosition();
    Coordinates[] getVertexScreenPosition();
    void update();
}
