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
        File newTempFile = Paths.get(ydProperties.getDownloadPath())
                .resolve(prepareFileName(fileName))
                .toFile();
        if (!newTempFile.exists()) {
            try {
                if (!newTempFile.createNewFile()) {
                    throw new RuntimeException("Не удалось создать временный файл!");
                }
            } catch (IOException e) {
                log.error("Не удалось создать файл {} в директории {}.", fileName, ydProperties.getDownloadPath());
                throw new RuntimeException(e);
            }
        }
        return newTempFile;
    }

    private String prepareFileName(String fileName) {
        return new String(fileName.trim().getBytes(StandardCharsets.UTF_8))
                .concat(FILE_EXTENSION);
    }
}
