package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class StreamDownloaderTest {

    private final StreamDownloader streamDownloader;
    private final YDProperties ydProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    @Test
    void realDownload() throws Exception {
        streamDownloader.realDownload(
                3,
                "https://rr6---sn-jvhnu5g-c35d.googlevideo.com/videoplayback?expire=1642968745&ei=SWLtYYrPKMnS7QT4naywBQ&ip=46.138.209.28&id=o-AHCgr7X8j8GQeqfV46b-eansYYJgendVqJyJ_-MnF-ZT&itag=249&source=youtube&requiressl=yes&mh=CN&mm=31%2C29&mn=sn-jvhnu5g-c35d%2Csn-jvhnu5g-n8vy&ms=au%2Crdu&mv=m&mvi=6&pl=19&initcwndbps=1955000&vprv=1&mime=audio%2Fwebm&ns=pWF6VSAf4kMJvIYpTyv2LQ0G&gir=yes&clen=25915277&dur=3947.941&lmt=1642750216255752&mt=1642946943&fvip=8&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=4431432&n=YDKPlsrTAxTJl-JNP6ex&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRQIgMIjjLixbCpza0JStavR6wWddD1NCy_YGFEeMqhxZT1ICIQD4nM9u9xsTUfP4krPUAHpSgUtqtfPC0nzdSkxGqEGYxg%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAKtNCaeyWEfRTfrnPnPs0GVExVLN85CT6YLiITbed-tXAiEAp0lHNuxW3rXGd8DuaqmbZ1jLS5EaBx1OKzIKUcMw1dg%3D",
                "test",
                25915277L,
                new DefaultHttpHeaders()
        );
    }
}