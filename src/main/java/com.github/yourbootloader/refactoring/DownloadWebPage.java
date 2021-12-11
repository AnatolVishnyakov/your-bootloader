package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
public class DownloadWebPage {
    private final String url;

    public DownloadWebPage(YoutubeUrl youtubeUrl) {
        this.url = youtubeUrl.getWebPageUrl() + "&bpctr=9999999999&has_verified=1";
    }

    @SneakyThrows
    public String download() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        MediaType contentType = response.getHeaders().getContentType();
        String encoding = parseEncoding(response.getBody(), contentType);
        String content = new String(Objects.requireNonNull(response.getBody()).getBytes(encoding));
        checkBlocked(content);
        return content;
    }

    private String parseEncoding(String content, @Nullable MediaType contentType) {
        if (contentType != null) {
            Pattern pattern = Pattern.compile("[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+\\s*;\\s*charset=(.+)");
            Matcher matcher = pattern.matcher(contentType.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        Pattern pattern = Pattern.compile("<meta[^>]+charset=[\\'\"]?([^\\'\")]+)[ /\\'\">]");
        Matcher matcher = pattern.matcher(content.substring(0, 1024));
        if (matcher.find()) {
            return matcher.group(1);
        } else if (content.startsWith("\\xff\\xfe")) {
            return "UTF-16";
        } else {
            return "UTF-8";
        }
    }

    private void checkBlocked(String content) {
        // TODO implements checker
        String firstBlock = content.substring(0, 512);
        if (firstBlock.contains("<title>Access to this site is blocked</title>") && firstBlock.contains("Websense")) {
            String msg = "Access to this webpage has been blocked by Websense filtering software in your network.";
            Pattern pattern = Pattern.compile("<iframe src=\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                msg += format(" Visit %s for more details", matcher.group(1));
            }
            throw new RuntimeException(msg);
        }

        if (firstBlock.contains("<title>The URL you requested has been blocked</title>")) {
            String msg = "Access to this webpage has been blocked by Indian censorship. " +
                    "Use a VPN or proxy server (with --proxy) to route around it.";
            Pattern pattern = Pattern.compile("</h1><p>(.*?)</p>");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                msg += format(" (Message: \"%s\")", matcher.group(1).replace("\n", " "));
            }
            throw new RuntimeException(msg);
        }

        if (firstBlock.contains("<title>TTK :: Доступ к ресурсу ограничен</title>") && firstBlock.contains("blocklist.rkn.gov.ru")) {
            throw new RuntimeException("'Access to this webpage has been blocked by decision of the Russian government. " +
                    "Visit http://blocklist.rkn.gov.ru/ for a block reason.");
        }
    }

    public static void main(String[] args) {
        YoutubeUrl youtubeUrl = new YoutubeUrl("https://www.youtube.com/watch?v=nui3hXzcbK0");
        DownloadWebPage downloadWebPage = new DownloadWebPage(youtubeUrl);
        String page = downloadWebPage.download();
        System.out.println();
    }
}
