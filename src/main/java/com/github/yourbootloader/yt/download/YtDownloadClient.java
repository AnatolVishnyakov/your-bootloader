package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.yt.Utils;
import com.github.yourbootloader.yt.download.listener.DefaultProgressListener;
import com.github.yourbootloader.yt.download.listener.TelegramProgressListener;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.http.HttpHeaders;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
public class YtDownloadClient {

    /**
     * Youtube throttles chunks >~10M;
     * May be useful for bypassing bandwidth throttling
     * imposed by a webserver (experimental)
     */
    private static final int CONTENT_PARTITION_ON_LENGTH = 10_485_760;
    private static final int READ_TIMEOUT_IN_MILLIS = (int) TimeUnit.SECONDS.toMillis(60);
    private static final int REQUEST_TIMEOUT_IN_MILLIS = (int) TimeUnit.MINUTES.toMillis(60);
    private static final int CONNECT_TIMEOUT_IN_MILLIS = (int) TimeUnit.MINUTES.toMillis(60);
    private static final int CHUNK_SIZE = 8_192 * 2;

    private static final DefaultAsyncHttpClientConfig ASYNC_HTTP_CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setThreadPoolName(YtDownloadClient.class.getSimpleName())
            .setReadTimeout(READ_TIMEOUT_IN_MILLIS)
            .setRequestTimeout(REQUEST_TIMEOUT_IN_MILLIS)
            .setConnectTimeout(CONNECT_TIMEOUT_IN_MILLIS)
            .setHttpClientCodecMaxChunkSize(CHUNK_SIZE)
            .setChunkedFileChunkSize(CHUNK_SIZE)
            .addChannelOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            .build();

    private final List<TransferListener> listeners = new ArrayList<>();
    private final TempFileGenerator tempFileGenerator;
    private final Bot bot;
    private final Chat chat;

    public YtDownloadClient(TempFileGenerator tempFileGenerator, Bot bot, Chat chat) {
        this.tempFileGenerator = tempFileGenerator;
        this.bot = bot;
        this.chat = chat;
        listeners.add(new DefaultProgressListener());
    }

    public void download(String url, String fileName, Long contentLength) {
        log.info("Downloading is start. Yt url: {}", url);
        log.info("Content-Length: {} Mb ({} Kb)",
                DataSize.ofBytes(contentLength).toMegabytes(),
                DataSize.ofBytes(contentLength).toKilobytes()
        );
        listeners.add(new TelegramProgressListener(bot, chat, contentLength));

        int chunkSize = ThreadLocalRandom.current().nextInt((int) (CONTENT_PARTITION_ON_LENGTH * 0.95), CONTENT_PARTITION_ON_LENGTH);
        File file = tempFileGenerator.createOrGetIfExists(fileName + "." + contentLength);
        long start = file.length();

        HttpHeaders headers = Utils.newHttpHeaders();
        while (start <= contentLength) {
            int end = (int) Math.min(start + chunkSize, contentLength - 1);
            headers.set(HttpHeaderNames.RANGE.toString(), "bytes=" + start + "-" + end);

            try {
                download0(url, file, headers);
                if (file.length() <= 0) {
                    log.error("Content can't be download.");
                    break;
                }
                start += chunkSize + 1;
            } catch (Exception exc) {
                log.error(exc.getMessage(), exc);
                break;
            }
        }
        log.info("Download finished...");
    }

    private void download0(String url, File file, HttpHeaders headers) throws Exception {
        log.info("Headers: {}, File length: {}", headers, file.length());

        YtDownloadAsyncHandler ytDownloadAsyncHandler = new YtDownloadAsyncHandler(file, listeners);
        try (AsyncHttpClient client = Dsl.asyncHttpClient(ASYNC_HTTP_CLIENT_CONFIG)) {
            client.prepareGet(url)
                    .setHeaders(headers)
                    .execute(ytDownloadAsyncHandler)
                    .get();
        }
    }
}
