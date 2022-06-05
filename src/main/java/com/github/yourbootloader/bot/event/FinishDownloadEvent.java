package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.util.UUID;

@Value
public class FinishDownloadEvent {

    Chat chat;

    UUID downloadId;

    File downloadFile;

}
