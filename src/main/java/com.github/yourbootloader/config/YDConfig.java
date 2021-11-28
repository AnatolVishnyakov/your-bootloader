package com.github.yourbootloader.config;

import com.github.yourbootloader.refactoring.StreamDownloader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@Configuration
public class YDConfig {

    @Bean
    @Scope(value = "prototype")
    public StreamDownloader streamDownloader(String url, String fileName, Map<String, Object> info) {
        return new StreamDownloader(url, fileName, info);
    }
}
