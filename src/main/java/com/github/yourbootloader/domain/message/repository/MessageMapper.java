package com.github.yourbootloader.domain.message.repository;

import com.github.yourbootloader.domain.message.model.MessageDto;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public Message map(MessageDto messageDto) {
        Message message = new Message();
        message.setMessageId(messageDto.getMessage().getMessageId());
        message.setUrl(messageDto.getMessage().getText());
        message.setCreatedUser(messageDto.getUser());
        message.setChat(messageDto.getChat());
        return message;
    }
}
