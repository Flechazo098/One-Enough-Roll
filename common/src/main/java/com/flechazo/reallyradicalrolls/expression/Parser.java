package com.flechazo.reallyradicalrolls.expression;

public abstract class Parser {
    protected final String string;
    protected int pos = -1;
    protected char ch;

    protected Parser(String string) {
        this.string = string;
    }

    protected void nextChar() {
        this.ch = ++this.pos < this.string.length() ? this.string.charAt(this.pos) : 0;
    }

    protected boolean weat(char charToEat) {
        while (Character.isWhitespace(this.ch)) this.nextChar();
        return this.eat(charToEat);
    }

    protected boolean eat(char charToEat) {
        if (this.ch == charToEat) {
            this.nextChar();
            return true;
        }
        return false;
    }

    protected boolean isVariableChar() {
        return (this.ch >= 'a' && this.ch <= 'z') || (this.ch >= 'A' && this.ch <= 'Z') || this.ch == '-' || this.ch == '_' || this.ch == ':';
    }

    public String getString() {
        return this.string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        Parser parser = (Parser) o;

        return this.string.equals(parser.string);
    }

    @Override
    public int hashCode() {
        return this.string.hashCode();
    }
}
