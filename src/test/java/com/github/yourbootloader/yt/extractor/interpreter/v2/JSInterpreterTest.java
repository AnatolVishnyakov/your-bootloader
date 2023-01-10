package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}