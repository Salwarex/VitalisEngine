package ru.belisario.engine.client.render;

import ru.belisario.engine.client.Frame;
import ru.belisario.engine.client.render.r2d.Renderer2D;
import ru.belisario.engine.core.Coordinates;

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

    public static int getUnitSizeX() {
        return (int) Math.round(lastMinSide * scaler);
    }
    public static int getUnitSizeY() {
        return (int) Math.round(lastMinSide * scaler);
    }

    public static Coordinates getScreenView(Coordinates tileView){
        double modifier = scaler*2;
        return new Coordinates(2).set(Coordinates.X, tileView.get(Coordinates.X)*modifier).set(Coordinates.Y, tileView.get(Coordinates.Y)*modifier);
    }

    public static Coordinates getTileView(Coordinates screenView){
        double modifier = scaler*2;
        return new Coordinates(2).set(Coordinates.X, screenView.get(Coordinates.X)/modifier).set(Coordinates.Y, screenView.get(Coordinates.Y)/modifier);
    }
}
