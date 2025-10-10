package ru.vitalis.engine.client.sprite;

import java.awt.image.BufferedImage;

public class StaticSprite extends Sprite{

    private final BufferedImage image;

    public StaticSprite(String path){
        this.image = this.loadTexture(path);
    }

    @Override
    public BufferedImage getTexture(Character frame) {
        return image;
    }
}
