package com.github.yourbootloader.yt.extractor.interpreter.v2;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class JsUtils {

    static final Map<String, BiFunction<Integer, Integer, Integer>> OPERATORS = Map.of(
            ">>", (a, b) -> a >> b,
            "<<", (a, b) -> a << b,
            "+", Integer::sum,
            "-", (a, b) -> a - b,
            "*", (a, b) -> a * b,
            "%", (a, b) -> a % b,
            "/", (a, b) -> a / b,
            "**", (a, b) -> (int) Math.pow((double) a, (double) b)
    );

    static final Map<String, BiFunction<Integer, Integer, Boolean>> COMP_OPERATORS = Map.of(
            "===", Objects::equals,
            "!==", (a, b) -> !Objects.equals(a, b),
            "==", Objects::equals,
            "!=", (a, b) -> !Objects.equals(a, b),
            "<=", (a, b) -> a <= b,
            ">=", (a, b) -> a >= b,
            "<", (a, b) -> a < b,
            ">", (a, b) -> a > b
    );

    static final Map<String, BiFunction<Integer, Integer, Integer>> LOG_OPERATORS = Map.of(
            "|", (a, b) -> a | b,
            "^", (a, b) -> a ^ b,
            "&", (a, b) -> a & b
    );

/*

_SC_OPERATORS = (
    ('?', None),
    ('??', None),
    ('||', None),
    ('&&', None),
)
    * */
}
