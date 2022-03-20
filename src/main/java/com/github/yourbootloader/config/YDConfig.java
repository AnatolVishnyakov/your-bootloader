package com.github.yourbootloader.config;

import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class YDConfig implements AsyncConfigurer {

    @Bean
    @Scope(value = "prototype")
    public YoutubePageParser youtubePageParser(String url) {
        return new YoutubePageParser(url);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-executor");
        executor.initialize();
        return executor;
    }
}
