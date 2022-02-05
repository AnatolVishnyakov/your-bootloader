package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class YtDownloadCommand implements Command {
    @Override
    public boolean canHandle(Update message) {
        return message.hasCallbackQuery();
    }

    @Override
    public void handle(Bot bot, Update update) {
        log.info("Download: {}", update.getCallbackQuery().getData());
        throw new MethodNotImplementedException();
    }
}
