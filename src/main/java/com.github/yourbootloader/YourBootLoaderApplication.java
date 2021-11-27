package com.github.yourbootloader;

import com.github.yourbootloader.refactoring.StreamDownloader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class YourBootLoaderApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(YourBootLoaderApplication.class, args);
        context.getBean(StreamDownloader.class).download();
    }
}