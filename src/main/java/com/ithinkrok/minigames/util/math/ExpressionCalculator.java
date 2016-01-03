package com.ithinkrok.minigames.util.math;

import com.ithinkrok.minigames.util.math.expression.Expression;
import com.ithinkrok.minigames.util.math.expression.NumberExpression;
import com.ithinkrok.minigames.util.math.expression.OperatorExpression;
import com.ithinkrok.minigames.util.math.expression.VariableExpression;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.*;

/**
 * Created by paul on 03/01/16.
 */
public class ExpressionCalculator implements Calculator {

    private static final List<String> orderedOperators = new ArrayList<>();
    private static final Map<String, OperatorExpression.Operator> operatorMap = new HashMap<>();

    static {
        orderedOperators.add("^");
        orderedOperators.add("/");
        orderedOperators.add("*");
        orderedOperators.add("+");
        orderedOperators.add("-");

        operatorMap.put("^", numbers -> Math.pow(numbers[0], numbers[1]));
        operatorMap.put("/", numbers -> numbers[0] / numbers[1]);
        operatorMap.put("*", numbers -> numbers[0] * numbers[1]);
        operatorMap.put("+", numbers -> numbers[0] + numbers[1]);

        operatorMap.put("-", numbers -> {
            if(numbers.length == 1) return -numbers[0];
            else return numbers[0] - numbers[1];
        });
    }

    private Expression expression;

    public ExpressionCalculator(String expression) {
        List<String> tokens;

        try {
            tokens = tokenize(expression);
        } catch (IOException e) {
            throw new RuntimeException("Failed to tokenize expression: " + expression, e);
        }

        tokens = toPostfixNotation(tokens);

        this.expression = parsePostfixNotation(tokens);
    }

    private static Expression parsePostfixNotation(List<String> tokens) {
        LinkedList<Expression> stack = new LinkedList<>();

        for(String token : tokens) {
            if(isNumber(token)) stack.add(new NumberExpression(Double.parseDouble(token)));
            else if(!orderedOperators.contains(token)) stack.add(new VariableExpression(token));
            else {
                Expression[] expressions = new Expression[Math.min(2, stack.size())];
                for(int index = expressions.length - 1; index >= 0; --index) {
                    expressions[index] = stack.removeLast();
                }

                stack.add(new OperatorExpression(operatorMap.get(token), expressions));
            }
        }

        if(stack.size() != 1) throw new RuntimeException("Bad expression: " + tokens);


        Expression expression = stack.getFirst();
        if(expression.isStatic()) expression = new NumberExpression(expression.calculate(null));
        return expression;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static List<String> toPostfixNotation(List<String> tokens) {
        LinkedList<String> tokenStack = new LinkedList<>();
        List<String> output = new ArrayList<>(tokens.size());

        for(String token : tokens) {
            if(isNumber(token)){
                output.add(token);
                continue;
            }

            int index = orderedOperators.indexOf(token);
            if(index != -1) {
                while(tokenStack.size() > 0 && lowerPrecedence(index, tokenStack.getLast())) {
                    output.add(tokenStack.removeLast());
                }
                tokenStack.add(token);
                continue;
            }

            if("(".equals(token)) {
                tokenStack.add(token);
                continue;
            }

            if(")".equals(token)) {
                while(tokenStack.size() > 0 && !"(".equals(tokenStack.getLast())){
                    output.add(tokenStack.removeLast());
                }
                if(tokenStack.size() == 0) throw new RuntimeException("Mismatched brackets: " + tokens);
                tokenStack.removeLast();
                //function token on top of stack (if implemented in future) should also be removed
                continue;
            }

            output.add(token);
        }


        while(tokenStack.size() > 0) {
            String token = tokenStack.removeLast();
            if("(".equals(token)) throw new RuntimeException("Mismatched brackets: " + tokens);
            output.add(token);
        }


        return output;
    }

    private static boolean lowerPrecedence(int o1Index, String o2) {
        int o2Index = orderedOperators.indexOf(o2);

        if(o2Index == -1) return false;

        return o1Index >= o2Index;
    }

    private static List<String> tokenize(String s) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));

        tokenizer.ordinaryChar('/');

        List<String> tokBuf = new ArrayList<>();
        boolean wasNotOperator = false;

        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            switch(tokenizer.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    if(wasNotOperator) tokBuf.add("+");
                    tokBuf.add(String.valueOf(tokenizer.nval));
                    wasNotOperator = true;
                    break;
                default:  // operator or word
                    String token;
                    if(tokenizer.ttype == StreamTokenizer.TT_WORD) token = tokenizer.sval;
                    else token = String.valueOf((char) tokenizer.ttype);

                    boolean operator = orderedOperators.contains(token) || "(".equals(token);
                    if(!operator && wasNotOperator && !")".equals(token)) tokBuf.add("+");

                    tokBuf.add(token);
                    wasNotOperator = !operator;
            }

        }
        return tokBuf;
    }

    @Override
    public double calculate(Variables variables) {
        return expression.calculate(variables);
    }
}
