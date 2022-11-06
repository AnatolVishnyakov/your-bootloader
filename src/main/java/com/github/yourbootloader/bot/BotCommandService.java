package com.github.yourbootloader.bot;

import com.github.yourbootloader.yt.download.YtDownloadClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotCommandService {

    private final YtDownloadClient ytDownloadClient;

    @SneakyThrows
    public void download(Chat chat, String url, String filename, Long filesize) {
        ytDownloadClient.setChat(chat);
        ytDownloadClient.realDownload(url, filename, filesize);
    }
}
