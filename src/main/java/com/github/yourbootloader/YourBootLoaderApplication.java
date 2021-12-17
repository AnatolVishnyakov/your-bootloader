package com.github.yourbootloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

@ConfigurationPropertiesScan
@SpringBootApplication
public class YourBootLoaderApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(YourBootLoaderApplication.class, args);

//        String url = "https://youtu.be/zcjKJ7FHDLM";
//        YoutubePageParser youtubePageParser = context.getBean(YoutubePageParser.class, url);
//        List<Map<String, Object>> formats = youtubePageParser.parse();
//        Map<String, Object> format = formats.get(0);
//
//        url = ((String) format.get("url"));
//        String title = (String) format.get("title");
//        StreamDownloader downloader = context.getBean(StreamDownloader.class, url, title, format);
//        try {
//            downloader.realDownload(3);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}