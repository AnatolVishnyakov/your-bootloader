package com.github.yourbootloader.config;

import com.github.yourbootloader.refactoring.StreamDownloader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class YDConfig {

    @Bean
    @Scope(value = "prototype")
    public StreamDownloader streamDownloader(String url, String fileName) {
        return new StreamDownloader(url, fileName);
    }
}
