package ru.vitalis.engine.client.render.r2d.buffers;

import ru.vitalis.engine.client.render.TileCoordinates;
import ru.vitalis.engine.core.Coordinates;

import static org.lwjgl.opengl.GL11.GL_DOUBLE;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import static ru.vitalis.engine.core.Coordinates.*;

public class RenderBuffers {
    public static RenderBuffer createBuffers(Coordinates coordinates, double[] size){
        double scaler = TileCoordinates.getScaler();

        double[] verticles = { // %4 = 0, 1 - позиция на экране, 2, 3 - UV-координаты (координаты текстуры)
                -scaler * size[0] + coordinates.get(X)*scaler, -scaler * size[1] + coordinates.get(Y)*scaler*2, 0, 0,
                scaler * size[0] + coordinates.get(X)*scaler, -scaler * size[1] + coordinates.get(Y)*scaler*2, 1, 0,
                scaler * size[0] + coordinates.get(X)*scaler,  scaler * size[1] + coordinates.get(Y)*scaler*2, 1, 1,
                -scaler * size[0] + coordinates.get(X)*scaler,  scaler * size[1] + coordinates.get(Y)*scaler*2, 0, 1
        };
        int[] indices = { //массив, определяющий, в каком порядке создаются треугольники
                0, 1, 2,
                2, 3, 0
        };

        int vao = glGenVertexArrays(); //объект, хранящий информацию об используемых атрибутах, связи с VBO. Шаблон для отрисовки.
        glBindVertexArray(vao);

        int vbo = glGenBuffers(); //буфер в видеопамяти GPU, где хранятся данные вершин. Получение id буфера.
        glBindBuffer(GL_ARRAY_BUFFER, vbo);  //делаем текущим
        glBufferData(GL_ARRAY_BUFFER, verticles, GL_STATIC_DRAW); //копируем verticles в gpu

        int ebo = glGenBuffers(); //буфер, хранящий индексы вершин. Создание и получение его id/
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); //задание буфера.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW); //при вызове glDrawElements будет брать вершины по этим индексам.

        //Настройка атрибутов вершин
        glVertexAttribPointer(0, //номер атрибута в шейдере
                2, //количество компонент (x, y, z)
                GL_DOUBLE, //тип данных
                false, //не нормализовать (приведение к [0, 1])
                4 * Double.BYTES, //шаг между началами соседних вершин
                0); //смещение внутри вершины
        glEnableVertexAttribArray(0); //включение использования атрибута

        //Настройка атрибутов UV
        glVertexAttribPointer(1, 2, GL_DOUBLE, false, 4 * Double.BYTES, 2 * Double.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new RenderBuffer(vao, vbo, ebo);
    }

    public static void updateBuffers(RenderBuffer buffer, Coordinates coordinates, double[] size) {
        if(size.length != 2) throw new IllegalArgumentException("size должен содержать ровно 2 координаты!");
        double scaler = TileCoordinates.getScaler();

        double[] vertices = { //надо вынести в отдельный метод для VBO и сделать координаты скелируемыми через TileCoordinates
                -scaler * size[0] + coordinates.get(X)*scaler*2, -scaler * size[1] + coordinates.get(Y)*scaler*2, 0, 0,
                scaler * size[0] + coordinates.get(X)*scaler*2, -scaler * size[1] + coordinates.get(Y)*scaler*2, 1, 0,
                scaler * size[0] + coordinates.get(X)*scaler*2,  scaler * size[1] + coordinates.get(Y)*scaler*2, 1, 1,
                -scaler * size[0] + coordinates.get(X)*scaler*2,  scaler * size[1] + coordinates.get(Y)*scaler*2, 0, 1
        };

        // Обновляем данные в существующем VBO
        glBindBuffer(GL_ARRAY_BUFFER, buffer.getVbo());
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
