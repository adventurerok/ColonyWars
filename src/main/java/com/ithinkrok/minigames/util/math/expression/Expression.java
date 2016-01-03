package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

/**
 * Created by paul on 03/01/16.
 */
public interface Expression {

    double calculate(Variables variables);

    /**
     *
     * @return If calculate() will always return the same result regardless of the variables
     */
    boolean isStatic();
}
