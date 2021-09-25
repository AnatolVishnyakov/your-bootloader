package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StreamDownloader {
    private final String url;
    private final DownloadContext ctx;
    private final Map<String, Object> headers;
    private int count;
    private int retries;

    public StreamDownloader(String fileName, Map<String, Object> infoDict) {
        this.url = "https://r4---sn-jvhnu5g-c35s.googlevideo.com/videoplayback?expire=1632610007" +
                "\u0026ei=d1JPYaGrEt2GyQWUhbb4Bw" +
                "\u0026ip=109.252.67.101" +
                "\u0026id=o-AKAktU0_hQLazfkN0Lrc1-cxHJI5myp9IaeH2GX425tw" +
                "\u0026itag=248" +
                "\u0026aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C271%2C278%2C313%2C394%2C395%2C396%2C397%2C398%2C399%2C400%2C401" +
                "\u0026source=youtube" +
                "\u0026requiressl=yes" +
                "\u0026mh=u5" +
                "\u0026mm=31%2C29" +
                "\u0026mn=sn-jvhnu5g-c35s%2Csn-jvhnu5g-n8vy" +
                "\u0026ms=au%2Crdu" +
                "\u0026mv=m" +
                "\u0026mvi=4" +
                "\u0026pcm2cms=yes" +
                "\u0026pl=18" +
                "\u0026initcwndbps=1768750" +
                "\u0026vprv=1" +
                "\u0026mime=video%2Fwebm" +
                "\u0026ns=KmQXY4TD7cq8bH6Td3nMnF4G" +
                "\u0026gir=yes" +
                "\u0026clen=1083003184" +
                "\u0026dur=4723.840" +
                "\u0026lmt=1612131987728235" +
                "\u0026mt=1632588064" +
                "\u0026fvip=4" +
                "\u0026keepalive=yes" +
                "\u0026fexp=24001373%2C24007246" +
                "\u0026c=WEB" +
                "\u0026txp=5411222" +
                "\u0026n=cZnDVMd-pRgeQky" +
                "\u0026sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt" +
                "\u0026sig=AOq0QJ8wRQIgdI24SBsXaKdwr7LqEf-34a-BxiCriJjWiYp0WkDl7bACIQDUMFXhsplIyHCcCg_lSHIcTxFY3FxJJcRvO97ZeFSDuQ%3D%3D" +
                "\u0026lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpcm2cms%2Cpl%2Cinitcwndbps" +
                "\u0026lsig=AG3C_xAwRQIhAOwLsfAfK87sYYomU9RNK2qBYAk7OiCkdk7X5K0IxrA8AiA-miVMfkIMFC_45GNjuYR3L7BBhQQra6u4s5xyyezJnw%3D%3D";

        DownloadContext ctx = new DownloadContext();
        ctx.setFileName(fileName);
        ctx.setChunkSize(0);
        ctx.setOpenMode("wb");
        ctx.setResumeLen(0);
        ctx.setDataLen(null);
        ctx.setBlockSize(1024);
        ctx.setStartTime(Instant.now());
        // TODO возможность продолжить скачивание continue-dl
        ctx.setResume(ctx.getResumeLen() > 0);
        this.ctx = ctx;

        headers = new HashMap<>();
        headers.put("Youtubedl-no-compression", "True");
        if (infoDict.get("http_headers") != null) {
            infoDict.keySet().forEach(key -> {
                headers.put(key, infoDict.get(key));
            });
        }

        count = 0;
        retries = 3;
    }

    private void tryConnect() {
        int chunkSize = 0;


    }

    public void establishConnection(DownloadContext context) {

    }

    @SneakyThrows
    public void download() {
        ReadableByteChannel readableByteChannel = Channels.newChannel(new URI(url).toURL().openStream());
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\" + UUID.randomUUID() + ".mp3");
        FileChannel fileChannel = fileOutputStream.getChannel();

        int pos = 0;
        while (true) {
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, pos, 4 * 1024);

        }
    }

    public static void main(String[] args) {
        Map<String, Object> infoDict = new HashMap<>();
        infoDict.put("Youtubedl-no-compression", "True");
        infoDict.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.13 Safari/537.36");
        infoDict.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        infoDict.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        infoDict.put("Accept-Encoding", "gzip, deflate");
        infoDict.put("Accept-Language", "en-us,en;q=0.5");

        StreamDownloader downloader = new StreamDownloader(
                "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp4",
                infoDict
        );
        downloader.download();
    }
}
