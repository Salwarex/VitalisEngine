package ru.vitalis.engine.client.objects;

import org.jetbrains.annotations.NotNull;
import ru.vitalis.engine.client.render.TileCoordinates;
import ru.vitalis.engine.client.render.r2d.buffers.RenderBuffer;
import ru.vitalis.engine.client.render.r2d.buffers.RenderBuffers;
import ru.vitalis.engine.client.render.r2d.Renderable;
import ru.vitalis.engine.client.render.r2d.RenderableType;
import ru.vitalis.engine.client.render.r2d.texture.Textures;
import ru.vitalis.engine.core.Coordinates;

import java.io.IOException;
import java.util.UUID;

import static ru.vitalis.engine.core.Coordinates.*;

public class ScreenObject implements Renderable, Comparable<ScreenObject> {
    protected final UUID uuid;
    protected final Coordinates centreScreenPos;
    protected final double[] size; //множители размера относительно стандартных 160x160
    protected final RenderableType renderType;
    protected final RenderBuffer renderBuffers;
    protected int textureId;

    public ScreenObject(UUID uuid, Coordinates centreScreenPos, RenderableType renderableType, double[] sizeMultipliers, String texturePath){
        if(uuid == null) this.uuid = UUID.randomUUID();
        else this.uuid = uuid;
        this.centreScreenPos = centreScreenPos;
        this.renderType = renderableType;
        this.size = sizeMultipliers;
        this.renderBuffers = RenderBuffers.createBuffers(centreScreenPos, size);
        try{
            this.textureId = Textures.loadTextureId(texturePath);
        } catch (IOException e) {
            e.printStackTrace();
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
                    .set(X, (i % 3 == 0 ? -1 : 1) * TileCoordinates.getScaler() * size[0] + centreScreenPos.get(X))
                    .set(Y, (i <= 1 ? -1 : 1) * TileCoordinates.getScaler() * size[1] + centreScreenPos.get(Y));
        }
        return result;
    }

    @Override
    public int getTextureId(){
        return this.textureId;
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
//        double x = 0.5 * Math.cos(a);
//        double y = 0.5 * Math.sin(a);
//
//        centreScreenPos.set(X, x).set(Y, y);
        RenderBuffers.updateBuffers(renderBuffers, centreScreenPos, size);
    }
}
