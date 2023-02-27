package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.StatementResultDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JSInterpreterTest {

    @Test
    @SneakyThrows
    void test01() {
        Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
        Path jsContentPath = folderJsData.resolve("content-01.js");
        String jsContent = String.join("", Files.readAllLines(jsContentPath));
        JSInterpreter jsInterpreter = new JSInterpreter(jsContent);
        jsInterpreter.extractFunctionFromCode(Arrays.asList("a"));
    }

    @Nested
    class JSInterpreterSeparate {
        @Test
        void separate01() {
            JSInterpreter jsInterpreter = new JSInterpreter(null);
            assertEquals("1", jsInterpreter.separate("{", ",", 0, null));
        }

        @Test
        void separate02() {
            JSInterpreter jsInterpreter = new JSInterpreter(null);
            assertEquals("2", jsInterpreter.separate("{}", ",", 0, null));
        }

        @Test
        void separate03() {
            JSInterpreter jsInterpreter = new JSInterpreter(null);
            assertEquals("3", jsInterpreter.separate("\"", ",", 0, null));
        }

        @Test
        void separate04() {
            JSInterpreter jsInterpreter = new JSInterpreter(null);
            assertEquals("4", jsInterpreter.separate("/]", ",", 0, null));
        }

        @Test
        @SneakyThrows
        void separatedAtParen01() {
            Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
            Path jsContentPath = folderJsData.resolve("content-01.js");
            String jsContent = String.join("", Files.readAllLines(jsContentPath));
            JSInterpreter jsInterpreter = new JSInterpreter(jsContent);
            assertEquals(2, jsInterpreter.separateAtParen(jsContent, null).size());
        }
    }

    @Test
    @SneakyThrows
    void interpretStatement01() {
        Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
        Path jsContentPath = folderJsData.resolve("content-01.js");
        String jsContent = String.join("", Files.readAllLines(jsContentPath));
        JSInterpreter jsInterpreter = new JSInterpreter(jsContent);
        jsInterpreter.interpretStatement(jsContent, new HashMap<>(), 100);
    }

    @Test
    @SneakyThrows
    void foo() {
        Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
        Path jsContentPath = folderJsData.resolve("content-03.js");

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        engine.eval(Files.newBufferedReader(jsContentPath, StandardCharsets.UTF_8));

        Invocable inv = (Invocable) engine;
        Object result = inv.invokeFunction("gma", "0GPyza2qYoFsejcBO");
        System.out.println();
    }

    @Nested
    class InterpretStatement {
        @Test
        void checkVarConstLetPattern() {
            String stmt = "var b=a.split(\"\")";
            Matcher matcher = Pattern.compile(JSInterpreter.VAR_CONST_LET_PATTERN).matcher(stmt);
            assertTrue(matcher.find());
            assertEquals("b=a.split(\"\")", stmt.substring(matcher.group(0).length()).strip());
        }

        @Test
        void checkStringValue() {
            String stmt = "\"1969-12-31T18:00:48.000-06:00\"";
            JSInterpreter jsInterpreter = new JSInterpreter(stmt);
            StatementResultDto result = jsInterpreter.interpretStatement(stmt, new HashMap<>(), 100);
            System.out.println();
        }
    }
}