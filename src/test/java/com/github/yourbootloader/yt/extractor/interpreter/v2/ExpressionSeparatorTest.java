package com.github.yourbootloader.yt.extractor.interpreter.v2;

import com.github.yourbootloader.yt.extractor.interpreter.v2.dto.DefaultSeparateArgs;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionSeparatorTest {
    @Test
    @SneakyThrows
    void separate05() {
        Path folderJsData = Paths.get("src/test/resources/testdata/jscontent");
        Path jsContentPath = folderJsData.resolve("content-01.js");
        String jsContent = String.join("", Files.readAllLines(jsContentPath));
        ExpressionSeparator expressionSeparator = new ExpressionSeparator();
        assertEquals(2, expressionSeparator.separate(jsContent, new DefaultSeparateArgs()).size());
    }
}