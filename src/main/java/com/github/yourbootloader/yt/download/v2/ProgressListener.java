package com.github.yourbootloader.yt.download.v2;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;

@Slf4j
class ProgressListener implements TransferListener {

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
        log.info("Bytes received: {}", bytes.length);
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
