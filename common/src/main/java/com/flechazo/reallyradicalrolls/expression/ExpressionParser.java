package com.flechazo.reallyradicalrolls.expression;

import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ExpressionParser extends Parser {
    public static final Codec<ExpressionParser> CODEC = Codec.STRING.xmap(ExpressionParser::new, Parser::getString);

    private static final int MAX_RECURSION_DEPTH = 256;

    private Expression compiled;
    private RuntimeException error;

    public static Expression parse(String string) {
        return new ExpressionParser(string).build();
    }

    public ExpressionParser(String string) {
        super(string);
    }

    public Expression getCompiled() {
        if (this.compiled == null && this.error == null) {
            try {
                return this.build();
            } catch (RuntimeException e) {
                this.error = e;
                return null;
            }
        }
        return this.compiled;
    }

    public Expression getCompiledOrDefaulting(double defaultValue) {
        return this.hasError() ? vars -> defaultValue : this.getCompiled();
    }

    public RuntimeException getError() {
        this.getCompiled();
        return this.error;
    }

    public boolean hasError() {
        this.getCompiled();
        return this.error != null;
    }

    public Expression build() {
        this.nextChar();
        Expression x = this.parseExpression(0);
        if (this.pos < this.string.length())
            throw new RuntimeException("Unexpected character '" + this.ch + "' at position " + this.pos);
        this.compiled = x;
        return x;
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor
    private Expression parseExpression(int depth) {
        if (depth > MAX_RECURSION_DEPTH) throw new RuntimeException("Exceeded max recursion depth");
        Expression x = this.parseTerm(depth + 1);
        while (true) {
            if (this.weat('+')) {
                Expression a = x;
                Expression b = this.parseTerm(depth + 1);
                x = vars -> a.eval(vars) + b.eval(vars);
            } else if (this.weat('-')) {
                Expression a = x;
                Expression b = this.parseTerm(depth + 1);
                x = vars -> a.eval(vars) - b.eval(vars);
            } else return x;
        }
    }

    private Expression parseTerm(int depth) {
        if (depth > MAX_RECURSION_DEPTH) throw new RuntimeException("Exceeded max recursion depth");
        Expression x = this.parseFactor(depth + 1);
        while (true) {
            if (this.weat('*')) {
                Expression a = x;
                Expression b = this.parseFactor(depth + 1);
                x = vars -> a.eval(vars) * b.eval(vars);
            } else if (this.weat('/')) {
                Expression a = x;
                Expression b = this.parseFactor(depth + 1);
                x = vars -> a.eval(vars) / b.eval(vars);
            } else return x;
        }
    }

    private Expression parseFactor(int depth) {
        if (depth > MAX_RECURSION_DEPTH) throw new RuntimeException("Exceeded max recursion depth");
        if (this.weat('+')) {
            Expression a = this.parseFactor(depth + 1);
            return vars -> +a.eval(vars);
        }
        if (this.weat('-')) {
            Expression a = this.parseFactor(depth + 1);
            return vars -> -a.eval(vars);
        }
        Expression x;
        int startPos = this.pos;
        if (this.weat('(')) {
            x = this.parseExpression(depth + 1);
            if (!this.weat(')')) throw new RuntimeException("Missing ')' at position " + this.pos);
        } else if (this.ch >= '0' && this.ch <= '9' || this.ch == '.') { // number literals
            boolean hasDigit = false;
            while (this.ch >= '0' && this.ch <= '9' || this.ch == '.') {
                if (this.ch >= '0' && this.ch <= '9') hasDigit = true;
                this.nextChar();
            }
            if (!hasDigit) throw new RuntimeException("Invalid number literal at position " + startPos);
            double a = Double.parseDouble(this.string.substring(startPos, this.pos));
            x = vars -> a;
        } else if (this.isVariableChar()) {
            while (this.isVariableChar()) this.nextChar();
            String name = this.string.substring(startPos, this.pos);
            List<Expression> args = new ArrayList<>();
            if (this.weat('(')) { // functions
                do {
                    args.add(this.parseExpression(depth + 1));
                } while (this.weat(','));
                if (!this.weat(')')) throw new RuntimeException("Missing ')' after argument to '" + name + "'");
                x = switch (args.size()) {
                    case 1 -> {
                        Expression a = args.get(0);
                        yield switch (name) {
                            case "sqrt" -> vars -> Math.sqrt(a.eval(vars));
                            case "sin" -> vars -> Math.sin(a.eval(vars));
                            case "cos" -> vars -> Math.cos(a.eval(vars));
                            case "tan" -> vars -> Math.tan(a.eval(vars));
                            case "asin" -> vars -> Math.asin(a.eval(vars));
                            case "acos" -> vars -> Math.acos(a.eval(vars));
                            case "atan" -> vars -> Math.atan(a.eval(vars));
                            case "abs" -> vars -> Math.abs(a.eval(vars));
                            case "ceil" -> vars -> Math.ceil(a.eval(vars));
                            case "floor" -> vars -> Math.floor(a.eval(vars));
                            case "log" -> vars -> Math.log(a.eval(vars));
                            case "round" -> vars -> Math.round(a.eval(vars));
                            case "randint" -> vars -> {
                                int bound = (int) a.eval(vars);
                                if (bound <= 0) throw new RuntimeException("randint bound must be > 0, got " + bound);
                                return ThreadLocalRandom.current().nextInt(bound);
                            };
                            default -> throw new RuntimeException(
                                    "Unknown function '" + name + "' for 1 arg at position " + (this.pos - name.length()));
                        };
                    }
                    case 2 -> {
                        Expression a = args.get(0);
                        Expression b = args.get(1);
                        yield switch (name) {
                            case "min" -> vars -> Math.min(a.eval(vars), b.eval(vars));
                            case "max" -> vars -> Math.max(a.eval(vars), b.eval(vars));
                            case "randint" -> vars -> {
                                double av = a.eval(vars);
                                int range = (int) (b.eval(vars) - av);
                                if (range <= 0) throw new RuntimeException(
                                        "randint range must be > 0, got " + range);
                                return av + ThreadLocalRandom.current().nextInt(range);
                            };
                            default -> throw new RuntimeException(
                                    "Unknown function '" + name + "' for 2 args at position " + (this.pos - name.length()));
                        };
                    }
                    default -> throw new RuntimeException(
                            "Unknown function '" + name + "' for " + args.size() + " args at position "
                                    + (this.pos - name.length()));
                };
            } else { // constants
                double a = switch (name) {
                    case "PI" -> Math.PI;
                    case "E" -> Math.E;
                    case "TO_RAD" -> Math.PI / 180;
                    case "TO_DEG" -> 180 / Math.PI;
                    default -> throw new RuntimeException(
                            "Unknown constant '" + name + "' at position " + (this.pos - name.length()));
                };
                x = vars -> a;
            }
        } else if (this.weat('$')) {
            while (this.isVariableChar()) this.nextChar();
            String variable = this.string.substring(startPos + 1, this.pos);
            x = vars -> {
                Double value = vars.get(variable);
                if (value == null)
                    throw new RuntimeException("Variable '$" + variable + "' is not defined");
                return value;
            };
        } else {
            throw new RuntimeException("Unexpected '" + this.ch + "' at position " + this.pos);
        }
        if (this.weat('^')) {
            Expression a = x;
            Expression b = this.parseFactor(depth + 1);
            x = vars -> Math.pow(a.eval(vars), b.eval(vars));
        }
        return x;
    }
}
