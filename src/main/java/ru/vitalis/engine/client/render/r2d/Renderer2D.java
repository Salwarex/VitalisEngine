package ru.vitalis.engine.client.render.r2d;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import ru.vitalis.engine.client.Frame;
import ru.vitalis.engine.client.objects.ScreenObject;
import ru.vitalis.engine.client.render.Renderer;
import ru.vitalis.engine.client.render.r2d.texture.Shaders;
import ru.vitalis.engine.core.Coordinates;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL30.*;

public class Renderer2D implements Renderer {
    private static Renderer2D instance = null;

    private final Frame frame;
    private final Set<Renderable> renderables = new HashSet<>();

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public Renderer2D(Frame frame){
        instance = this;
        this.frame = frame;
    }

    @Override
    public void addRenderable(Renderable renderable) {
        if(renderables.contains(renderable)) return;
        renderables.add(renderable);
    }

    @Override
    public void removeRenderable(Renderable renderable){
        if(!renderables.contains(renderable)) return;
        renderables.remove(renderable);
    }

    public void setProjection() {
        int width = frame.getWidth();
        int height = frame.getHeight();

        glViewport(0, 0, width, height);

        float aspect = (float) width / height;
        float halfSize = 1.0f;

        // Сбрасываем буфер матрицы
        matrixBuffer.clear();

        if (aspect >= 1.0) {
            projectionMatrix.setOrtho(
                    -halfSize * aspect, halfSize * aspect,
                    -halfSize, halfSize,
                    -1.0f, 1.0f
            );
        } else {
            projectionMatrix.setOrtho(
                    -halfSize, halfSize,
                    -halfSize / aspect, halfSize / aspect,
                    -1.0f, 1.0f
            );
        }
    }

    @Override
    public void draw(){
        if(renderables.isEmpty()){
            for(int i = -6; i <= 6; i++){
                for(int j = -6; j <= 6; j++){
                    Coordinates cord = new Coordinates(2);
                    ScreenObject object = new ScreenObject(null,
                            cord.set(Coordinates.X, i).set(Coordinates.Y, j),
                            RenderableType.PROPS, new double[]{1.0, 1.0}, (i % 2 == 0 ?
                            "textures/entity/player/jacob/idle/down1.png"
                            : "textures/entity/player/jacob/idle/down2.png"));
                    renderables.add(object);
                }
            }
        }

        for(Renderable current : renderables){
            current.update();

            //загрузка шейдерной программы
            //vertex - обрабатывает каждую вершину
            //fragment - обрабатывает каждый пиксель (фрагмент)
            int shaderProgram = Shaders.getShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");

            //загрузка текстуры
            int textureId = current.getTextureId();

            //Активация текстуры
            //активация шейдерной программы
            glUseProgram(shaderProgram);
            // Передача матрицы проекции
            int projLoc = glGetUniformLocation(shaderProgram, "u_Projection");
            if (projLoc != -1) {
                projectionMatrix.get(matrixBuffer); // Записываем матрицу в буфер
                glUniformMatrix4fv(projLoc, false, matrixBuffer);
            }


            glActiveTexture(GL_TEXTURE0); //текстурный юнит (канал)
            glBindTexture(GL_TEXTURE_2D, textureId); //привязывает ткустуру к этому юниту

            int loc = glGetUniformLocation(shaderProgram, "u_Texture"); //берет u_Texture из шейдера
            glUniform1i(loc, 0); // 0 — это GL_TEXTURE0

            glBindVertexArray(current.getRenderBuffers().getVao()); //загружает все настройки
            //отрисовка
            glDrawElements(GL_TRIANGLES, //режим треугольников
                    6, //количество индексов
                    GL_UNSIGNED_INT, //тип каждого индекса
                    0); //смещение EBO

            //очистка
            glBindVertexArray(0);
            glUseProgram(0);
        }
    }

    public static Renderer2D getInstance(){
        return instance;
    }

    public Frame getFrame() {
        return frame;
    }
}
