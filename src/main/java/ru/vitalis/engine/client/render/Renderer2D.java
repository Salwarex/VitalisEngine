package ru.vitalis.engine.client.render;

import ru.vitalis.engine.client.Frame;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer2D implements Renderer{
    private final Frame frame;
    private final List<Renderable> renderables = new ArrayList<>();

    private final double standardMultiplierSize = 0.1;
    private int lastMinSide = 0;
    private double scaler = 0;

    public Renderer2D(Frame frame){
        this.frame = frame;
        updateScaler();
    }

    public void setProjection() {
        glEnable(GL_DEPTH_TEST);

        int width = frame.getWidth();
        int height = frame.getHeight();

        glViewport(0, 0, width, height);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        double aspect = (double) width / height;
        double halfSize = 1.0;

        if (aspect >= 1.0f) {
            glOrtho(-halfSize * aspect, halfSize * aspect, -halfSize, halfSize, -1.0, 1.0);
        } else {
            glOrtho(-halfSize, halfSize, -halfSize / aspect, halfSize / aspect, -1.0, 1.0);
        }
    }

    @Override
    public void draw(){
        glTranslatef(0, 0, 0f);
        glBegin(GL_QUADS);
        glColor3d(1.0f, 0f, 0.0f); //цвета

        //скаляры относительно всего размера окна. Z больше - ближе к экрану, меньше - дальше
        updateScaler();

        glVertex3d(-scaler, -scaler, 0); //левый нижний
        glVertex3d(scaler, -scaler, 0); //правый нижний
        glVertex3d(scaler, scaler, 0); //правый верхний
        glVertex3d(-scaler, scaler, 0); //левый верхний

        glEnd();
    }

    private void updateScaler(){
        int minSide = Math.min(frame.getHeight(), frame.getWidth());
        if(minSide == lastMinSide) return;

        int standardScreenSize = 800;

        if(minSide < 500){
            standardScreenSize = 400;
        } else if (minSide <= 700) {
            standardScreenSize = 600;
        }

        lastMinSide = minSide;

        scaler = standardScreenSize * standardMultiplierSize / minSide;
    }

    @Override
    public int getUnitSize() {
        return (int) Math.round(this.lastMinSide * scaler);
    }
}
