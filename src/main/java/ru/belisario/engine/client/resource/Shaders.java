package ru.belisario.engine.client.resource;

import static org.lwjgl.opengl.GL30.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Shaders {
    private static int cachedShaderProgram = -1;

    public static int getShaderProgram(String vertexPath, String fragmentPath) {
        if(cachedShaderProgram != -1) return cachedShaderProgram;

        // 1. Загружаем исходный код шейдеров
        String vertexSource = loadFile(vertexPath);
        String fragmentSource = loadFile(fragmentPath);

        // 2. Создаём и компилируем вершинный шейдер
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);

        // Проверка ошибок компиляции вершинного шейдера
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Vertex shader compilation error: " + glGetShaderInfoLog(vertexShader));
        }

        // 3. Создаём и компилируем фрагментный шейдер
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);

        // Проверка ошибок компиляции фрагментного шейдера
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Fragment shader compilation error: " + glGetShaderInfoLog(fragmentShader));
        }

        // 4. Создаём шейдерную программу и прикрепляем шейдеры
        int program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        // 5. Линкуем программу
        glLinkProgram(program);

        // Проверка ошибок линковки
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader link program error: " + glGetProgramInfoLog(program));
        }

        // 6. Опционально: отсоединяем и удаляем шейдеры (они уже скопированы в программу)
        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);


        cachedShaderProgram = program;
        return program;
    }

    // Вспомогательная функция: читает файл как строку
    private static String loadFile(String resourcePath) {
        try (InputStream is = Shaders.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Ресурс не найден: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить шейдер: " + resourcePath, e);
        }
    }
}
