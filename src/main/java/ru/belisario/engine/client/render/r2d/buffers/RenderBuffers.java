package ru.belisario.engine.client.render.r2d.buffers;

import ru.belisario.engine.core.Coordinates;
import ru.belisario.engine.client.render.ClientCords;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBuffers {
    public static RenderBuffer createBuffers(Coordinates coordinates, double[] size){
        int[] indices = { //массив, определяющий, в каком порядке создаются треугольники
                0, 1, 2,
                2, 3, 0
        };

        int vao = glGenVertexArrays(); //объект, хранящий информацию об используемых атрибутах, связи с VBO. Шаблон для отрисовки.
        glBindVertexArray(vao);

        int vbo = vbo(null, coordinates, size);

        int ebo = glGenBuffers(); //буфер, хранящий индексы вершин. Создание и получение его id/
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); //задание буфера.
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW); //при вызове glDrawElements будет брать вершины по этим индексам.

        //Настройка атрибутов вершин
        glVertexAttribPointer(0, //номер атрибута в шейдере
                2, //количество компонент (x, y, z)
                GL_FLOAT, //тип данных
                false, //не нормализовать (приведение к [0, 1])
                4 * Float.BYTES, //шаг между началами соседних вершин
                0); //смещение внутри вершины
        glEnableVertexAttribArray(0); //включение использования атрибута

        //Настройка атрибутов UV
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return new RenderBuffer(vao, vbo, ebo);
    }

    private static int vbo(RenderBuffer buffer, Coordinates coordinates, double[] size){
        double scaler = ClientCords.getScaler();
        Coordinates normCords = ClientCords.getScreenView(coordinates);
        double x = normCords.get(Coordinates.X);
        double y = normCords.get(Coordinates.Y);

        float[] vertices = { //надо вынести в отдельный метод для VBO и сделать координаты скелируемыми через TileCoordinates
                (float) (-scaler * size[0] + x), (float) (-scaler * size[1] + y), 0f, 0f,
                (float) (scaler * size[0] + x), (float) (-scaler * size[1] + y), 1f, 0f,
                (float) (scaler * size[0] + x),  (float) (scaler * size[1] + y), 1f, 1f,
                (float) (-scaler * size[0] + x),  (float) (scaler * size[1] + y), 0f, 1f
        };

        int vbo = 0;
        if(buffer == null){
            vbo = glGenBuffers(); //буфер в видеопамяти GPU, где хранятся данные вершин. Получение id буфера.
            glBindBuffer(GL_ARRAY_BUFFER, vbo);  //делаем текущим
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); //копируем vertices в gpu
        }
        else{
            glBindBuffer(GL_ARRAY_BUFFER, buffer.getVbo());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        return vbo;
    }

    public static void updateBuffers(RenderBuffer buffer, Coordinates coordinates, double[] size) {
        if(size.length != 2) throw new IllegalArgumentException("size должен содержать ровно 2 координаты!");
        vbo(buffer, coordinates, size);
    }
}
