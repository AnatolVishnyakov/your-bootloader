package com.github.yourbootloader.yt.extractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSInterpreterTest {

    private JSInterpreter jsi;

    @Test
    void testBasic() {
        jsi = new JSInterpreter("function x(){;}");
        assertNull(jsi.callFunction("x", null));

        jsi = new JSInterpreter("function x3(){return 42;}");
        assertEquals(42, jsi.callFunction("x3", null));

        jsi = new JSInterpreter("var x5 = function(){return 42;}");
        assertEquals(42, jsi.callFunction("x5", null));
    }

    @Test
    void testCalc() {
        jsi = new JSInterpreter("function x4(a){return 2*a+1;}");
        assertEquals(7, jsi.callFunction("x4", 3));
    }

    @Test
    void testEmptyReturn() {
        jsi = new JSInterpreter("function f(){return; y()}");
        assertNull(jsi.callFunction("f", null));
    }

    @Test
    void testMoreSpace() {
        jsi = new JSInterpreter("function x (a) { return 2 * a + 1 ; }");
        assertEquals(7, jsi.callFunction("x", 3));

        jsi = new JSInterpreter("function f () { x =  2  ; return x; }");
        assertEquals(2, jsi.callFunction("f", null));
    }

    @Test
    void testStrangeChars() {
        jsi = new JSInterpreter("function $_xY1 ($_axY1) { var $_axY2 = $_axY1 + 1; return $_axY2; }");
        assertEquals(21, jsi.callFunction("$_xY1", 20));
    }
}