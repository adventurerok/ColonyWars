package com.ithinkrok.oldmccw.playerclass.items;

/**
 * Created by paul on 18/11/15.
 * <p>
 * "Calculates" a value by looking it up in an array.
 */
public class ArrayCalculator implements Calculator {

    private double[] returnValues;

    public ArrayCalculator(double... returnValues) {
        this.returnValues = returnValues;
    }

    @Override
    public double calculate(int playerLevel) {
        return returnValues[playerLevel];
    }
}
