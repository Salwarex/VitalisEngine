package ru.belisario.engine.client.resource;

import java.util.Map;
import java.util.Optional;

public class ResourceSet implements Cloneable{
    private final String key;

    private final int[] textures;
    private int cursor;

    //animation
    private int animCursor;
    private String currentAnimation = null;
    private final Map<String, String> animations;

    //other arguments
    private final Map<String, String> arguments;

    public ResourceSet(String key, int[] textureIds, Map<String, String> animations, Map<String, String> arguments){
        this.key = key;
        this.textures = new int[Math.min(textureIds.length, 36)];
        this.animations = animations;
        this.arguments = arguments;
        System.arraycopy(textureIds, 0, textures, 0, textures.length);
    }

    public ResourceSet(String key, int textureId){
        this.key = key;
        textures = new int[1];
        animations = null;
        arguments = null;
        textures[0] = textureId;
    }

    public String getKey() {
        return key;
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
        if(animations == null) return;
        if(animations.containsKey(name))
            currentAnimation = animations.get(name);
        else throw new RuntimeException("Анимация %s не найдена!".formatted(name));
    }

    public Optional<String> getArgument(String key){
        if(arguments == null) return Optional.empty();
        if(!arguments.containsKey(key)) return Optional.empty();
        return Optional.of(arguments.get(key));
    }

    @Override
    protected ResourceSet clone() {
        return new ResourceSet(key, textures, animations, arguments);
    }
}
