package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.yt.Utils;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

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
            .setThreadPoolName(YtDownloadClient.class.getSimpleName())
            .setReadTimeout(60 * 1_000) // 60 sec
            .setRequestTimeout(60 * 60 * 1_000) // 1 hour
            .setConnectTimeout(60 * 60 * 1_000) // 1 hour
            .setHttpClientCodecMaxChunkSize(8_192 * 3)
            .setChunkedFileChunkSize(8_192 * 3)
            .addChannelOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            .build();

    private final TempFileGenerator tempFileGenerator;
    @Setter
    private TransferListener listener = new DefaultProgressListener();

    private void establishConnection() {
    }

    private void download0(String url, File file, HttpHeaders headers) throws Exception {
        YtDownloadAsyncHandler ytDownloadAsyncHandler = new YtDownloadAsyncHandler(file, listener);
        log.info("File length: {}", file.length());

        try (AsyncHttpClient client = Dsl.asyncHttpClient(ASYNC_HTTP_CLIENT_CONFIG)) {
            client.prepareGet(url)
                    .setHeaders(headers)
                    .execute(ytDownloadAsyncHandler)
                    .get();
        }
    }

    @Async
    public void download(String url, String fileName, Long contentLength) {
        log.info("Downloading is start. Yt url: {}", url);
        log.info("Content-Length: {} Mb ({} Kb)",
                DataSize.ofBytes(contentLength).toMegabytes(),
                DataSize.ofBytes(contentLength).toKilobytes()
        );

        File file = tempFileGenerator.create(fileName + "." + contentLength);

        int start = (int) file.length() == 0 ? 0 : (int) file.length();
        int end;

        HttpHeaders headers = Utils.newHttpHeaders();
        while (start < contentLength) {
            log.info("Headers: {}", headers);
            int chunkSize = ThreadLocalRandom.current().nextInt((int) (CHUNK_SIZE_DEFAULT * 0.95), CHUNK_SIZE_DEFAULT);
            end = (int) Math.min(start + chunkSize, contentLength - 1);
            log.info("Range {}-{} downloading... ", start, end);

            if (start < end) {
                headers.set(HttpHeaderNames.RANGE.toString(), "bytes=" + start + "-" + end);
            }

            establishConnection();
            try {
                download0(url, file, headers);
            } catch (Exception exc) {
                if (exc.getCause().getClass() != TimeoutException.class) {
                    return;
                }
                log.error(exc.getMessage(), exc);
                continue;
            }

            start += chunkSize + 1;
        }
        log.info("Download finished...");
    }
}
