package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YtDownloadClientTest {

    private final YtDownloadClient ytDownloadClient;
    private final YDProperties ydProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    @Test
    void realDownload() throws Exception {
        ytDownloadClient.realDownload(
                3,
//                "https://speed.hetzner.de/100MB.bin",
                "https://rr2---sn-jvhnu5g-c35k.googlevideo.com/videoplayback?expire=1645980147&ei=k1UbYufHCoqM7ASr2YxI&ip=46.138.209.28&id=o-AN0v1cGnoTZGVBxVq0VaLyLQ50PnBgSuRRLWSA2UHS-f&itag=18&source=youtube&requiressl=yes&mh=0_&mm=31%2C29&mn=sn-jvhnu5g-c35k%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pl=21&initcwndbps=1308750&vprv=1&mime=video%2Fmp4&ns=Rc33wF6dhY7qrjG48-4KEw8G&gir=yes&clen=238936637&ratebypass=yes&dur=5100.402&lmt=1537070139281011&mt=1645958217&fvip=4&fexp=24001373%2C24007246&c=WEB&n=tHlHwPfyvWBt399&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIgNhbGw1_zmmGP0gk-jbNPrY1iYaJAQrk3I2ofUgWPamkCIQD5p0bykdRvDNxyZn1lfNqOoSjYyaYYhTtXCs5yNvcSog%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhALzEhJsGnnJ3Jn7j143CVXkLNdXr_bjL7D5sr19ZafOFAiEAoONlPrKqzkv9MFd9QR2QDs332cZ1v8OvlYH2N6j6aTk%3D",
                "test-6656",
                17844736L
        );
    }
}