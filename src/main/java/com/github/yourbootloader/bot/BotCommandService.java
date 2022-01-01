package com.github.yourbootloader.bot;

import com.github.yourbootloader.yt.download.StreamDownloader;
import io.netty.handler.codec.http.DefaultHttpHeaders;
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

    private final StreamDownloader streamDownloader;

    @SneakyThrows
    public void download(Chat chat, String url, String filename, Long filesize) {
        streamDownloader.setChat(chat);
        streamDownloader.realDownload(3, url, filename, filesize, new DefaultHttpHeaders());
    }
}
