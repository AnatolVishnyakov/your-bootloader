package com.github.yourbootloader.domain.message.model;

import com.github.yourbootloader.domain.chat.repository.Chat;
import com.github.yourbootloader.domain.users.repository.Users;
import lombok.Value;
import org.telegram.telegrambots.meta.api.objects.Message;

@Value
public class MessageDto {

    Message message;

    Chat chat;

    Users user;

    public MessageDto(Message msg, Chat chat, Users user) {
        this.message = msg;
        this.chat = chat;
        this.user = user;
    }
}
