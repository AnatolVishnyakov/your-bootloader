package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Value
public class FailureDownloadEvent {

    Chat chat;

    Throwable error;

}
