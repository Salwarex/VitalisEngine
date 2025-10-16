package ru.belisario.engine.client.objects;

import org.jetbrains.annotations.NotNull;
import ru.belisario.engine.client.render.resource.Resources;
import ru.belisario.engine.client.render.resource.ResourceSet;
import ru.belisario.engine.core.Coordinates;
import ru.belisario.engine.client.render.ClientCords;
import ru.belisario.engine.client.render.r2d.buffers.RenderBuffer;
import ru.belisario.engine.client.render.r2d.buffers.RenderBuffers;
import ru.belisario.engine.client.render.r2d.Renderable;
import ru.belisario.engine.client.render.r2d.RenderableType;

import java.io.IOException;
import java.util.UUID;

public class ScreenObject implements Renderable, Comparable<ScreenObject> {
    protected final UUID uuid;
    protected final Coordinates centreScreenPos;
    protected final double[] size; //множители размера относительно стандартных 160x160
    protected final RenderableType renderType;
    protected final RenderBuffer renderBuffers;
    protected final ResourceSet resourceSet;

    public ScreenObject(UUID uuid, Coordinates centreScreenPos, RenderableType renderableType, double[] sizeMultipliers, String textureFolder){
        if(uuid == null) this.uuid = UUID.randomUUID();
        else this.uuid = uuid;
        this.centreScreenPos = centreScreenPos;
        this.renderType = renderableType;
        this.size = sizeMultipliers;
        this.renderBuffers = RenderBuffers.createBuffers(centreScreenPos, size);
        try {
            this.resourceSet =
                    Resources.getResourceSet(textureFolder)
                    .orElse(
                    new ResourceSet(
                            Resources.loadResourceId("textures/entity/player/template/idle/down1.png")
                    )); // без / на конце!
        }
        catch (IOException e){
            throw new RuntimeException("Не удалось загрузить текстуру!");
        }

    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Coordinates getCentreScreenPosition() {
        Coordinates result;
        try{
            result = centreScreenPos.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Coordinates[] getVertexScreenPosition(){
        Coordinates[] result = new Coordinates[4];
        for(int i = 0; i < 4 ; i++){
            result[i] = new Coordinates(2)
                    .set(Coordinates.X, (i % 3 == 0 ? -1 : 1) * ClientCords.getScaler() * size[0] + centreScreenPos.get(Coordinates.X))
                    .set(Coordinates.Y, (i <= 1 ? -1 : 1) * ClientCords.getScaler() * size[1] + centreScreenPos.get(Coordinates.Y));
        }
        return result;
    }

    @Override
    public int getTextureId(){
        return this.resourceSet.getCurrent();
    }

    @Override
    public ResourceSet getResourceSet() {
        return resourceSet;
    }

    @Override
    public RenderableType getRenderType() {
        return renderType;
    }

    @Override
    public int compareTo(@NotNull ScreenObject o) {
        return Double.compare(this.centreScreenPos.get(Coordinates.Y), o.centreScreenPos.get(Coordinates.Y));
    }

    @Override
    public RenderBuffer getRenderBuffers(){
        return this.renderBuffers;
    }

    @Override
    public double[] getSize() {
        return this.size;
    }

    double a = 0;

    @Override
    public void update(){
//        a += 0.01;
//
//        Coordinates norm = ClientCords.getScreenView(centreScreenPos);
//
//        double x = norm.get(X) + 0.1 * Math.cos(a);
//        double y = norm.get(Y) + 0.1 * Math.sin(a);

//        centreScreenPos.set(X, x).set(Y, y);
        RenderBuffers.updateBuffers(renderBuffers, centreScreenPos, size);
    }
}
