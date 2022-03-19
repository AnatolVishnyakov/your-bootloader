package com.github.yourbootloader.yt.extractor.interpreter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование двоичных операторов")
class BitOperatorTest {

    @Test
    void or() {
        assertThat(BitOperator.OR.calculate("1", "2"))
                .isEqualTo(new BigInteger("1").or(new BigInteger("2")).toString());
        assertThat(BitOperator.OR.calculate("1", "2"))
                .isEqualTo("3");
    }

    @Test
    void xor() {
        assertThat(BitOperator.XOR.calculate("10", "2"))
                .isEqualTo(new BigInteger("10").xor(new BigInteger("2")).toString());
        assertThat(BitOperator.XOR.calculate("10", "2"))
                .isEqualTo("8");
    }

    @Test
    void and() {
        assertThat(BitOperator.AND.calculate("14", "5"))
                .isEqualTo(new BigInteger("14").and(new BigInteger("5")).toString());
        assertThat(BitOperator.AND.calculate("14", "5"))
                .isEqualTo("4");
    }

    @Test
    void shiftRight() {
        assertThat(BitOperator.SHIFT_RIGHT.calculate("134", "2"))
                .isEqualTo(new BigInteger("134").shiftRight(new BigInteger("2").intValue()).toString());
        assertThat(BitOperator.SHIFT_RIGHT.calculate("134", "2"))
                .isEqualTo("33");
    }

    @Test
    void shiftLeft() {
        assertThat(BitOperator.SHIFT_LEFT.calculate("134", "5"))
                .isEqualTo(new BigInteger("134").shiftLeft(new BigInteger("5").intValue()).toString());
        assertThat(BitOperator.SHIFT_LEFT.calculate("134", "5"))
                .isEqualTo("4288");
    }

    @Test
    void subtract() {
        assertThat(BitOperator.SUBTRACT.calculate("23", "12"))
                .isEqualTo(new BigInteger("23").subtract(new BigInteger("12")).toString());
        assertThat(BitOperator.SUBTRACT.calculate("23", "12"))
                .isEqualTo("11");
    }

    @Test
    void add() {
        assertThat(BitOperator.ADD.calculate("23", "12"))
                .isEqualTo(new BigInteger("23").add(new BigInteger("12")).toString());
        assertThat(BitOperator.ADD.calculate("23", "12"))
                .isEqualTo("35");
    }

    @Test
    void mod() {
        assertThat(BitOperator.MOD.calculate("13", "2"))
                .isEqualTo(new BigInteger("13").mod(new BigInteger("2")).toString());
        assertThat(BitOperator.MOD.calculate("13", "2"))
                .isEqualTo("1");
    }

    @Test
    void divide() {
        assertThat(BitOperator.DIVIDE.calculate("22", "2"))
                .isEqualTo(new BigInteger("22").divide(new BigInteger("2")).toString());
        assertThat(BitOperator.DIVIDE.calculate("22", "2"))
                .isEqualTo("11");
    }

    @Test
    void multiply() {
        assertThat(BitOperator.MULTIPLY.calculate("22", "2"))
                .isEqualTo(new BigInteger("22").multiply(new BigInteger("2")).toString());
        assertThat(BitOperator.MULTIPLY.calculate("22", "2"))
                .isEqualTo("44");
    }
}