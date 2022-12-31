package com.github.yourbootloader.yt.download.listener;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.util.unit.DataSize;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class DefaultProgressListener implements TransferListener {

    private long lastTimeMsReceivedByte = System.currentTimeMillis();
    private final AtomicLong receivedBytes = new AtomicLong(0);

    @Override
    public void onRequestHeadersSent(HttpHeaders headers) {
        log.info("Headers sent: {}", headers);
    }

    @Override
    public void onResponseHeadersReceived(HttpHeaders headers) {
        log.info("Headers received: {}", headers);
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        long currentTimeMillis = System.currentTimeMillis();
        long allReceivedBytes = receivedBytes.addAndGet(bytes.length);
        log.info("Bytes received: {} Kb (chunk: {}, delay: {} ms)",
                DataSize.ofBytes(allReceivedBytes).toKilobytes(),
                bytes.length, currentTimeMillis - lastTimeMsReceivedByte);
        lastTimeMsReceivedByte = currentTimeMillis;
    }

    @Override
    public void onBytesSent(long amount, long current, long total) {
        log.info("Bytes sent, amount: {}, current: {}, total: {}", amount, current, total);
    }

    @Override
    public void onRequestResponseCompleted() {
        log.info("Request or response completed.");
    }

    @Override
    public void onThrowable(Throwable t) {
        log.error("Was error: {}", t.getMessage());
    }
}
