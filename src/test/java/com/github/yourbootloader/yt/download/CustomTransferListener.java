package com.github.yourbootloader.yt.download;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
class CustomTransferListener implements TransferListener {

    AtomicLong timeout = new AtomicLong();
    AtomicLong counter = new AtomicLong();
    AtomicReference<Long> time = new AtomicReference<>(System.currentTimeMillis());
    LocalDateTime start = LocalDateTime.now();

    @Override
    public void onRequestHeadersSent(HttpHeaders headers) {
        log.info("onRequestHeadersSent {}", headers);
    }

    @Override
    public void onResponseHeadersReceived(HttpHeaders headers) {
        log.info("onResponseHeadersReceived {}", headers);
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        log.info("{} ETA {} | received {} MBytes | timeout: {}",
                formatBytes(bytes),
                formatSeconds(calcSpeed(time.get(), System.currentTimeMillis(), bytes)),
                DataSize.ofBytes(counter.get()).toMegabytes(),
                System.currentTimeMillis() - timeout.get()
        );
        counter.addAndGet(bytes.length);
        time.set(System.currentTimeMillis());
        timeout.set(System.currentTimeMillis());
    }

    private String formatBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "N/A";
        }

        int exponent = calcExp(bytes.length);
        String suffix = new String[]{"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"}[exponent];
        double converted = bytes.length / Math.pow(1024, exponent);
        return String.format("%.2f %s/s", converted, suffix);
    }

    private int calcExp(int length) {
        int i = 0;
        while (length / 1024 != 0) {
            i++;
            length /= 1024;
        }
        return i;
    }

    private Double calcSpeed(Long start, Long now, byte[] bytes) {
        long dif = now - start;
        if (bytes.length == 0 || dif < 0.001) {
            return 0.0;
        }
        return ((double) bytes.length) / dif;
    }

    private String formatSeconds(double seconds) {
        int mins = (int) seconds / 60;
        int secs = (int) seconds % 60;
        int hours = mins / 60;
        mins = mins % 60;

        if (hours > 99) {
            return "--:--:--";
        }
        if (hours == 0) {
            return String.format("%d:%d", mins, secs);
        } else {
            return String.format("%d:%d:%d", hours, mins, secs);
        }
    }

    @Override
    public void onBytesSent(long amount, long current, long total) {
        log.info("Bytes sent: {}", total);
    }

    @Override
    public void onRequestResponseCompleted() {
        log.info("Response completed. time: {}", Duration.between(start, LocalDateTime.now()).getSeconds());
    }

    @Override
    public void onThrowable(Throwable t) {
        log.info("onThrowable");
    }
}