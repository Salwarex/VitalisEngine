package ru.belisario.engine.client.render.resource;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.*;
import java.util.*;

import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Resources {
    public static int loadResourceId(String resourcePath) throws IOException {
        int width, height, channels;
        ByteBuffer imageBuffer;

        try(InputStream is = Resources.class.getClassLoader().getResourceAsStream(resourcePath)){
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
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); //GL_LINEAR - плавное сглаживание
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); //GL_NEAREST - резкие пиксели
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); //GL_CLAMP_TO_EDGE - растягивание крайних пикселей
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); //GL_REPEAT - повторять текстуру

        STBImage.stbi_image_free(image); //освобождение памяти под image

        glBindTexture(GL_TEXTURE_2D, 0); //отвязывание

        return textureId; //возвращение texture id из OpenGL.
    }

    public static Optional<ResourceSet> getResourceSet(String pathRoot){ //textures/entity/player/jacob
        Path filePath;
        try{
            filePath = getResourcePath(pathRoot + "/resource-set.br");
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        if(!Files.exists(filePath) || !Files.isReadable(filePath)) return Optional.empty();

        List<String> fileData;
        try{
            fileData = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл %s".formatted(filePath));
        }

        int currentTextures = 0;
        Map<String, ResourceLoadSet> textures = new HashMap<>();
        Map<String, String> animations = new HashMap<>();
        Map<String, String> arguments = new HashMap<>();

        for(String fLine : fileData){
            String trimmedLine = fLine.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }

            String[] args = fLine.split(" #")[0].split(" ");
            if(args.length == 0) continue;

            String command = args[0];

            if(args.length < 3) {
                throw new RuntimeException("Ошибка обработки синтаксиса файла %s".formatted(filePath));
            }

            String name = args[1];

            if(command.equalsIgnoreCase("version")){
                String version = "%s.%s".formatted(args[1], args[2]);
            }
            else if(command.equalsIgnoreCase("load")){
                int texture;
                String dir = pathRoot + "/" + args[2];
                try{
                    texture = loadResourceId(dir);
                } catch (IOException e) {
                    throw new RuntimeException("Ошибка при загрузке файла %s".formatted(dir));
                }
                if(currentTextures >= 36){
                    throw new RuntimeException("Невозможно добавить больше 36 текстур в один TextureSet!");
                }
                textures.put(name, new ResourceLoadSet(currentTextures, texture));
                currentTextures++;
            } else if (command.equalsIgnoreCase("animation")) {
                StringBuilder animationBuilder = new StringBuilder();

                for(int i = 2; i < args.length; i++){
                    String key = args[i];
                    if(key.charAt(0) == '#') continue;

                    if(!textures.containsKey(key)) {
                        throw new RuntimeException("Текстура с именем %s не загружена!");
                    }
                    ResourceLoadSet tls = textures.get(key);
                    animationBuilder.append(Character.forDigit(tls.localId(), 36));
                }

                animations.put(name, animationBuilder.toString());
            } else if (command.equalsIgnoreCase("set")) {
                String value = args[2];

                arguments.put(name, value);
            }
        }

       ResourceLoadSet[] tlsS = textures.values().toArray(new ResourceLoadSet[0]);
       Arrays.sort(tlsS, Comparator.comparingInt(ResourceLoadSet::localId));
       int[] textureIds = new int[tlsS.length];
       for(int i = 0; i < tlsS.length; i++){
           textureIds[i] = tlsS[i].glId();
       }

       return Optional.of(new ResourceSet(textureIds, animations, arguments));
    }

    private static Path getResourcePath(String resourceName) throws Exception {
        URL resource = Resources.class.getResource("/" + resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Ресурс не найден: " + resourceName);
        }

        URI uri = resource.toURI();

        if ("jar".equals(uri.getScheme())) {
            FileSystem fs = FileSystems.getFileSystem(uri);
            if (fs == null) {
                fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            return fs.getPath(resourceName);
        } else {
            return Paths.get(uri);
        }
    }
}

record ResourceLoadSet(int localId, int glId) {
}