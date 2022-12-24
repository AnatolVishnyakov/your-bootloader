package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.yt.Utils;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    private static final int CONTENT_PARTITION_ON_LENGTH = 10_485_760;
    private static final int READ_TIMEOUT_IN_MILLIS = 60_000;
    private static final int REQUEST_TIMEOUT_IN_MILLIS = 3_600_000;
    private static final int CONNECT_TIMEOUT_IN_MILLIS = 3_600_000;
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

    private final TempFileGenerator tempFileGenerator;
    @Getter
    private final List<TransferListener> listeners = new ArrayList<>();

    private void establishConnection() {
    }

    private void download0(String url, File file, HttpHeaders headers) throws Exception {
        YtDownloadAsyncHandler ytDownloadAsyncHandler = new YtDownloadAsyncHandler(file, listeners);
        log.info("File length: {}", file.length());

        try (AsyncHttpClient client = Dsl.asyncHttpClient(ASYNC_HTTP_CLIENT_CONFIG)) {
            client.prepareGet(url)
                    .setHeaders(headers)
                    .execute(ytDownloadAsyncHandler)
                    .get();
        }
    }

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
            int chunkSize = ThreadLocalRandom.current().nextInt((int) (CONTENT_PARTITION_ON_LENGTH * 0.95), CONTENT_PARTITION_ON_LENGTH);
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
