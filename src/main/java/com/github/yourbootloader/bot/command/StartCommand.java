package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class StartCommand implements Command {
    @Override
    public boolean canHandle(Update message) {
        return message.hasMessage() &&
                message.getMessage().getText().equals(START);
    }

    @Override
    public void handle(Bot bot, Update message) {
        log.info("Start command: {}", message);
    }
}
