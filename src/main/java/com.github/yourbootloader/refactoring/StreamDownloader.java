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
        this.url = "https://r4---sn-jvhnu5g-c35s.googlevideo.com/videoplayback?expire=1633303271&ei=h-ZZYcy4MKf57QTY9pf4Cw&ip=109.252.67.101&id=o-ACoHRlnMHUyqOU3Km6m-6t-zHaGfL1inATGy36k4sUwz&itag=251&source=youtube&requiressl=yes&mh=u5&mm=31%2C29&mn=sn-jvhnu5g-c35s%2Csn-jvhnu5g-n8vy&ms=au%2Crdu&mv=m&mvi=4&pcm2cms=yes&pl=22&initcwndbps=1781250&vprv=1&mime=audio%2Fwebm&ns=baBvFgwX-3B8IypmK61I5R4G&gir=yes&clen=80099367&dur=4723.861&lmt=1609712300865486&mt=1633281430&fvip=4&keepalive=yes&fexp=24001373%2C24007246&c=WEB&txp=5532434&n=eIjxKzlssUf91G7E&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRgIhALkxPW9JlSSr49eil0wWkc3TuiXFBeb4TGJ0TX0K2McjAiEA2dVF93yRzO-aqKGf-n_1OvTEpWS3FWh0YBzCp4eH1cw%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpcm2cms%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAMCSGEot23CIdZF9m0AsXnaACWFeU6qjF25Os2AE7fi7AiEAo1X1JpfiJ9BJ02dtWsbw_PVxNjwj1AuxDH8Q1Av53zA%3D";

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
            fileChannel.transferFrom(readableByteChannel, pos, 4 * 1024);
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
