package com.github.yourbootloader;

import com.github.yourbootloader.refactoring.StreamDownloader;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

@ConfigurationPropertiesScan
@SpringBootApplication
public class YourBootLoaderApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(YourBootLoaderApplication.class, args);
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.13 Safari/537.36");
        headers.add("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.add("Accept-Encoding", "gzip, deflate");
        headers.add("Accept-Language", "en-us,en;q=0.5");

        Map<String, Object> infoDict = new HashMap<>();
        infoDict.put("http_headers", headers);

        String url = "https://r2---sn-jvhnu5g-c35z.googlevideo.com/videoplayback?expire=1638730618&ei=GresYfTnLJaUyQWAqK7YDg&ip=46.138.209.188&id=o-AO0CyttZQB3zrUY_NQanZ13JfM4wYf0DwYv8gUL5DSWe&itag=22&source=youtube&requiressl=yes&mh=-P&mm=31%2C29&mn=sn-jvhnu5g-c35z%2Csn-jvhnu5g-n8ve7&ms=au%2Crdu&mv=m&mvi=2&pl=20&initcwndbps=2108750&vprv=1&mime=video%2Fmp4&ns=sZX1Sq1kljOQIjeYzNOmr1kG&cnr=14&ratebypass=yes&dur=4278.555&lmt=1538268971961146&mt=1638708540&fvip=2&fexp=24001373%2C24007246&c=WEB&txp=2311222&n=-6wcPfj_kG9UAH3TJr&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Ccnr%2Cratebypass%2Cdur%2Clmt&sig=AOq0QJ8wRQIhAIcNiW7n0pqopRmwiKqcrYiOmOmKcN9uNV-_o0ZnTHZ7AiA2jQINM9Wgr50SccBPQLn3nmZBXuZhZgZ4PXL_MZMzOg%3D%3D&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRAIgWB8IJ1CZRntSLPHx-Hh8mwT_XmLMK0HrAQ_IMSl9V9sCIHAzrNPuRBG3fLT9EBq57n-An-pynsRFLWlBPR9eL_TX";
        String fileName = "Productivity Music — Maximum Efficiency for Creators, Programmers, Designers-C4MpzSMkinw.mp3";

        StreamDownloader downloader = context.getBean(StreamDownloader.class, url, fileName, infoDict);
        downloader.realDownload(3);
    }
}