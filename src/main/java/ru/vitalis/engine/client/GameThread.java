package ru.vitalis.engine.client;

import org.lwjgl.opengl.GL;
import ru.vitalis.engine.client.render.Renderer;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class GameThread implements Callable<Integer> {
    private final int tickRate;
    private Frame frame;
    private int exitCode = 1;
    //    private Scene scene;
    //    private AccountHandler accounts;
    //    private MouseHandler mouse;
    //    private KeyboardHandler keyboard;
    //    private NetworkManager network;
    //    private SoundManager sounds;

    public GameThread(){
        this.tickRate = 64;
    }

    @Override
    public Integer call() {
        frame = new Frame(1600, 900, false);

        if(frame.getWindow() == 0L) exitCode = 2;

        if(exitCode == 1) loop();

        //После цикла - освобождение памяти
        glfwFreeCallbacks(frame.getWindow());
        glfwDestroyWindow(frame.getWindow());
        glfwTerminate();
        glfwSetErrorCallback(null).free();

        return exitCode;
    }

    private void loop() {
        //Возможность рисования
        GL.createCapabilities();

        Renderer renderer = frame.getRenderer();
        renderer.setProjection();

        //Установка цвета фона
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        long prevTickNanos = 0L;
        long tickRateNanos = 1_000_000_000 / tickRate;

        //Игровой цикл
        while (!glfwWindowShouldClose(frame.getWindow())) {
            if(exitCode != 1) break;

            long currentTimeNanos = System.nanoTime();
            if((currentTimeNanos - prevTickNanos) < tickRateNanos) continue;
            prevTickNanos = currentTimeNanos;

            //Закрашивание
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            frame.getRenderer().draw();

            //Передача буферов (для избежания искажения)
            glfwSwapBuffers(frame.getWindow());

            //Проверка
            glfwPollEvents();
        }
    }

    public Frame getFrame() {
        return frame;
    }
}
