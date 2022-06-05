package com.github.yourbootloader.bot.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RemoveMessageEvent extends ApplicationEvent {

    private final String chatId;
    private final int messageId;

    public RemoveMessageEvent(String chatId, int messageId) {
        super(chatId + messageId);
        this.chatId = chatId;
        this.messageId = messageId;
    }
}
