package com.github.yourbootloader.refactoring;

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
        Map<String, Object> infoDict = new HashMap<>();
        infoDict.put("Youtubedl-no-compression", "True");
        infoDict.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.13 Safari/537.36");
        infoDict.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        infoDict.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        infoDict.put("Accept-Encoding", "gzip, deflate");
        infoDict.put("Accept-Language", "en-us,en;q=0.5");

        String url = "https://r2---sn-jvhnu5g-c35z.googlevideo.com/videoplayback?expire=1638123708&ei=XHSjYZzpFYaW7QSq8qIQ&ip=46.138.209.1&id=o-AHtoCF1TqFKYVRvxMO7C14J0_bDnuXlEuiNeuQGuIDxr&itag=22&source=youtube&requiressl=yes&mh=-P&mm=31%2C29&mn=sn-jvhnu5g-c35z%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pl=20&initcwndbps=1793750&vprv=1&mime=video%2Fmp4&ns=eQlhPyyaKn0xqroC-uQbfkkG&cnr=14&ratebypass=yes&dur=4278.555&lmt=1538268971961146&mt=1638101816&fvip=2&fexp=24001373%2C24007246&c=WEB&txp=2311222&n=7F6UcgOWMNaYwDdt&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRAIgcVwSdtXfWFwcV0wjCZHTMi9747sL6UugCf2XpOmvJM0CIHWESzB2nmko0bUGyHwVZ7LZqN8a6MKMlAw38lm1c4B2&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRAIgNvKM3llVn6hur22TCkopyYZbDwEGJo2jdSHOuPxncT8CIFjIbd0XCkv9N0QaoC85To2P1v0ZWXkE9PSxDY6z2Q5V";
        String fileName = "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp3";

        StreamDownloader downloader = beanFactory.getBean(StreamDownloader.class, url, fileName);
        downloader.realDownload(3);
    }
}