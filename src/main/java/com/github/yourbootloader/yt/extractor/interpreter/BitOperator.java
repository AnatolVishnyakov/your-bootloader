package com.github.yourbootloader.yt.extractor.interpreter;

import lombok.Getter;

@Getter
public enum BitOperator {
    OR("\\|") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a | b;
        }
    },
    XOR("^") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a ^ b;
        }
    },
    AND("&") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a & b;
        }
    },
    SHIFT_RIGHT(">>") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a >> b;
        }
    },
    SHIFT_LEFT("<<") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a << b;
        }
    },
    SUBTRACT("-") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a - b;
        }
    },
    ADD("\\+") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a + b;
        }
    },
    MOD("%") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a % b;
        }
    },
    DIVIDE("/") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a / b;
        }
    },
    MULTIPLY("\\*") {
        @Override
        public Integer calculate(Integer a, Integer b) {
            return a * b;
        }
    };

    private final String op;

    BitOperator(String op) {
        this.op = op;
    }

    public String calculate(String a, String b) {
        return String.valueOf(calculate(Integer.parseInt(a), Integer.parseInt(b)));
    }

    public abstract Integer calculate(Integer a, Integer b);
}
