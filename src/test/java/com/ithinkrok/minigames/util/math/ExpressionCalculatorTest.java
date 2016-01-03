package com.ithinkrok.minigames.util.math;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by paul on 03/01/16.
 */
@RunWith(JUnitParamsRunner.class)
public class ExpressionCalculatorTest {

    @Test
    @Parameters({"64.3,64.3", "2+3,5","2^15,32768","3+2*(7/2+4^3)-75,63", "8/(-4),-2"})
    public void expressionCalculatorShouldEvaluateSumsCorrectly(String sum, double result) {
        ExpressionCalculator calculator = new ExpressionCalculator(sum);

        assertThat(calculator.calculate(null)).isEqualTo(result);
    }
}