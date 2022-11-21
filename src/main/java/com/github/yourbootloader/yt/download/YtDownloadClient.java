package com.github.yourbootloader.yt.download;

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
@RequiredArgsConstructor
public class YtDownloadClient {

    /**
     * Youtube throttles chunks >~10M;
     * May be useful for bypassing bandwidth throttling
     * imposed by a webserver (experimental)
     */
    private static final int CHUNK_SIZE_DEFAULT = 10_485_760;

    private static final DefaultAsyncHttpClientConfig ASYNC_HTTP_CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setReadTimeout(100_000_000)
            .setRequestTimeout(100_000_000)
            .setConnectTimeout(100_000_000)
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

    private final TempFileGenerator tempFileGenerator;
    private final ApplicationEventPublisher publisher;
    private Chat chat;

    private void establishConnection() {
    }

    private void download(String url, DataSize dataSize, long rangeOffset, File file, HttpHeaders headers) throws Exception {
        YtDownloadAsyncHandler ytDownloadAsyncHandler = new YtDownloadAsyncHandler();
        ytDownloadAsyncHandler.setFile(file);
        ytDownloadAsyncHandler.addTransferListener(new ProgressListener(
                publisher, chat, dataSize, file));
        log.info("File length: {}", file.length());

        try (AsyncHttpClient client = Dsl.asyncHttpClient(ASYNC_HTTP_CLIENT_CONFIG)) {
            client.prepareGet(url)
                    .setRangeOffset(rangeOffset)
                    .setHeaders(headers)
                    .execute(ytDownloadAsyncHandler)
                    .get();
        }
    }

    @Async
    public void realDownload(String url, String fileName, Long contentSize) throws Exception {
        log.info("Downloading is start. Yt url: {}", url);

        DataSize dataSize = DataSize.ofBytes(contentSize);
        log.info("Size content: {} Mb ({} Kb)", dataSize.toMegabytes(), dataSize.toKilobytes());
        File file = tempFileGenerator.create(fileName + "." + dataSize.toBytes());

        int start = (int) file.length() == 0 ? 0 : (int) file.length();

        HttpHeaders headers = Utils.newHttpHeaders();
        while (start < contentSize) {
            int chunkSize = ThreadLocalRandom.current().nextInt((int) (CHUNK_SIZE_DEFAULT * 0.95), CHUNK_SIZE_DEFAULT);
            int end = (int) Math.min(start + chunkSize - 1, dataSize.toBytes() - 1);
            log.info("Range {}-{} downloading... ", start, end);

            if (start < end) {
                headers.set(HttpHeaderNames.RANGE.toString(), "bytes=" + start + "-" + end);
            }

            establishConnection();
            download(url, dataSize, start, file, headers);

            start += chunkSize + 1;
        }
        log.info("Download finished...");
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
