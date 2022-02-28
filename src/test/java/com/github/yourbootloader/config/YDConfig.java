package com.github.yourbootloader.config;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class YDConfig {
    @MockBean
    Bot bot;

    @MockBean
    BotInitializer botInitializer;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}