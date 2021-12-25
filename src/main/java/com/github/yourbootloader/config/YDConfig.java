package com.github.yourbootloader.config;

import com.github.yourbootloader.yt.extractor.YoutubePageParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class YDConfig {

    @Bean
    @Scope(value = "prototype")
    public YoutubePageParser youtubePageParser(String url) {
        return new YoutubePageParser(url);
    }
}
