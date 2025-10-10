package ru.vitalis.engine.client;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import ru.vitalis.engine.client.render.Renderer;
import ru.vitalis.engine.client.render.Renderer2D;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Frame {
    private final long window;
    private final int height;
    private final int width;
    private final Renderer renderer;

    public Frame(int width, int height, boolean _3D){
        this.height = height;
        this.width = width;
        this.renderer = new Renderer2D(this);

        //местный обработчик ошибок
        GLFWErrorCallback.createPrint(System.err).set();

        //Включение glfw
        if (!glfwInit())
            throw new IllegalStateException("GLFW error!");

        //Настройки glfw по-умолчанию
        glfwDefaultWindowHints();

        //Окно невидимое (?)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        //Окно неизменяемое
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        //Создание окна
        window = glfwCreateWindow(this.width, this.height, "Vitalis Project", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Can't create window");

        //Обработчик нажатий клавишь и взаимодействие для взимодействия окном
//        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
//            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
//                glfwSetWindowShouldClose(window, true);
//        });

        //Передача данных в нативный C-код
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            //Подгон окна (?)
            glfwGetWindowSize(window, pWidth, pHeight);

            //Разрешение экрана
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            //Перемещение окна в центр
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        //Задаем окно текущим контекстом
        glfwMakeContextCurrent(window);

        //Плавная смена кадров
        glfwSwapInterval(1);

        //Показываем окно
        glfwShowWindow(window);
    }


    public long getWindow(){
        return window;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Renderer getRenderer() {
        return renderer;
    }
}
