package com.github.yourbootloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class YourBootLoaderApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourBootLoaderApplication.class, args);
    }
}