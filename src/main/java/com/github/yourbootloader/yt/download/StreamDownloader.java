package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StreamDownloader {
    private static final int DEFAULT_TIMEOUT = 600_000;

    private final YDProperties ydProperties;
    private final TempFileGenerator tempFileGenerator;
    private final ApplicationEventPublisher publisher;

    // TODO вынести
    private String url;
    private String fileName;
    private Long fileSize;
    private Chat chat;

    private void establishConnection() {
    }

    private void download() throws Exception {
        DataSize dataSize = DataSize.ofBytes(fileSize);
        log.info("Размер скачиваемого содержимого: {} Mb ({} Kb)", dataSize.toMegabytes(), dataSize.toKilobytes());
        File file = tempFileGenerator.create(fileName + "." + dataSize.toBytes());

        validate(file);

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setMaxRequestRetry(3)
                .setThreadPoolName(StreamDownloader.class.getSimpleName())
                .setHttpClientCodecMaxChunkSize(8_192 * 2)
                .addIOExceptionFilter(new ResumableIOExceptionFilter())
                .addRequestFilter(new ThrottleRequestFilter(1_000))
                .build();

        DownloaderAsyncHandler downloaderAsyncHandler = new DownloaderAsyncHandler(chat, file);
        downloaderAsyncHandler.setApplicationEventPublisher(publisher);
        downloaderAsyncHandler.setContentSize(dataSize);
        log.info("File length: {}", file.length());

        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
            client.prepareGet(url)
                    .setRangeOffset(file.length())
                    .execute(downloaderAsyncHandler).get();
        }
    }

    private void validate(File file) {
        if (DataSize.ofBytes(fileSize).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(file.length()).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(fileSize - file.length() + file.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
            throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
        }
    }

    public void realDownload(int retries, String url, String fileName, Long fileSize) throws Exception {
        log.info("Скачивание url: {}", url);
        this.url = url;
        this.fileName = fileName;
        this.fileSize = fileSize;

//            headers.add("Youtubedl-no-compression", "True");

        establishConnection();
        download();
        log.info("Download finished...");
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
