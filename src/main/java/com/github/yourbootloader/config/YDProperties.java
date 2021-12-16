package com.github.yourbootloader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@ConfigurationProperties(prefix = "youtube")
public class YDProperties {
    private String downloadPath;

    public Path getDownloadPath() {
        return Paths.get(downloadPath);
    }
}
