package com.github.yourbootloader.domain.chat.repository;

import org.springframework.stereotype.Component;

@Component
public class ChatMapper {
    public Chat map(org.telegram.telegrambots.meta.api.objects.Chat chat) {
        Chat newChat = new Chat();
        newChat.setTitle(chat.getTitle());
        newChat.setDescription(chat.getDescription());
        newChat.setType(chat.getType());
        newChat.setInviteLink(chat.getInviteLink());
        return newChat;
    }
}
