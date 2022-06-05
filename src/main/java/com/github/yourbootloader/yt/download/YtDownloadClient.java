package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.asynchttpclient.handler.TransferListener;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YtDownloadClient {
    private static final int DEFAULT_TIMEOUT = 10_000_000;

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
                .setKeepAlive(true)
                .setThreadPoolName(YtDownloadClient.class.getSimpleName())
                .setHttpClientCodecMaxChunkSize(8_192 * 5)
                .setChunkedFileChunkSize(8_192 * 3)
                .addIOExceptionFilter(new ResumableIOExceptionFilter())
                .addRequestFilter(new ThrottleRequestFilter(1_000))
                .build();

//        DownloaderAsyncHandler downloaderAsyncHandler = new DownloaderAsyncHandler(chat, file);
//        downloaderAsyncHandler.setApplicationEventPublisher(publisher);
//        downloaderAsyncHandler.setContentSize(dataSize);
//        log.info("File length: {}", file.length());
//
//        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
//            client.prepareGet(url)
//                    .setRangeOffset(file.length())
//                    .setHeader("Youtubedl-no-compression", "True")
//                    .setHeader("Cache-Control", "private, max-age=21298")
//                    .setHeader("Accept-Ranges", "bytes")
//                    .setHeader("Connection", "keep-alive")
//                    .setHeader("Vary", "Origin")
//                    .setHeader("Cross-Origin-Resource-Policy", "cross-origin")
//                    .setHeader("X-Content-Type-Options", "nosniff")
//                    .setHeader("Server", "gvs 1.0")
//                    .execute(downloaderAsyncHandler).get();
//        }

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
            UUID downloadId = UUID.randomUUID();
            TransferCompletionHandler transferCompletionHandler = new TransferCompletionHandler();
            transferCompletionHandler.addTransferListener(new TransferListener() {
                @Override
                public void onRequestHeadersSent(HttpHeaders headers) {
                    log.info("call-onRequestHeadersSent");
                }

                @Override
                public void onResponseHeadersReceived(HttpHeaders headers) {
                    log.info("call-onResponseHeadersReceived");
                }

                @SneakyThrows
                @Override
                public void onBytesReceived(byte[] bytes) {
                    log.info("call-onBytesReceived: {}", bytes.length);

                    ByteBuffer buffer = ByteBuffer.wrap(bytes);
                    randomAccessFile.seek(randomAccessFile.length());
                    if (buffer.hasArray()) {
                        randomAccessFile.write(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
                    } else {
                        byte[] b = new byte[buffer.remaining()];
                        int pos = buffer.position();
                        buffer.get(b);
                        buffer.position(pos);
                        randomAccessFile.write(b);
                    }

                    publisher.publishEvent(new ProgressIndicatorEvent(chat, dataSize, file.length(), bytes.length, downloadId));
                }

                @Override
                public void onBytesSent(long amount, long current, long total) {
                    log.info("call-onBytesSent: {} / {} / {}", amount, current, total);
                }

                @Override
                public void onRequestResponseCompleted() {
                    log.info("call-onRequestResponseCompleted");
                    publisher.publishEvent(new FinishDownloadEvent(chat, downloadId, file));
                }

                @Override
                public void onThrowable(Throwable t) {
                    log.info("call-onThrowable");
                }
            });

            client.prepareGet(url).execute(transferCompletionHandler).get();
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

        establishConnection();
        download();
        log.info("Download finished...");
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
