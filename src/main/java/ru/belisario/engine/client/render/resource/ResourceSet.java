package ru.belisario.engine.client.render.resource;

import java.util.Map;

public class ResourceSet {
    private final int[] textures;
    private int cursor;

    //animation
    private int animCursor;
    private String currentAnimation = null;
    private final Map<String, String> animations;

    public ResourceSet(int[] textureIds, Map<String, String> animations){
        this.textures = new int[Math.min(textureIds.length, 36)];
        this.animations = animations;
        System.arraycopy(textureIds, 0, textures, 0, textures.length);
    }

    public ResourceSet(int textureId){
        textures = new int[1];
        animations = null;
        textures[0] = textureId;
    }

    public int getCurrent(){
        return getImage(cursor);
    }

    public int nextImage(){
        cursor++;
        return getImage(cursor);
    }

    public int getImage(int i){
        cursor = i % textures.length;
        return textures[cursor];
    }

    public int getCursor(){
        return cursor;
    }

    public int animate(String mask){//animation
        if(!mask.matches("[a-zA-Z0-9]+")) mask = null;
        if(mask == null) currentAnimation = null;
        if(currentAnimation == null) return getImage(cursor);
        if(!currentAnimation.equalsIgnoreCase(mask)){
            currentAnimation = mask;
            animCursor = 0;
        }

        char currentChar = currentAnimation.charAt(animCursor);
        int currentId = Character.digit(currentChar, 36);

        if(currentId <= -1)
            throw new RuntimeException("Неизвестный символ в маске анимации");

        return getImage(currentId);
    }

    public int animate(){
        return animate(currentAnimation);
    }

    public void setAnimation(String name){
        if(animations.containsKey(name))
            currentAnimation = animations.get(name);
        else throw new RuntimeException("Анимация %s не найдена!".formatted(name));
    }
}
