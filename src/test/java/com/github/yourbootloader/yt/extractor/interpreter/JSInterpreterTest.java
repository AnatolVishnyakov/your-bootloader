package com.github.yourbootloader.yt.extractor.interpreter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void testOperators() {
        jsi = new JSInterpreter("function f(){return 1 << 5;}");
        assertEquals(32, jsi.callFunction("f", null));

        jsi = new JSInterpreter("function f(){return 19 & 21;}");
        assertEquals(17, jsi.callFunction("f", null));

        jsi = new JSInterpreter("function f(){return 11 >> 2;}");
        assertEquals(2, jsi.callFunction("f", null));
    }

    @Test
    void testArrayAccess() {
        jsi = new JSInterpreter("function f(){var x = [1,2,3]; x[0] = 4; x[0] = 5; x[2] = 7; return x;}");
        assertEquals(Arrays.asList(5, 2, 7), jsi.callFunction("f", null));
    }

    @Test
    void testParens() {
        jsi = new JSInterpreter("function f(){return (1) + (2) * ((( (( (((((3)))))) )) ));}");
        assertEquals(7, jsi.callFunction("f", null));

        jsi = new JSInterpreter("function f(){return (1 + 2) * 3;}");
        assertEquals(9, jsi.callFunction("f", null));
    }

    @Test
    void testAssignments() {
        jsi = new JSInterpreter("function f(){var x = 20; x = 30 + 1; return x;}");
        assertEquals(31, jsi.callFunction("f", null));

        jsi = new JSInterpreter("function f(){var x = 20; x += 30 + 1; return x;}");
        assertEquals(51, jsi.callFunction("f", null));

        jsi = new JSInterpreter("function f(){var x = 20; x -= 30 + 1; return x;}");
        assertEquals(-11, jsi.callFunction("f", null));
    }

    @Test
    @Disabled
    void testComments() {
        // TODO Not implemented
    }

    @Test
    void testPrecedence() {
        jsi = new JSInterpreter(
                "function x() {" +
                        "    var a = [10, 20, 30, 40, 50];" +
                        "    var b = 6;" +
                        "    a[0]=a[b%a.length];" +
                        "    return a;" +
                        "}"
        );
        assertEquals(Arrays.asList(20, 20, 30, 40, 50), jsi.callFunction("x", null));
    }

    @Test
    void testCall() {
        jsi = new JSInterpreter(
                " function x() { return 2; }" +
                        " function y(a) { return x() + a; }" +
                        " function z() { return y(3); }"
        );
        assertEquals(5, jsi.callFunction("z", null));
    }

    @Test
    void testForLoop() {
        jsi = new JSInterpreter("function x() { a=0; for (i=0; i-10; i = i + 1) {a++} a }");
        assertEquals(10, jsi.callFunction("x", null));
    }

    @Test
    void testSwitchDefault() {
        jsi = new JSInterpreter("" +
                "        function x(f) { switch(f){" +
                "            case 2: f+=2;" +
                "            default: f-=1;" +
                "            case 5:" +
                "            case 6: f+=6;" +
                "            case 0: break;" +
                "            case 1: f+=1;" +
                "        } return f }");
        assertEquals(2, jsi.callFunction("x", 1));
        assertEquals(11, jsi.callFunction("x", 5));
        assertEquals(14, jsi.callFunction("x", 9));
    }

    @Test
    void testTry() {
        jsi = new JSInterpreter("function x() { try{return 10} catch(e){return 5} }");
        assertEquals(10, jsi.callFunction("x", null));
    }

    @Test
    void testForLoopContinue() {
        jsi = new JSInterpreter("function x() { a=0; for (i=0; i-10; i++) { continue; a++ } a }");
        assertEquals(0, jsi.callFunction("x", null));
    }

    @Test
    void testForLoopBreak() {
        jsi = new JSInterpreter("function x() { a=0; for (i=0; i-10; i++) { break; a++ } a }");
        assertEquals(0, jsi.callFunction("x", null));
    }

    @Test
    void testLiteralList() {
        jsi = new JSInterpreter("function x() { [1, 2, \"asdf\", [5, 6, 7]][3] }");
        assertEquals(Arrays.asList(5, 6, 7), jsi.callFunction("x", null));
    }

    @Test
    void testComma() {
        jsi = new JSInterpreter("function x() { a=5; a -= 1, a+=3; return a }");
        assertEquals(7, jsi.callFunction("x", null));
    }
}