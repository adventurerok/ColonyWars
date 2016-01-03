package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

/**
 * Created by paul on 03/01/16.
 */
public class NumberExpression implements Expression {

    private final double number;

    public NumberExpression(double number) {
        this.number = number;
    }

    @Override
    public double calculate(Variables variables) {
        return number;
    }

    @Override
    public boolean isStatic() {
        return true;
    }
}
