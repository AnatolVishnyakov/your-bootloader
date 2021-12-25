package com.github.yourbootloader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "youtube")
public class YDProperties {
    private String downloadPath;

    private Long maxFileSize;
}
