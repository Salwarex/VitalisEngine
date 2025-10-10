package ru.vitalis.engine.client.sprite;

public class AnimatedSpriteFramesAmountException extends RuntimeException {
    public AnimatedSpriteFramesAmountException() {
        super("Количество ключевых символов строки не соответствует числу указанных путей!");
    }
}
