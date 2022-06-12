package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.asynchttpclient.handler.TransferListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YtDownloadClientTest {

    private final YtDownloadClient ytDownloadClient;
    private final YDProperties ydProperties;
    private final static DefaultAsyncHttpClientConfig CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setRequestTimeout(4_000_000) // 1.1 час
//            .setCompressionEnforced(true)
//            .setEnablewebSocketCompression(false)
//            .setKeepAlive(true)
            .setHttpClientCodecMaxChunkSize(8_192 * 5)
            .setChunkedFileChunkSize(1_024)
//            .setTcpNoDelay(false)

//                .setReadTimeout(10_000)
//                .setConnectTimeout(10_000)
//                .setMaxRequestRetry(3)
//                .setKeepAlive(true)
//                .setThreadPoolName(YtDownloadClient.class.getSimpleName())
//                .addIOExceptionFilter(new ResumableIOExceptionFilter())
//                .addRequestFilter(new ThrottleRequestFilter(1_000))
//                .setIoThreadsCount(10)
            .build();

    @TempDir
    Path tempDir;

    @BeforeEach
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    //    @Test
    void realDownload() throws Exception {
        ytDownloadClient.realDownload(
                3,
//                "https://speed.hetzner.de/100MB.bin",
                "https://rr2---sn-jvhnu5g-c35k.googlevideo.com/videoplayback?expire=1645980147&ei=k1UbYufHCoqM7ASr2YxI&ip=46.138.209.28&id=o-AN0v1cGnoTZGVBxVq0VaLyLQ50PnBgSuRRLWSA2UHS-f&itag=18&source=youtube&requiressl=yes&mh=0_&mm=31%2C29&mn=sn-jvhnu5g-c35k%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pl=21&initcwndbps=1308750&vprv=1&mime=video%2Fmp4&ns=Rc33wF6dhY7qrjG48-4KEw8G&gir=yes&clen=238936637&ratebypass=yes&dur=5100.402&lmt=1537070139281011&mt=1645958217&fvip=4&fexp=24001373%2C24007246&c=WEB&n=tHlHwPfyvWBt399&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIgNhbGw1_zmmGP0gk-jbNPrY1iYaJAQrk3I2ofUgWPamkCIQD5p0bykdRvDNxyZn1lfNqOoSjYyaYYhTtXCs5yNvcSog%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhALzEhJsGnnJ3Jn7j143CVXkLNdXr_bjL7D5sr19ZafOFAiEAoONlPrKqzkv9MFd9QR2QDs332cZ1v8OvlYH2N6j6aTk%3D",
                "test-6656",
                17844736L
        );
    }

    @Test
    @SneakyThrows
    void foo() {
        // 8_192 * 2 ~ 84 seconds
        // 8_192 ~ 85 second

        AtomicLong counter = new AtomicLong();
        LocalDateTime start = LocalDateTime.now();
//        String ytUrl = "https://speed.hetzner.de/100MB.bin";
        String ytUrl = "https://rr2---sn-jvhnu5g-c35k.googlevideo.com/videoplayback?expire=1655066365&ei=nfqlYsG9EdOCv_IP-eSJiAY&ip=2a00%3A1370%3A819e%3A86b9%3Aed5b%3A8e4d%3A6065%3A1eec&id=o-AKi6EKfmTA5o3LkDl0JXew_aku7bKj9SG_ailA3MSbfo&itag=18&source=youtube&requiressl=yes&mh=9_&mm=31%2C29&mn=sn-jvhnu5g-c35k%2Csn-jvhnu5g-n8vy&ms=au%2Crdu&mv=m&mvi=2&pl=49&initcwndbps=1721250&spc=4ocVC8-DYzoYOCAowxYIoLF7scXNLM8&vprv=1&mime=video%2Fmp4&ns=isISGZTmlLtrhR4sI96colcG&gir=yes&clen=225096905&ratebypass=yes&dur=3673.749&lmt=1649348055936696&mt=1655044382&fvip=8&fexp=24001373%2C24007246&c=WEB&txp=4530434&n=knZYK09-GKOztvWQGD&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRgIhAN5Wni0H2kQkt7ZzBmGq3W5Pz9_WpYSujWucm5xqevwIAiEA41UjRjdcoQpOwUBoQtmVr6nVsIRWMlPDiWdDKiMzHNg%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhALOeZro63eknMlwH3stUuHEul9_v2rkH6s4rPRUAUwgDAiEA_hdBAs7uasylGLM-Ado81XaG-n6QTFBRjOs7DE70jvg%3D";
        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            TransferCompletionHandler tl = new TransferCompletionHandler();
//            DefaultHttpHeaders headers = new DefaultHttpHeaders();
//            headers.add("Youtubedl-no-compression", "True");
//            tl.headers(headers);
            AtomicReference<Long> time = new AtomicReference<>(System.currentTimeMillis());
            tl.addTransferListener(new TransferListener() {
                @Override
                public void onRequestHeadersSent(HttpHeaders headers) {
                    log.info("onRequestHeadersSent");
                }

                @Override
                public void onResponseHeadersReceived(HttpHeaders headers) {
                    log.info("onResponseHeadersReceived");
                }

                @Override
                public void onBytesReceived(byte[] bytes) {
                    log.info("{} ETA {} | received {} MBytes",
                            formatBytes(bytes),
                            formatSeconds(calcSpeed(time.get(), System.currentTimeMillis(), bytes)),
                            DataSize.ofBytes(counter.get()).toMegabytes()
                    );
                    counter.addAndGet(bytes.length);
                    time.set(System.currentTimeMillis());
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


            });
            client.prepareGet(ytUrl)
//                    .setHeader("Youtubedl-no-compression", "True")
//                    .setHeader("Cache-Control", "private, max-age=21298")
//                    .setHeader("Accept-Ranges", "bytes")
//                    .setHeader("Connection", "keep-alive")
//                    .setHeader("Vary", "Origin")
//                    .setHeader("Cross-Origin-Resource-Policy", "cross-origin")
//                    .setHeader("X-Content-Type-Options", "nosniff")
//                    .setHeader("Server", "gvs 1.0")
                    .execute(tl)
                    .get();
        }
    }
}