package ru.vitalis.engine.core;

import java.util.Arrays;

public class Coordinates implements Cloneable{
    protected double[] array;
    public final static int X = 0, Y = 1, Z = 2, W = 3, V = 4;

    public Coordinates(int dimensions){
        this.array = new double[dimensions];
        fill(0);
    }

    public Coordinates set(int axis, double value){
        if(!correct(axis)) throw new IndexOutOfBoundsException();
        array[axis] = value;
        return this;
    }

    public double get(int axis){
        if(!correct(axis)) throw new IndexOutOfBoundsException();
        return array[axis];
    }

    public Coordinates addEl(int axis, double value){
        if(!correct(axis)) throw new IndexOutOfBoundsException();
        array[axis] += value;
        return this;
    }

    public Coordinates multiplyEl(int axis, double value){
        if(!correct(axis)) throw new IndexOutOfBoundsException();
        array[axis] *= value;
        return this;
    }

    public Coordinates powEl(int axis, double value){
        if(!correct(axis)) throw new IndexOutOfBoundsException();
        array[axis] = Math.pow(array[axis], value);
        return this;
    }

    public Coordinates addAll(double term){
        for(int i = 0; i < array.length; i++){
            addEl(i, term);
        }
        return this;
    }

    public Coordinates multiplyAll(double multiplier){
        for(int i = 0; i < array.length; i++){
            multiplyEl(i, multiplier);
        }
        return this;
    }

    public Coordinates powAll(double value){
        for(int i = 0; i < array.length; i++){
            powEl(i, value);
        }
        return this;
    }

    public Coordinates addDepth(double ... terms){
        int elems = Math.min(terms.length, array.length);
        for(int i = 0; i < elems; i++){
            addEl(i, terms[i]);
        }
        return this;
    }

    public Coordinates multiplyDepth(double ... multipliers){
        int elems = Math.min(multipliers.length, array.length);
        for(int i = 0; i < elems; i++){
            multiplyEl(i, multipliers[i]);
        }
        return this;
    }

    public Coordinates powDepth(double ... pows){
        int elems = Math.min(pows.length, array.length);
        for(int i = 0; i < elems; i++){
            powEl(i, pows[i]);
        }
        return this;
    }

    public Coordinates fill(double i){
        Arrays.fill(array, i);
        return this;
    }

    @Override
    public Coordinates clone() throws CloneNotSupportedException {
        Coordinates c = (Coordinates) super.clone();
        System.arraycopy(array, 0, c.array, 0, array.length);
        return c;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Coordinates other)) return false;
        if(other.hashCode() != this.hashCode()) return false;
        return true;
    }

    protected boolean correct(int axis) {
        return axis >= 0 && axis < array.length;
    }
}
