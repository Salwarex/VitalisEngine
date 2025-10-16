package ru.belisario.engine.client.render.r2d;

import ru.belisario.engine.client.render.r2d.buffers.RenderBuffer;
import ru.belisario.engine.client.render.resource.ResourceSet;
import ru.belisario.engine.core.Coordinates;

public interface Renderable {
    int getTextureId();
    ResourceSet getResourceSet();
    RenderableType getRenderType();
    RenderBuffer getRenderBuffers();
    double[] getSize();
    Coordinates getCentreScreenPosition();
    Coordinates[] getVertexScreenPosition();
    void update();
}
