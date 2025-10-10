package ru.vitalis.engine.client.sprite;



import java.awt.image.BufferedImage;
import java.util.*;

public class MultiSprite extends Sprite {
    private Map<Character, BufferedImage> images;
    private int fps = 60;

    public MultiSprite(LinkedHashSet<Character> keys, String ... paths){
        images = new LinkedHashMap<>();
        if(keys.size() != paths.length) throw new AnimatedSpriteFramesAmountException();
        int i = 0;
        for(Character frame : keys){
            BufferedImage image = this.loadTexture(paths[i]);
            images.put(frame, image);
            i++;
        }
    }

    public MultiSprite(LinkedHashMap<Character, String> frames){
        images = new LinkedHashMap<>();
        for(Character frame : frames.keySet()){
            BufferedImage image = this.loadTexture(frames.get(frame));
            images.put(frame, image);
        }
    }

    private MultiSprite(){}

    public void setFps(int fps){
        this.fps = fps;
    }

    public int getFps(){
        return this.fps;
    }

    public Set<Character> getFrames(){
        return images.keySet();
    }

    @Override
    public BufferedImage getTexture(Character frame) {
        if(frame == null) {
            List<Character> list = new ArrayList<>(images.keySet());
            frame = list.getFirst();
            list.clear();
        }
        return images.get(frame);
    }
}

