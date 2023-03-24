package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class TempFileGeneratorTest {

    private final TempFileGenerator tempFileGenerator;
    private final YDProperties ydProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    @Test
    @SneakyThrows
    void create() {
        String fileName = "test.mp3";
        String ext = "part";
        File actualFile = tempFileGenerator.createOrGetIfExists(fileName);
        File expectedFile = tempDir.resolve(fileName.concat(".").concat(ext)).toFile();

        assertTrue(expectedFile.exists());
        assertEquals(expectedFile.getName(), actualFile.getName());
        assertEquals(expectedFile.length(), actualFile.length());
    }
}