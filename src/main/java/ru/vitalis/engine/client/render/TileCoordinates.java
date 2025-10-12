package ru.vitalis.engine.client.render;

import ru.vitalis.engine.client.Frame;
import ru.vitalis.engine.client.render.r2d.Renderer2D;
import ru.vitalis.engine.core.Coordinates;

import java.util.Arrays;

public class TileCoordinates extends Coordinates{
    private static final double standardMultiplierSize = 0.1;
    private static int lastMinSide = 0;
    private static double scaler = 0;

    public TileCoordinates(int dimensions) {
        super(dimensions);
    }

    public Coordinates addEl(int axis, double value){
        return super.addEl(axis, value*scaler);
    }

    public Coordinates multiplyEl(int axis, double value){
        return super.multiplyEl(axis, value*scaler);
    }

    public Coordinates powEl(int axis, double value){
        return super.powEl(axis, value*scaler);
    }

    public Coordinates addAll(double term){
        return super.addAll(term*scaler);
    }

    public Coordinates multiplyAll(double multiplier){
        return super.multiplyAll(multiplier*scaler);
    }

    public Coordinates powAll(double value){
        return super.powAll(value*scaler);
    }

    public Coordinates addDepth(double ... terms){
        double[] scaledTerms = new double[terms.length];
        for(int i = 0; i < terms.length; i++){
            scaledTerms[i] = terms[i] * scaler;
        }
        return super.addDepth(scaledTerms);
    }

    public Coordinates multiplyDepth(double ... multipliers){
        double[] scaledElems = new double[multipliers.length];
        for(int i = 0; i < multipliers.length; i++){
            scaledElems[i] = multipliers[i] * scaler;
        }
        return super.multiplyDepth(scaledElems);
    }

    public Coordinates powDepth(double ... pows){
        double[] scaledPows = new double[pows.length];
        for(int i = 0; i < pows.length; i++){
            scaledPows[i] = pows[i] * scaler;
        }
        return super.powDepth(scaledPows);
    }

    public Coordinates fill(double i){
        Arrays.fill(array, i*scaler);
        return this;
    }

//    @Override
//    public double get(int axis) {
//        return super.get(axis) / scaler;
//    }



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
}
