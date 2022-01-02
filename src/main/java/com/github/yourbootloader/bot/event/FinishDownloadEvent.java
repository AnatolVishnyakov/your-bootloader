package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;

@Value
public class FinishDownloadEvent {

    Chat chat;

    File downloadFile;

}
