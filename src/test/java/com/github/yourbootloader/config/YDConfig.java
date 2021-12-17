package com.github.yourbootloader.config;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration
public class YDConfig {
    @MockBean
    Bot bot;

    @MockBean
    BotInitializer botInitializer;
}