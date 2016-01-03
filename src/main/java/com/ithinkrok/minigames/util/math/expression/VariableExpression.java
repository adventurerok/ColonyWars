package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

/**
 * Created by paul on 03/01/16.
 */
public class VariableExpression implements Expression {

    private final String variable;

    public VariableExpression(String variable) {
        this.variable = variable;
    }

    @Override
    public double calculate(Variables variables) {
        return variables.getVariable(variable);
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
