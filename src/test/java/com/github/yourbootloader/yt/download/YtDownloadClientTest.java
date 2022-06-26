package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.Timer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.asynchttpclient.handler.TransferListener;
import org.asynchttpclient.netty.channel.ChannelManager;
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
    private static final int CHUNK_SIZE = 8_192 * 3;
    private static final DefaultAsyncHttpClientConfig CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setRequestTimeout(4_000_000) // 1.1 час
            .setChunkedFileChunkSize(CHUNK_SIZE)
            .setSoRcvBuf(CHUNK_SIZE)
            .setWebSocketMaxBufferSize(CHUNK_SIZE)
            .setWebSocketMaxFrameSize(CHUNK_SIZE)
            .setHttpClientCodecMaxChunkSize(CHUNK_SIZE)
            .setHttpClientCodecMaxHeaderSize(CHUNK_SIZE)
            .setHttpClientCodecMaxInitialLineLength(CHUNK_SIZE)
            .setHttpClientCodecInitialBufferSize(CHUNK_SIZE)
            .setTcpNoDelay(true)
            .setEnablewebSocketCompression(false)
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
        String ytUrl = "https://rr6---sn-jvhnu5g-c35d.googlevideo.com/videoplayback?expire=1656279607&ei=1324YtClE5mg7QSxsLSwAg&ip=2a00%3A1370%3A819e%3A47d%3A900b%3Ae1dc%3A671c%3Ab75a&id=o-AKkbkzsGPCqID-fwaXcQ1o8HjD7nzPY7LW5Xf99weGy-&itag=251&source=youtube&requiressl=yes&mh=-P&mm=31%2C29&mn=sn-jvhnu5g-c35d%2Csn-jvhnu5g-n8vr&ms=au%2Crdu&mv=m&mvi=6&pl=54&initcwndbps=1478750&spc=4ocVC_YqRYkAzGSS7HpdkMq8TfIM3JE&vprv=1&mime=audio%2Fwebm&ns=2DDmthaJ7F37vUrw18AOUEoG&gir=yes&clen=57838027&dur=4278.521&lmt=1538274826435090&mt=1656257572&fvip=8&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=1301222&n=MD3JkUEfYxWFANa2RFS&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRgIhAKTIR-iRKusVOscc3pi5s1ACUN9dfD3vJMc7btJs9vMlAiEAuEzDiwIZpw20tSzV7Vdv4ZLTAwDeKloh2_G-FZbzSCM%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIgB7eybbfadEiLtXtoKR1oiCxpN4SEWa2B8zlq6Ll_sx4CIQD9dxu8hDpPuR7zFyM6TBLQlSqL_sxjlmzUM1JgdJqxMw%3D%3D";
        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            TransferCompletionHandler tl = new TransferCompletionHandler(true);
            DefaultHttpHeaders headers = new DefaultHttpHeaders();
            headers.set("Range", "bytes=0-10245737");
            headers.set("Last-Modified", "Sun, 30 Sep 2018 00:52:20 GMT");
            headers.set("Content-Type", "audio/mp4");
            headers.set("Date", "Sun, 26 Jun 2022 15:50:08 GMT");
            headers.set("Expires", "Sun, 26 Jun 2022 15:50:08 GMT");
            headers.set("Cache-Control", "private, max-age=21297");
            headers.set("Accept-Ranges", "bytes");
            headers.set("Connection", "keep-alive");
            headers.set("Vary", "Origin");
            headers.set("Cross-Origin-Resource-Policy", "cross-origin");
            headers.set("X-Content-Type-Options", "nosniff");

            tl.headers(headers);
            AtomicReference<Long> time = new AtomicReference<>(System.currentTimeMillis());
            tl.addTransferListener(new TransferListener() {
                @Override
                public void onRequestHeadersSent(HttpHeaders headers) {
                    log.info("onRequestHeadersSent {}", headers);
                    headers.set("Range", "bytes=0-10245737");
                    headers.set("Last-Modified", "Sun, 30 Sep 2018 00:52:20 GMT");
                    headers.set("Content-Type", "audio/mp4");
                    headers.set("Date", "Sun, 26 Jun 2022 15:50:08 GMT");
                    headers.set("Expires", "Sun, 26 Jun 2022 15:50:08 GMT");
                    headers.set("Cache-Control", "private, max-age=21297");
                    headers.set("Accept-Ranges", "bytes");
                    headers.set("Connection", "keep-alive");
                    headers.set("Vary", "Origin");
                    headers.set("Cross-Origin-Resource-Policy", "cross-origin");
                    headers.set("X-Content-Type-Options", "nosniff");
                }

                @Override
                public void onResponseHeadersReceived(HttpHeaders headers) {
                    log.info("onResponseHeadersReceived {}", headers);
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

    @Test
    @SneakyThrows
    void foo2() {
        String ytUrl = "https://rr2---sn-jvhnu5g-c35k.googlevideo.com/videoplayback?expire=1655136925&ei=PQ6nYr7RHJe17QSw76Yw&ip=2a00%3A1370%3A819e%3A86b9%3Ac5d6%3A1db3%3Af9fc%3A8686&id=o-AFnAEg6JIIhIFSCFrU3hvmAzPaBn21gaYcLT5KNtRRTm&itag=251&source=youtube&requiressl=yes&mh=9_&mm=31%2C29&mn=sn-jvhnu5g-c35k%2Csn-jvhnu5g-n8vy&ms=au%2Crdu&mv=m&mvi=2&pl=49&initcwndbps=1610000&spc=4ocVC8_2oYOOsauK80ZeAIY-65pb8tU&vprv=1&mime=audio%2Fwebm&ns=WwSbSxlGiCHBa2ntzb-jFhIG&gir=yes&clen=64336234&dur=3673.721&lmt=1649344031395306&mt=1655114944&fvip=8&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=4532434&n=TkvNDsN9J8BhU_mhWo&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cspc%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRgIhAJoMnIXfyj1RXKwebfly-vbNTJKShgFtkEM7pgRdmvFMAiEA9WKxJ-NBN-7cLPaUpGowqVCD-rrX-e8CEwnHP75NxXo%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIgYqj1cDbzD3d-tJLP0pYeDYto9yvjFiItkCnNJZEcUqECIQDz0iCW7OhCNrzfmlGMOcJ8_0mj4ues_DbZRuYnGeldVw%3D%3D";
        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            Timer nettyTimer = client.getConfig().getNettyTimer();
            ChannelManager channelManager = new ChannelManager(client.getConfig(), nettyTimer);
//            client.prepareGet(ytUrl).execute(
//                    new WebSocketHandler(
//                            client.getConfig(),
//                            channelManager,
//                            new NettyRequestSender(client.getConfig(), channelManager, nettyTimer, new AsyncHttpClientState(new AtomicBoolean()))
//                    )
//            ).get();
        }
    }
}