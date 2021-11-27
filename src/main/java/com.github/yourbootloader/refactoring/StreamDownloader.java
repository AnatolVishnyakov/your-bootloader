package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class StreamDownloader {
    private final String url;
    private final DownloadContext ctx;
    private final Map<String, Object> headers;
    private int count;
    private int retries;

    public StreamDownloader(String url, String fileName, Map<String, Object> infoDict) {
        this.url = url;

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
        String filePath = "D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\archive\\" + UUID.randomUUID() + ".mp3";
        FileOutputStream stream = new FileOutputStream(filePath);

        AsyncHttpClient client = Dsl.asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setRequestTimeout(600_000).setReadTimeout(600_000).setConnectTimeout(600_000));
        client.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {
            @Override
            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                printContentWritten();
                stream.getChannel().write(bodyPart.getBodyByteBuffer());
                return State.CONTINUE;
            }

            @Override
            public FileOutputStream onCompleted(Response response) {
                System.out.println("Completed download!");
                return stream;
            }

            @SneakyThrows
            private void printContentWritten() {
                long fileSize = Files.size(new File(filePath).toPath());
                DataSize dataSize = DataSize.ofBytes(fileSize);
                if (dataSize.toMegabytes() == 0) {
                    log.info(dataSize.toKilobytes() + " Kb");
                } else {
                    log.info(dataSize.toMegabytes() + " Mb (" + dataSize.toKilobytes() + " Kb)");
                }
            }
        });
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
                "https://r18---sn-n8v7znsz.googlevideo.com/videoplayback?expire=1638062525&ei=XYWiYdj6HYTryQXg2KYI&ip=2a00%3A1370%3A8174%3Ab2a6%3A5c91%3Ae269%3A4c96%3A7b06&id=o-AJQAb1Xh5TH7lMP9F97ZXjZ1o5b0Asc2tBJ1UNa48M2z&itag=22&source=youtube&requiressl=yes&mh=-P&mm=31%2C29&mn=sn-n8v7znsz%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=18&pl=46&initcwndbps=1772500&vprv=1&mime=video%2Fmp4&ns=lrkd2KXSQzRwfK5pxwuej2gG&cnr=14&ratebypass=yes&dur=4278.555&lmt=1538268971961146&mt=1638040627&fvip=2&fexp=24001373%2C24007246&c=WEB&txp=2311222&n=Px9uEPP2STBozSvg&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRgIhAK5nQv355GkyfbDqIm3tqCqVOEGjqp7DIT5RkM4Hhh0bAiEAxFS5nBB04MQOD9wIALT5OJpT-duOne69-FkrK0YZkEc%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhAMJaAutP-YHxma2LBd9bPOXAuPGttMLjhcDYxND9Xk9fAiBywLMyqXlSwcvnlWULaMl8HKG5o0Oa70Y1s4p8EW4FGA%3D%3D",
                "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp4",
                infoDict
        );
        downloader.download();
    }
}
