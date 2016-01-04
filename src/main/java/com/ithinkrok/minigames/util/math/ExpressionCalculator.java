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

    private static final Map<String, OpInfo> opMap = new HashMap<>();

    public static boolean isOperatorOrFunction(String check) {
        return opMap.containsKey(check);
    }

    static {
        opMap.put("^", new OpInfo(numbers -> Math.pow(numbers[0], numbers[1]), false, false, 10, 2, 2));
        opMap.put("/", new OpInfo(numbers -> numbers[0] / numbers[1], false, false, 20, 2, 2));
        opMap.put("*", new OpInfo(numbers -> numbers[0] * numbers[1], false, false, 20, 2, 2));
        opMap.put("%", new OpInfo(numbers -> numbers[0] % numbers[1], false, false, 20, 2, 2));
        opMap.put("+", new OpInfo(numbers -> numbers[0] + numbers[1], false, false, 30, 2, 2));
        opMap.put("-", new OpInfo(numbers -> numbers[0] - numbers[1], false, false, 30, 2, 2));
        opMap.put("~", new OpInfo(numbers -> -numbers[0], false, false, 40, 1, 1));

        opMap.put("sin", new OpInfo(numbers -> Math.sin(numbers[0]), true, false, 0, 1, 1));
        opMap.put("cos", new OpInfo(numbers -> Math.cos(numbers[0]), true, false, 0, 1, 1));
        opMap.put("tan", new OpInfo(numbers -> Math.tan(numbers[0]), true, false, 0, 1, 1));

        opMap.put("asin", new OpInfo(numbers -> Math.asin(numbers[0]), true, false, 0, 1, 1));
        opMap.put("acos", new OpInfo(numbers -> Math.acos(numbers[0]), true, false, 0, 1, 1));
        opMap.put("atan", new OpInfo(numbers -> Math.atan(numbers[0]), true, false, 0, 1, 1));
        opMap.put("atan2", new OpInfo(numbers -> Math.atan2(numbers[0], numbers[1]), true, false, 0, 2, 2));

        opMap.put("degrees", new OpInfo(numbers -> Math.toDegrees(numbers[0]), true, false, 0, 1, 1));
        opMap.put("radians", new OpInfo(numbers -> Math.toRadians(numbers[0]), true, false, 0, 1, 1));

        opMap.put("ln", new OpInfo(numbers -> Math.log(numbers[0]), true, false, 0, 1, 1));
        opMap.put("lg", new OpInfo(numbers -> Math.log(numbers[0]) * 1.44269504089, true, false, 0, 1, 1));
        opMap.put("log", new OpInfo(numbers -> Math.log10(numbers[0]), true, false, 0, 1, 1));
        opMap.put("exp", new OpInfo(numbers -> Math.exp(numbers[0]), true, false, 0, 1, 1));

        opMap.put("random", new OpInfo(numbers -> Math.random(), true, true, 0, 0, 0));

        opMap.put("abs", new OpInfo(numbers -> Math.abs(numbers[0]), true, false, 0, 1, 1));
        opMap.put("floor", new OpInfo(numbers -> Math.floor(numbers[0]), true, false, 0, 1, 1));
        opMap.put("ceil", new OpInfo(numbers -> Math.ceil(numbers[0]), true, false, 0, 1, 1));
        opMap.put("expression", new OpInfo(numbers -> numbers[0], true, false, 0, 1, 1));

        opMap.put("min", new OpInfo(numbers -> {
            double min = Double.POSITIVE_INFINITY;

            for(double num : numbers){
                if(num < min) min = num;
            }

            return min;
        }, true, false, 0, 1, Integer.MAX_VALUE));

        opMap.put("max", new OpInfo(numbers -> {
            double min = Double.NEGATIVE_INFINITY;

            for(double num : numbers){
                if(num > min) min = num;
            }

            return min;
        }, true, false, 0, 1, Integer.MAX_VALUE));

        opMap.put("array", new OpInfo(numbers ->  {
            int index = (int) (numbers[0] + 1);
            return numbers[index];
        }, true, false, 0, 2, Integer.MAX_VALUE));
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

    private static List<String> tokenize(String s) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));

        tokenizer.ordinaryChar('-');
        tokenizer.ordinaryChar('/');

        List<String> tokBuf = new ArrayList<>();
        boolean valueLast = false;

        while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            switch (tokenizer.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    tokBuf.add(String.valueOf(tokenizer.nval));
                    valueLast = true;
                    break;
                default:  // operator or word
                    String token;
                    if (tokenizer.ttype == StreamTokenizer.TT_WORD) token = tokenizer.sval;
                    else token = String.valueOf((char) tokenizer.ttype);

                    boolean operator = opMap.containsKey(token) || "(".equals(token) || ",".equals(token);

                    if(!"-".equals(token) || valueLast) tokBuf.add(token);
                    else tokBuf.add("~"); //Unary minus
                    valueLast = !operator;
            }

        }
        return tokBuf;
    }

    private static List<String> toPostfixNotation(List<String> tokens) {
        LinkedList<String> tokenStack = new LinkedList<>();
        List<String> output = new ArrayList<>(tokens.size());

        for (String token : tokens) {
            OpInfo opInfo = opMap.get(token);
            if (opInfo != null) {
                while (tokenStack.size() > 0 && lowerPrecedence(opInfo.precedence, tokenStack.getLast())) {
                    output.add(tokenStack.removeLast());
                }
                tokenStack.add(token);
            } else if (isNumber(token)) {
                output.add(token);
            } else if ("(".equals(token)) {
                if (tokenStack.size() > 0 && opMap.containsKey(tokenStack.getLast()) &&
                        opMap.get(tokenStack.getLast()).isFunction) output.add(token);
                tokenStack.add(token);
            } else if(",".equals(token)){
                while (!"(".equals(tokenStack.getLast())) {
                    output.add(tokenStack.removeLast());
                }
            } else if (")".equals(token)) {
                while (!"(".equals(tokenStack.getLast())) {
                    output.add(tokenStack.removeLast());
                }
                tokenStack.removeLast();

                if (tokenStack.size() > 0 && opMap.containsKey(tokenStack.getLast()) &&
                        opMap.get(tokenStack.getLast()).isFunction) output.add(tokenStack.removeLast());
            } else output.add(token);
        }


        while (tokenStack.size() > 0) {
            String token = tokenStack.removeLast();
            if ("(".equals(token)) throw new RuntimeException("Mismatched brackets: " + tokens);
            output.add(token);
        }


        return output;
    }

    private static Expression parsePostfixNotation(List<String> tokens) {
        LinkedList<Expression> stack = new LinkedList<>();

        for (String token : tokens) {
            if ("(".equals(token)) stack.add(null); //Use a null expression as the stack separator
            else if (isNumber(token)) stack.add(new NumberExpression(Double.parseDouble(token)));
            else if (!opMap.containsKey(token)) stack.add(new VariableExpression(token));
            else {
                OpInfo op = opMap.get(token);

                LinkedList<Expression> expressions = new LinkedList<>();

                int count;
                for (count = 0; !stack.isEmpty() && (op.isFunction || count < op.maxArguments); ++count) {
                    Expression expr = stack.removeLast();
                    if (expr == null) {
                        break;
                    }
                    expressions.addFirst(expr);
                }

                if (count > op.maxArguments) throw new RuntimeException("Too many arguments for function" + token);
                else if (count < op.minArguments)
                    throw new RuntimeException("Too few arguments for function: " + token);

                stack.add(new OperatorExpression(op.operator, op.isDynamic, expressions));
            }
        }

        //noinspection StatementWithEmptyBody
        while (stack.remove(null)) ; //Remove all null elements from the stack

        if (stack.size() != 1) throw new RuntimeException("Bad expression: " + tokens);


        Expression expression = stack.getFirst();
        if (expression.isStatic()) expression = new NumberExpression(expression.calculate(null));
        return expression;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean lowerPrecedence(int o1Index, String o2) {
        OpInfo o2Info = opMap.get(o2);

        if (o2Info == null) return false;

        return o1Index >= o2Info.precedence;
    }

    @Override
    public double calculate(Variables variables) {
        return expression.calculate(variables);
    }

    private static class OpInfo {
        private final OperatorExpression.Operator operator;
        private final boolean isFunction, isDynamic;
        private final int maxArguments;
        private final int minArguments;
        private final int precedence;

        public OpInfo(OperatorExpression.Operator operator, boolean isFunction, boolean isDynamic, int precedence, int minArguments,
                      int maxArguments) {
            this.operator = operator;
            this.isFunction = isFunction;
            this.isDynamic = isDynamic;
            this.maxArguments = maxArguments;
            this.minArguments = minArguments;
            this.precedence = precedence;
        }
    }
}
