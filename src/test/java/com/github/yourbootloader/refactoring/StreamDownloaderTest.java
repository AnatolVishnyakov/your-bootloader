package com.github.yourbootloader.refactoring;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StreamDownloaderTest {

    @Autowired
    BeanFactory beanFactory;

    @BeforeEach
    void init() {
        File temp = new File("D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\archive");
        if (temp.exists() && Arrays.stream(Objects.requireNonNull(temp.listFiles())).findAny().isPresent()) {
            for (File file : Objects.requireNonNull(temp.listFiles())) {
                file.deleteOnExit();
            }
        }
    }

    @Test
    void realDownload() {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.13 Safari/537.36");
        headers.add("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Accept-Language", "en-us,en;q=0.5");

        Map<String, Object> infoDict = new HashMap<>();
        infoDict.put("http_headers", headers);

        String url = "https://r2---sn-jvhnu5g-c35z.googlevideo.com/videoplayback?expire=1638637820&ei=nEyrYciJDaKA0u8P_seqeA&ip=46.138.209.188&id=o-AAGwThEpWZ1plpiFKXWh4PaDT18SabXV9lvZeGE1sY5r&itag=22&source=youtube&requiressl=yes&mh=-P&mm=31%2C29&mn=sn-jvhnu5g-c35z%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pcm2cms=yes&pl=20&initcwndbps=2353750&vprv=1&mime=video%2Fmp4&ns=Ax79sKcYFkVvjAQNTQ7lp68G&cnr=14&ratebypass=yes&dur=4278.555&lmt=1538268971961146&mt=1638615887&fvip=2&fexp=24001373%2C24007246&c=WEB&txp=2311222&n=HRIJxZUtVo7z99-76V&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIhALg5p6gPmz-aYtYOYlVhJFPfbDFN2X6HFXY4qbr8uMPgAiBhPdF4VAv4MlS2gWgZAO0uBzn8rLtFIHoWLtLvHNw5ag%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpcm2cms%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRQIhALaPHq1PQxCO6MhfznDd215gcyvSnGtW2-FYEHuvMioaAiAKgBqen9isV_XaVvU52w6KRPWDu8Vji0iK3W5rXSPUEg%3D%3D";
        String fileName = "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp3";

        StreamDownloader downloader = beanFactory.getBean(StreamDownloader.class, url, fileName, infoDict);
        downloader.realDownload(3);
    }
}