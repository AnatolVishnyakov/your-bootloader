package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TempFileGenerator {
    private static final String FILE_EXTENSION = ".part";

    private final YDProperties ydProperties;

    public File create(String fileName) {
        log.info("Create temp file {} to directory {}", fileName, ydProperties.getDownloadPath());
        File newTempFile = Paths.get(ydProperties.getDownloadPath())
                .resolve(prepareFileName(fileName))
                .toFile();

        if (newTempFile.exists()) {
            log.warn("File {} already exists!", fileName);
            return newTempFile;
        }

        try {
            if (!newTempFile.createNewFile()) {
                throw new RuntimeException("File can't create!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("File created success!");
        return newTempFile;
    }

    private String prepareFileName(String fileName) {
        return new String(fileName.trim().getBytes(StandardCharsets.UTF_8))
                .concat(FILE_EXTENSION);
    }
}
