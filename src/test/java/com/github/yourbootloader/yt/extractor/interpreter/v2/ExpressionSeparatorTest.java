package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionSeparatorTest {
    @Test
    @SneakyThrows
    void separate01() {
        Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
        Path jsContentPath = folderJsData.resolve("content-01.js");
        String jsContent = String.join("", Files.readAllLines(jsContentPath));
        ExpressionSeparator expressionSeparator = new ExpressionSeparator();
        assertEquals(2, expressionSeparator.separate(jsContent, new DefaultSeparateArgs()).size());
    }

    @Test
    @SneakyThrows
    void separate02() {
        String expr = "2<=c[66]?(0,c[new Date(\"31 December 1969 19:00:18 EST\")/1E3])(c[55],c[68]):(0,c[33])(c[62],(0,c[24])(),c[0])";
        ExpressionSeparator expressionSeparator = new ExpressionSeparator();
        List<String> result = expressionSeparator.separate(expr, new DefaultSeparateArgs("?", null, Arrays.asList("??", "?.")));
        assertEquals(2, result.size());
    }
}