package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import com.github.yourbootloader.yt.Utils;
import com.github.yourbootloader.yt.download.v2.ProgressListener;
import com.github.yourbootloader.yt.download.v2.YtDownloadAsyncHandler;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YtDownloadClient {

    private static final int DEFAULT_TIMEOUT = 10_000_000;
    private static final DefaultAsyncHttpClientConfig ASYNC_HTTP_CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setRequestTimeout(DEFAULT_TIMEOUT)
            .setReadTimeout(DEFAULT_TIMEOUT)
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setMaxRequestRetry(3)
            .setThreadPoolName(YtDownloadClient.class.getSimpleName())
            .setHttpClientCodecMaxChunkSize(8_192 * 3)
            .setChunkedFileChunkSize(8_192 * 3)
            .addIOExceptionFilter(new ResumableIOExceptionFilter())
            .addRequestFilter(new ThrottleRequestFilter(1_000))
            .setIoThreadsCount(10)
            .setKeepAlive(true)
            .setSoKeepAlive(true)
            .setTcpNoDelay(true)
            .addChannelOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            .build();

    private final YDProperties ydProperties;
    private final TempFileGenerator tempFileGenerator;
    private final ApplicationEventPublisher publisher;

    // TODO вынести
    private String url;
    private Long fileSize;
    private Chat chat;

    private void establishConnection() {
    }

    private void download(int start, int end, DataSize dataSize, File file, HttpHeaders headers) throws Exception {
//        DownloaderAsyncHandler downloaderAsyncHandler = new DownloaderAsyncHandler(chat, file);
//        downloaderAsyncHandler.setApplicationEventPublisher(publisher);
//        downloaderAsyncHandler.setContentSize(dataSize);
        YtDownloadAsyncHandler ytDownloadAsyncHandler = new YtDownloadAsyncHandler();
        ytDownloadAsyncHandler.setFile(file);
        ytDownloadAsyncHandler.addTransferListener(new ProgressListener(
                publisher, chat, dataSize, file));
        log.info("File length: {}", file.length());

        try (AsyncHttpClient client = Dsl.asyncHttpClient(ASYNC_HTTP_CLIENT_CONFIG)) {
            if (start < end) {
                headers.set(HttpHeaderNames.RANGE.toString(), "bytes=" + start + "-" + end);
            }

            client.prepareGet(url)
//                    .setRangeOffset(file.length())
                    .setHeaders(headers)
//                    .execute(downloaderAsyncHandler)
                    .execute(ytDownloadAsyncHandler)
                    .get();
        }
    }

    @Async
    public void realDownload(String url, String fileName, Long fileSize) throws Exception {
        log.info("Скачивание url: {}", url);
        this.url = url;
        this.fileSize = fileSize;

        DataSize dataSize = DataSize.ofBytes(fileSize);
        log.info("Размер скачиваемого содержимого: {} Mb ({} Kb)", dataSize.toMegabytes(), dataSize.toKilobytes());
        File file = tempFileGenerator.create(fileName + "." + dataSize.toBytes());

        int start = (int) file.length() == 0 ? 0 : (int) file.length();
        int end = 0;

        HttpHeaders headers = Utils.newHttpHeaders();
        int chunkSizeDefault = 10_485_760;
        while (start < fileSize) {
            int chunkSize = ThreadLocalRandom.current().nextInt((int) (chunkSizeDefault * 0.95), chunkSizeDefault);
            end = (int) Math.min(start + chunkSize - 1, dataSize.toBytes() - 1);
            log.info("Chunk bytes={}-{} downloading... ", start, end);

            establishConnection();
            download(start, end, dataSize, file, headers);

            start += chunkSize + 1;
        }
        log.info("Download finished...");
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
