package ru.vitalis.engine.client.objects;

import org.jetbrains.annotations.NotNull;
import ru.vitalis.engine.client.render.Renderable;
import ru.vitalis.engine.client.render.RenderableType;
import ru.vitalis.engine.core.Coordinates;

import java.util.UUID;

public abstract class ScreenObject implements Renderable, Comparable<ScreenObject> {
    protected final UUID uuid;
    protected final Coordinates centrePosition;
    private final RenderableType type;

    public ScreenObject(UUID uuid, Coordinates centrePosition, RenderableType renderableType){
        if(uuid == null) this.uuid = UUID.randomUUID();
        else this.uuid = uuid;
        this.centrePosition = centrePosition;
        this.type = renderableType;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Coordinates getCentrePosition() {
        Coordinates result;
        try{
            result = centrePosition.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public RenderableType getType() {
        return type;
    }

    @Override
    public int compareTo(@NotNull ScreenObject o) {
        return Double.compare(this.centrePosition.get(Coordinates.Y), o.centrePosition.get(Coordinates.Y));
    }
}
