package ru.belisario.engine.client.resource;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import ru.belisario.engine.client.ClientInstance;
import ru.belisario.engine.client.Frame;
import ru.belisario.engine.client.GameThread;
import ru.belisario.engine.client.render.ClientCords;
import ru.belisario.engine.client.resource.ui.UIType;

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

public class ResourcesLoader {
    private static final ResourcePool pool = new ResourcePool();
    private static String BRVersion = null;

    public static int loadTextureGl(String resourcePath) throws IOException {
        int width, height, channels;
        ByteBuffer imageBuffer;

        try(InputStream is = ResourcesLoader.class.getClassLoader().getResourceAsStream(resourcePath)){
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

    public static ResourcePool getPool(){
        return pool;
    }

    public static void loadResources(String pathRoot, String fileName){
        List<ResourceSet> list = getResourceSet(pathRoot, fileName).get();
        for(ResourceSet set : list){
            pool.add(set.getKey(), set);
        }
    }

    public static Optional<List<ResourceSet>> getResourceSet(String key){
        if(pool.contains(key)) return Optional.of(List.of(pool.get(key).get()));
        String path = key.replace(".", "\\");

        return getResourceSet(path, null);
    }

    public static Optional<List<ResourceSet>> getResourceSet(String pathRoot, String fileName){ //textures/entity/player/jacob
        List<ResourceSet> resources = new ArrayList<>();
        RawResourceSet raw = BRHandle(pathRoot, fileName);
        resourcesTreeRecursiveLoad(raw, resources);
        return Optional.of(resources);
    }

    private static void resourcesTreeRecursiveLoad(RawResourceSet raw, List<ResourceSet> list){
        if(!raw.animations().isEmpty()
                || !raw.arguments().isEmpty()
                || !raw.textures().isEmpty()) {
            ResourceSet resource = handleRaw(raw);
            list.add(resource);
        }
        if(!raw.includes().isEmpty()){
            for(RawResourceSet includedRaw : raw.includes()){
                resourcesTreeRecursiveLoad(includedRaw, list);
            }
        }
    }

    private static ResourceSet handleRaw(RawResourceSet raw) {
        ResourceLoadSet[] tlsS = raw.textures().values().toArray(new ResourceLoadSet[0]);
        Arrays.sort(tlsS, Comparator.comparingInt(ResourceLoadSet::localId));
        int[] textureIds = new int[tlsS.length];
        for(int i = 0; i < tlsS.length; i++){
            textureIds[i] = tlsS[i].glId();
        }

        System.out.printf("[BELISARIO RESOURCES] Loaded resource %s%n".formatted(raw.path()));

        return new ResourceSet(raw.path(), textureIds, raw.animations(), raw.arguments());
    }

    private static RawResourceSet BRHandle(String pathRoot, String fileName) {
        System.out.printf("[BELISARIO RESOURCES] File handling: %s\\%s.br%n"
                .formatted(pathRoot, (fileName == null ? "resource-set" : fileName)));

        Path filePath;
        try{
            filePath = getResourcePath(pathRoot + "/%s.br"
                    .formatted(fileName == null ? "resource-set" : fileName));
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        if(!Files.exists(filePath) || !Files.isReadable(filePath))
            throw new RuntimeException("Файл %s не найден!".formatted(filePath));

        List<String> fileData;
        try{
            fileData = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл %s".formatted(filePath));
        }

        String key = pathRoot
                .replace("\\", ".")
                .replace("/", ".");
        Map<String, ResourceLoadSet> textures = new HashMap<>();
        Map<String, String> animations = new HashMap<>();
        Map<String, String> arguments = new HashMap<>();
        List<RawResourceSet> includes = new ArrayList<>();

        int currentTextures = 0;
        for(String fLine : fileData){
            String trimmedLine = fLine.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            String[] args = fLine.split(" #")[0].split(" ");
            if(args.length == 0) continue;

            String command = args[0];

            if(args.length < 2) {
                throw new RuntimeException("Ошибка обработки синтаксиса файла %s (Недостаточно аргументов!)".formatted(filePath));
            }
            String name = args[1];

            if(command.equalsIgnoreCase("include")){
                String includedPathRoot = "%s\\%s".formatted(pathRoot, name.replace(".", "\\"));
                String includedFileName = (args.length >= 3 ? args[2] : null);
                includes.add(BRHandle(includedPathRoot, includedFileName));

                continue;
            } else if (command.equalsIgnoreCase("name")) {
                key = name;
                continue;
            }

            if(args.length < 3) {
                throw new RuntimeException("Ошибка обработки синтаксиса файла %s (Недостаточно аргументов!)".formatted(filePath));
            }

            if(command.equalsIgnoreCase("version")){
                String version = "%s.%s".formatted(args[1], args[2]);
                if(BRVersion == null) BRVersion = version;
            }
            else if(command.equalsIgnoreCase("load")){
                int texture = BRLoad(pathRoot+ "\\" + args[2]);
                if(currentTextures >= 36){
                    throw new RuntimeException("Невозможно добавить больше 36 текстур в один ResourceSet!");
                }
                textures.put(name, new ResourceLoadSet(currentTextures, texture));
                currentTextures++;
            } else if (command.equalsIgnoreCase("animation")) {
                String animation = BRAnimation(Arrays.copyOfRange(args, 2, args.length), textures);

                animations.put(name, animation);
            } else if (command.equalsIgnoreCase("set")) {
                String value = args[2];

                arguments.put(name, value);
            }
        }

        return new RawResourceSet(key, textures, animations, arguments, includes);
    }


    private static String BRAnimation(String[] args, Map<String, ResourceLoadSet> textures) {
        StringBuilder animationBuilder = new StringBuilder();

        for (String key : args) {
            if (key.charAt(0) == '#') continue;

            if (!textures.containsKey(key)) {
                throw new RuntimeException("Текстура с именем %s не загружена!");
            }
            ResourceLoadSet tls = textures.get(key);
            animationBuilder.append(Character.forDigit(tls.localId(), 36));
        }
        return animationBuilder.toString();
    }

    private static int BRLoad(String path) {
        int texture;
        try{
            texture = loadTextureGl(path);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке файла %s: %s".formatted(path, e.toString()));
        }
        return texture;
    }

    private static Path getResourcePath(String resourceName) throws Exception {
        URL resource = ResourcesLoader.class.getResource("/" + resourceName);
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

record Param(
        ProvidedParamsObjectType defaultType,
        String defaultName,
        String origin
){
    @Override
    public ProvidedParamsObjectType defaultType() {
        return defaultType;
    }

    @Override
    public String defaultName() {
        return defaultName;
    }

    @Override
    public String origin() {
        return origin;
    }
}

record RawResourceSet(
        String path,
        Map<String, ResourceLoadSet> textures,
        Map<String, String> animations,
        Map<String, String> arguments,
        List<RawResourceSet> includes
){
    @Override
    public String path() {
        return path;
    }

    @Override
    public Map<String, ResourceLoadSet> textures() {
        return textures;
    }

    @Override
    public Map<String, String> animations() {
        return animations;
    }

    @Override
    public Map<String, String> arguments() {
        return arguments;
    }

    @Override
    public List<RawResourceSet> includes() {
        return includes;
    }
}

enum ProvidedParamsObjectType {
    IMAGE,
    ANIMATION,
    LINK,
    UNKNOWN
}

record ResourceLoadSet(int localId, int glId) {
}

@FunctionalInterface
interface MathExpression{
    double apply(double a, double b);
}

enum MathOperation {
    PLUS((a, b) -> a + b),
    MINUS((a, b) -> a - b),
    DIVIDE((a, b) -> a / b),
    MULTIPLY((a, b) -> a*b),
    UNKNOWN((a, b) -> Double.NaN);

    MathExpression expression;

    MathOperation(MathExpression expression){
        this.expression = expression;
    }

    static MathOperation getByOperation(char operationChar){
        return switch(operationChar){
            case '+' -> PLUS;
            case '-' -> MINUS;
            case '/' -> DIVIDE;
            case '*' -> MULTIPLY;
            default -> UNKNOWN;
        };
    }
}