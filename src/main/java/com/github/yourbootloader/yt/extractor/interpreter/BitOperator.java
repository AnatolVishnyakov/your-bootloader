package com.github.yourbootloader.yt.extractor.interpreter;

import lombok.Getter;

import java.util.function.BiFunction;

@Getter
public enum BitOperator {
    OR("\\|", (a, b) -> a | b),
    XOR("^", (a, b) -> a ^ b),
    AND("&", (a, b) -> a & b),
    SHIFT_RIGHT(">>", (a, b) -> a >> b),
    SHIFT_LEFT("<<", (a, b) -> a << b),
    SUBTRACT("-", (a, b) -> a - b),
    ADD("\\+", Integer::sum),
    MOD("%", (a, b) -> a % b),
    DIVIDE("/", (a, b) -> a / b),
    MULTIPLY("\\*", (a, b) -> a * b);

    private final String operator;
    private final BiFunction<Integer, Integer, Integer> function;

    BitOperator(String operator, BiFunction<Integer, Integer, Integer> function) {
        this.operator = operator;
        this.function = function;
    }

    public String calculate(String a, String b) {
        return String.valueOf(function.apply(Integer.parseInt(a), Integer.parseInt(b)));
    }
}
