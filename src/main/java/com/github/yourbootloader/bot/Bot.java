package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.command.Command;
import com.github.yourbootloader.bot.exception.CommandNotFoundException;
import com.github.yourbootloader.config.BotConfig;
import com.github.yourbootloader.utils.StreamCollectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final List<Command> commands;

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: {}", update.getMessage());

        StreamEx.of(commands)
                .filter(command -> command.canHandle(update))
                .collect(StreamCollectors.atMostOneRow(CommandNotFoundException::new))
                .handle(this, update);
    }

    public String getBotUsername() {
        return config.getBotUserName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
