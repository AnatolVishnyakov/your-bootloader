package com.github.yourbootloader;

import com.github.yourbootloader.refactoring.StreamDownloader;
import com.github.yourbootloader.refactoring.YoutubePageParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.util.unit.DataSize;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ConfigurationPropertiesScan
@SpringBootApplication
public class YourBootLoaderApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(YourBootLoaderApplication.class, args);
        String url = "https://www.youtube.com/watch?v=nui3hXzcbK0";
        YoutubePageParser youtubePageParser = context.getBean(YoutubePageParser.class, url);
        List<Map<String, Object>> formats = youtubePageParser.parse();

        Map<String, Object> format = formats.get(2);
        System.out.println(DataSize.ofBytes(((Integer) format.get("filesize")).longValue()).toMegabytes() + " Mb");
        StreamDownloader downloader = context.getBean(StreamDownloader.class, format.get("url"), UUID.randomUUID().toString(), Collections.emptyMap());
        downloader.realDownload(3);
    }
}