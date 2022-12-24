package com.github.yourbootloader.bot;

import com.github.yourbootloader.yt.download.YtDownloadClient;
import com.github.yourbootloader.yt.download.listener.DefaultProgressListener;
import com.github.yourbootloader.yt.download.listener.TelegramProgressListener;
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
    public void download(Bot bot, Chat chat, String url, String filename, Long contentLength) {
        ytDownloadClient.getListeners().add(new TelegramProgressListener(bot, chat, contentLength));
        ytDownloadClient.getListeners().add(new DefaultProgressListener());
        ytDownloadClient.download(url, filename, contentLength);
    }
}
