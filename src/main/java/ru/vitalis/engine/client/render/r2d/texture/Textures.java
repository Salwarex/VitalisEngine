package ru.vitalis.engine.client.render.r2d.texture;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Textures {
    public static int loadTextureId(String resourcePath) throws IOException {
        int width, height, channels;
        ByteBuffer imageBuffer;

        try(InputStream is = Textures.class.getClassLoader().getResourceAsStream(resourcePath)){
            if(is == null) throw new IOException("File is not exists: " + resourcePath);

            byte[] bytes = is.readAllBytes(); //читаем байты из потока

            //выделяем из нативной памяти (с которой работает OpenGL) место под изображение соразмерно массиву байтов.
            imageBuffer = memAlloc(bytes.length);
            imageBuffer.put(bytes).flip(); //передаем массив байтов в буфер и переводим режим
        }

        //Декодированние
        ByteBuffer image; //буфер для декодированных байтов
        try(MemoryStack stack = MemoryStack.stackPush()){ //временная память-стек
            //выделение места во временной памяти
            IntBuffer w = stack.mallocInt(1); //ширина
            IntBuffer h = stack.mallocInt(1); //высота
            IntBuffer c = stack.mallocInt(1); //каналы

            //непосредственно декодирование
            image = STBImage.stbi_load_from_memory(imageBuffer, w, h, c, 0);
            //загружаем из памяти в буфер, где каждый пиксель массив из rgba.

            if (image == null) {
                throw new IOException("Can't load the image: " + STBImage.stbi_failure_reason());
            }

            //переносим высчитанные значения
            width = w.get(0);
            height = h.get(0);
            channels = c.get(0);
        }

        memFree(imageBuffer); //очищаем ненужные данные из первого буфера.

        //форматирование для интерпретации данных
        int format;
        switch(channels){
            case 1 -> format = GL_RED; //серый
            case 3 -> format = GL_RGB; //цветной
            case 4 -> format = GL_RGBA; //цветной с альфа-каналом
            default -> throw new IOException("Can't load the image: Unavailable channels size");
        }

        int textureId = glGenTextures(); //создаем opengl id текстуры
        glBindTexture(GL_TEXTURE_2D, textureId); //начинаем обработку данной текстуры

        glTexImage2D(GL_TEXTURE_2D,  //тип текстуры
                0, //уровень MIP (0 - база)
                format, //внутренний формат (сколько данных хранить на GPU)
                width, //ширина
                height, //высота
                0, //устаревший параметр (всегда = 0)
                format, //формат пикселей в буфере
                GL_UNSIGNED_BYTE, //тип данных (байты от 0 до 255)
                image); //буфер пикселей

        //фильтрация и обертка
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); //GL_LINEAR - плавное сглаживание
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); //GL_NEAREST - резкие пиксели
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); //GL_CLAMP_TO_EDGE - растягивание крайних пикселей
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); //GL_REPEAT - повторять текстуру

        STBImage.stbi_image_free(image); //освобождение памяти под image

        glBindTexture(GL_TEXTURE_2D, 0); //отвязывание

        return textureId; //возвращение texture id из OpenGL.
    }
}
