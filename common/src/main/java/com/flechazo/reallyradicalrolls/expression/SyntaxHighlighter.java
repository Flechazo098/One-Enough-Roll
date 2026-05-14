package com.flechazo.reallyradicalrolls.expression;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class SyntaxHighlighter {
    public static Component highlightText(String text) {
        MutableComponent formattedText = Component.literal("");
        var ctx = new SyntaxHighlightContext(text);

        while (ctx.getCurrent() != (char) 0) {
            if (ctx.getCurrent() == '$') {
                formattedText.append(String.valueOf(ctx.getCurrent()));
                ctx.advance();
                while (isLetter(ctx.getCurrent()) || ctx.getCurrent() == '_') {
                    formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Variable));
                    ctx.advance();
                }
            } else if (ctx.getCurrent() == '-' || ctx.getCurrent() == '+') {
                if (Character.isDigit(ctx.peek()) && ctx.prevTokenWasOperator()) {
                    formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Number));
                    ctx.advance();
                } else if (isLetter(ctx.peek()) && ctx.prevTokenWasOperator()) {
                    formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Function));
                    ctx.advance();
                } else {
                    formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Operator));
                    ctx.setPrevTokenOperator(true);
                    ctx.advance();
                }
            } else if (Character.isDigit(ctx.getCurrent()) || ctx.getCurrent() == '.') {
                formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Number));
                ctx.setPrevTokenOperator(false);
                ctx.advance();
            } else if (isLetter(ctx.getCurrent())) {
                StringBuilder builder = new StringBuilder();
                while (isLetter(ctx.getCurrent()) || ctx.getCurrent() == '_') {
                    builder.append(ctx.getCurrent());
                    ctx.advance();
                }
                String builtResult = builder.toString();
                if (isKeyword(builtResult) && ctx.getCurrent() == '(') {
                    formattedText.append(formatText(builtResult, SyntaxType.Function));
                } else if (isConstant(builtResult)) {
                    formattedText.append(formatText(builtResult, SyntaxType.Constant));
                } else {
                    formattedText.append(formatText(builtResult, SyntaxType.Error));
                }
                ctx.setPrevTokenOperator(false);
            } else if (isOperator(ctx.getCurrent())) {
                formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Operator));
                ctx.setPrevTokenOperator(true);
                ctx.advance();
            } else if (isScope(ctx.getCurrent())) {
                formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Scope));
                ctx.setPrevTokenOperator(ctx.getCurrent() == '(' || ctx.getCurrent() == ',');
                ctx.advance();
            } else if (Character.isWhitespace(ctx.getCurrent())) {
                formattedText.append(String.valueOf(ctx.getCurrent()));
                ctx.advance();
            } else {
                formattedText.append(formatText(ctx.getCurrent(), SyntaxType.Error));
                ctx.setPrevTokenOperator(false);
                ctx.advance();
            }
        }
        return formattedText;
    }

    public static boolean isConstant(String str) {
        return switch (str) {
            case "PI", "E", "TO_RAD", "TO_DEG" -> true;
            default -> false;
        };
    }

    public static boolean isKeyword(String str) {
        return switch (str) {
            case "sqrt", "sin", "cos", "tan", "asin", "acos",
                 "atan", "abs", "exp", "ceil", "floor", "log",
                 "round", "randint", "min", "max" -> true;
            default -> false;
        };
    }

    public static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    public static boolean isScope(char c) {
        return c == ',' || c == '(' || c == ')';
    }

    public static MutableComponent formatText(char ch, SyntaxType type) {
        return formatText(String.valueOf(ch), type);
    }

    public static MutableComponent formatText(String str, SyntaxType type) {
        return switch (type) {
            case Variable -> Component.literal(str).withStyle(ChatFormatting.GREEN);
            case Operator -> Component.literal(str).withStyle(ChatFormatting.LIGHT_PURPLE);
            case Error -> Component.literal(str).withStyle(ChatFormatting.RED);
            case Number -> Component.literal(str).withStyle(ChatFormatting.AQUA);
            case Function -> Component.literal(str).withStyle(ChatFormatting.YELLOW);
            case Constant -> Component.literal(str).setStyle(Style.EMPTY.withColor(0xFFA500));
            case Scope -> Component.literal(str);
        };
    }

    static class SyntaxHighlightContext {
        private int position;
        private boolean prevTokenOperator;
        private final String rawText;

        SyntaxHighlightContext(String raw) {
            this.rawText = raw;
            this.prevTokenOperator = true; // start of expression counts as after an operator
        }

        void advance() { position++; }

        void setPrevTokenOperator(boolean v) { prevTokenOperator = v; }

        boolean prevTokenWasOperator() { return prevTokenOperator; }

        char peek() { return getByIndex(position + 1); }

        char getCurrent() { return getByIndex(position); }

        char getByIndex(int i) {
            return i >= rawText.length() ? (char) 0 : rawText.charAt(i);
        }
    }

    public static void renderHighlighted(GuiGraphicsExtractor g, Font font, String raw, int x, int y, int defaultColor) {
        g.text(font, highlightText(raw), x, y, defaultColor, false);
    }

    public enum SyntaxType {
        Variable, Operator, Error, Scope, Function, Number, Constant
    }
}
