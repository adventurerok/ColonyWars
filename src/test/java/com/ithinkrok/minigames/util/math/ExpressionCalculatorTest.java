package com.ithinkrok.minigames.util.math;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.offset;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Created by paul on 03/01/16.
 */
@RunWith(DataProviderRunner.class)
public class ExpressionCalculatorTest {

    @Test
    @DataProvider({"64.3,64.3", "2+3,5","2^15,32768"})
    public void shouldHandleBasicSumsCorrectly(String sum, double result) {
        testValues(sum, null, result);
    }

    @Test
    @DataProvider({"3+2*(7/2+4^3)-75,63", "8/(-4),-2"})
    public void shouldHandleMoreAdvancedSumsCorrectly(String sum, double result) {
        testValues(sum, null, result);
    }

    @Test
    @DataProvider({"sin(4),-0.7568024953", "(3)-sin(4),3.75680249531", "abs(3*7-200),179"})
    public void shouldHandleFunctionsCorrectly(String sum, double result) {
        testValues(sum, null, result);
    }

    @Test
    @DataProvider(value = {"min(4,7,1,9);1"}, splitBy = ";")
    public void shouldHandleMinCorrectly(String sum, double result) {
        testValues(sum, null, result);
    }

    @Test
    @DataProvider({"x+y,4,5,9"})
    public void shouldHandleVariablesCorrectly(String sum, double x, double y, double result) {
        Variables variables = mock(Variables.class);
        doThrow(new RuntimeException("Should only be asking for x and y")).when(variables).getVariable(any());
        doReturn(x).when(variables).getVariable("x");
        doReturn(y).when(variables).getVariable("y");

        testValues(sum, variables, result);
    }

    private void testValues(String sum, Variables variables, double expected) {
        ExpressionCalculator calculator = new ExpressionCalculator(sum);

        double actual = calculator.calculate(variables);

        assertThat(actual).isEqualTo(expected, offset(1e-5));
    }
}