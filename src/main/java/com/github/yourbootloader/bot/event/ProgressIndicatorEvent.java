package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.UUID;

@Value
public class ProgressIndicatorEvent {

    Chat chat;

    DataSize contentSize;

    long fileSize;

    int blockSize;

    UUID downloadId;

    long delay;

}
