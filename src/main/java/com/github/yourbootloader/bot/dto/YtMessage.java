package com.github.yourbootloader.bot.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName(value = "YtMessage")
public class YtMessage {
    @JsonDeserialize(as = Chat.class)
    Chat chat;

    @JsonDeserialize(as = String.class)
    String url;

    @JsonDeserialize(as = String.class)
    String title;

    @JsonDeserialize(as = Long.class)
    Long filesize;
}
