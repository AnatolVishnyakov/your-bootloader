package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

    String START = "/start";

    boolean canHandle(Update message);

    void handle(Bot bot, Update message);

}