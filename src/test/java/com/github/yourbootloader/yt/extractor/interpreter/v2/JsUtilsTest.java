package com.github.yourbootloader.yt.extractor.interpreter.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsUtilsTest {
    @Test
    void testOperators() {
        assertEquals(4, JsUtils.OPERATORS.get(">>").apply(9, 1));
        assertEquals(2, JsUtils.OPERATORS.get(">>").apply(9, 2));
        assertEquals(18, JsUtils.OPERATORS.get("<<").apply(9, 1));
        assertEquals(36, JsUtils.OPERATORS.get("<<").apply(9, 2));
        assertEquals(19, JsUtils.OPERATORS.get("+").apply(4, 15));
        assertEquals(-11, JsUtils.OPERATORS.get("-").apply(4, 15));
        assertEquals(60, JsUtils.OPERATORS.get("*").apply(4, 15));
        assertEquals(4, JsUtils.OPERATORS.get("%").apply(4, 15));
        assertEquals(4, JsUtils.OPERATORS.get("/").apply(16, 4));
        assertEquals(8, JsUtils.OPERATORS.get("**").apply(2, 3));
    }
}