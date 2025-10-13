package ru.vitalis.engine.client.render;

import ru.vitalis.engine.client.Frame;
import ru.vitalis.engine.client.render.r2d.Renderer2D;
import ru.vitalis.engine.core.Coordinates;

import static ru.vitalis.engine.core.Coordinates.*;

public class ClientCords {
    private static final double standardMultiplierSize = 0.1;
    private static int lastMinSide = 0;
    private static double scaler = 0;

    public static void updateScaler(){
        Frame frame = Renderer2D.getInstance().getFrame();
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

    public static double getScaler(){
        updateScaler();
        return scaler;
    }

    public static int getUnitSize() {
        return (int) Math.round(lastMinSide * scaler);
    }

    public static Coordinates getScreenView(Coordinates tileView){
        double modifier = scaler*2;
        return new Coordinates(2).set(X, tileView.get(X)*modifier).set(Y, tileView.get(Y)*modifier);
    }

    public static Coordinates getTileView(Coordinates screenView){
        double modifier = scaler*2;
        return new Coordinates(2).set(X, screenView.get(X)/modifier).set(Y, screenView.get(Y)/modifier);
    }
}
