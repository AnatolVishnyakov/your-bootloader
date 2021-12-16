package com.github.yourbootloader.config;

import com.github.yourbootloader.yt.StreamDownloader;
import com.github.yourbootloader.yt.YoutubePageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;

@Configuration
public class YDConfig {

    @Autowired
    ApplicationContext context;

    @Bean
    @Scope(value = "prototype")
    public StreamDownloader streamDownloader(String url, String fileName, Map<String, Object> info) {
        StreamDownloader downloader = new StreamDownloader(url, fileName, info);
        downloader.setYdProperties(context.getBean(YDProperties.class));
        return downloader;
    }

    @Bean
    @Scope(value = "prototype")
    public YoutubePageParser youtubePageParser(String url) {
        return new YoutubePageParser(url);
    }
}
