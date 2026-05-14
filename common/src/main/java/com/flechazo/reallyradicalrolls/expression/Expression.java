package com.flechazo.reallyradicalrolls.expression;

import java.util.Map;

public interface Expression {
    double eval(Map<String, Double> vars);
}
