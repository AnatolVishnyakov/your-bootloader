package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class UndefinedCommand implements Command {
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
}
