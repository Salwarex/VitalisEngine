package ru.vitalis.engine.client.sprite;

import ru.vitalis.engine.client.ClientInstance;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public abstract class Sprite {
    protected int spriteSizeX = ClientInstance.getThread().getFrame().getRenderer().getUnitSize(); //ClientStrasure.getInstance().getScreen().getTileSize();
    protected int spriteSizeY = ClientInstance.getThread().getFrame().getRenderer().getUnitSize(); //ClientStrasure.getInstance().getScreen().getTileSize();

    public void setSpriteSize(int x, int y){
        if(x <= 0 || y <= 0) return;
        this.spriteSizeX = ClientInstance.getThread().getFrame().getRenderer().getUnitSize();
        this.spriteSizeY = ClientInstance.getThread().getFrame().getRenderer().getUnitSize();
    }
    public int getSpriteSizeX(){
        return this.spriteSizeX;
    }
    public int getSpriteSizeY(){
        return this.spriteSizeY;
    }

    public abstract BufferedImage getTexture(Character frame);
    protected BufferedImage loadTexture(String path){
        try {
            BufferedImage result = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/assets/textures" + path)));
            this.setSpriteSize(result.getWidth(), result.getHeight());
            return result;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
