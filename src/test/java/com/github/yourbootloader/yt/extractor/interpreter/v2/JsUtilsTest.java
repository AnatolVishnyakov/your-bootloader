package com.github.yourbootloader.yt.extractor.interpreter.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(1, JsUtils.OPERATORS.get("/").apply(1000, (int) 1E3));
        assertEquals(8, JsUtils.OPERATORS.get("**").apply(2, 3));
    }

    @Test
    void testCompareOperators() {
        assertFalse(JsUtils.COMP_OPERATORS.get("===").apply(301529828, -9));
        assertTrue(JsUtils.COMP_OPERATORS.get("!==").apply(2147359943, (int) -1.0));
        assertTrue(JsUtils.COMP_OPERATORS.get("==").apply(1, 1));
        assertTrue(JsUtils.COMP_OPERATORS.get("!=").apply(1, 2));
        assertTrue(JsUtils.COMP_OPERATORS.get("<=").apply(2, 2));
        assertTrue(JsUtils.COMP_OPERATORS.get(">=").apply(2, 2));
        assertTrue(JsUtils.COMP_OPERATORS.get("<").apply(2, 3));
        assertTrue(JsUtils.COMP_OPERATORS.get(">").apply(3, 2));
    }

    @Test
    void testLogOperators() {
        assertEquals(11, JsUtils.LOG_OPERATORS.get("|").apply(3, 8));
        assertEquals(0, JsUtils.LOG_OPERATORS.get("^").apply(1, 1));
        assertEquals(3, JsUtils.LOG_OPERATORS.get("^").apply(1, 2));
        assertEquals(1, JsUtils.LOG_OPERATORS.get("^").apply(3, 2));
        assertEquals(1, JsUtils.LOG_OPERATORS.get("&").apply(1, 1));
        assertEquals(1, JsUtils.LOG_OPERATORS.get("&").apply(1, 3));
    }
}