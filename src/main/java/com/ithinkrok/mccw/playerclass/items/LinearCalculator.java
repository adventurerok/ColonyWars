package com.ithinkrok.mccw.playerclass.items;

/**
 * Created by paul on 18/11/15.
 *
 * Calculates a number based on y=mx+c, where x is the player level.
 */
public class LinearCalculator implements Calculator {

    private double levelMultiplier;
    private double baseValue;

    public LinearCalculator(double baseValue, double levelMultiplier) {
        this.baseValue = baseValue;
        this.levelMultiplier = levelMultiplier;
    }

    @Override
    public double calculate(int playerLevel) {
        return baseValue + playerLevel * levelMultiplier;
    }
}
