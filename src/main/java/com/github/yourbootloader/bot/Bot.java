package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.command.Command;
import com.github.yourbootloader.bot.command.UndefinedCommand;
import com.github.yourbootloader.config.BotConfig;
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
                .findAny()
                .orElseGet(UndefinedCommand::new)
                .handle(this, update);
    }

    @Override
    public String getBotUsername() {
        return config.getBotUserName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
