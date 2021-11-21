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

    public StreamDownloader(String fileName, Map<String, Object> infoDict) {
        this.url = "https://r4---sn-jvhnu5g-c35l.googlevideo.com/videoplayback?expire=1637515460&ei=ZCyaYfHAKoWQyQXm1ZHYAQ&ip=2a00%3A1370%3A8127%3Ad967%3A6da5%3Ad65e%3A7d49%3A7b36&id=o-AIzDJYP_pQIbypMhuBxvxUuWS1VQCAKbsWhKaJqfBqUU&itag=22&source=youtube&requiressl=yes&mh=3L&mm=31%2C29&mn=sn-jvhnu5g-c35l%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=4&pl=51&initcwndbps=2120000&vprv=1&mime=video%2Fmp4&ns=CSsm_RRY9nwNla8N0FJckPgG&cnr=14&ratebypass=yes&dur=3835.147&lmt=1513857691676377&mt=1637493655&fvip=4&fexp=24001373%2C24007246&c=WEB&n=WJlxaTf3qJCBRi8s&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRgIhALNO4VNGpE7X5r7-DkafmYhLMKkSkpAFUYihv7m_8baZAiEA3a53V5WDvhvMqX060_Srt2_wIUXNZR0S9FT-Dm8mKsU%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAKArHzOzctiPJxfmgpJUlgeuUnnqzSN7HuOPgYvZTIFnAiEAtn_j7L1lzox4SeN19ppC3OX5LEDsS_jevK9zFMQ7PXQ%3D";

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
        String filePath = "D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\" + UUID.randomUUID() + ".mp3";
        FileOutputStream stream = new FileOutputStream(filePath);

        AsyncHttpClient client = Dsl.asyncHttpClient();
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
                "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp4",
                infoDict
        );
        downloader.download();
    }
}
