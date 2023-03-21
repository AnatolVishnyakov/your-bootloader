package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

    boolean canHandle(Update message);

    void handle(Bot bot, Update message);

    @SneakyThrows
    default void sendNotification(Bot bot, Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }
}