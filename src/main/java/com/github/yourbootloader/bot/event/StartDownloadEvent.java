package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Value
public class StartDownloadEvent {

    Chat chat;

}
