package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {

    String START = "/start";

    boolean canHandle(Update message);

    void handle(Bot bot, Update message);

    @SneakyThrows
    default void sendNotification(Bot bot, Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }

    static Command undefinedCommand() {
        return new Command() {
            @Override
            public boolean canHandle(Update message) {
                return false;
            }

            @SneakyThrows
            @Override
            public void handle(Bot bot, Update message) {
                Message currentMessage = message.getMessage();
                DeleteMessage msg = new DeleteMessage(currentMessage.getChatId().toString(), currentMessage.getMessageId());
                bot.execute(msg);
            }
        };
    }
}