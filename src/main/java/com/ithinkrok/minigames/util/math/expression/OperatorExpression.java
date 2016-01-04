package com.ithinkrok.minigames.util.math.expression;

import com.ithinkrok.minigames.util.math.Variables;

import java.util.List;

/**
 * Created by paul on 03/01/16.
 */
public class OperatorExpression implements Expression {

    private final Expression[] subExpressions;
    private final Operator operator;
    private final boolean dynamic;

    public OperatorExpression(Operator operator, boolean dynamic, List<Expression> expressions) {
        this(operator, dynamic, toArray(expressions));
    }

    private static Expression[] toArray(List<Expression> expressions) {
        Expression[] array = new Expression[expressions.size()];
        return expressions.toArray(array);
    }

    public OperatorExpression(Operator operator, boolean dynamic, Expression... expressions) {
        this.dynamic = dynamic;
        subExpressions = expressions;

        for (int i = 0; i < subExpressions.length; ++i) {
            if (subExpressions[i].isStatic())
                subExpressions[i] = new NumberExpression(subExpressions[i].calculate(null));
        }

        this.operator = operator;
    }

    @Override
    public double calculate(Variables variables) {
        double[] numbers = new double[subExpressions.length];

        for(int index = 0; index < subExpressions.length; ++index) {
            numbers[index] = subExpressions[index].calculate(variables);
        }

        return operator.operate(numbers);
    }

    @Override
    public boolean isStatic() {
        if(dynamic) return false;

        for(Expression expression : subExpressions) {
            if(!expression.isStatic()) return false;
        }

        return true;
    }

    public interface Operator {
        double operate(double...numbers);
    }

}
