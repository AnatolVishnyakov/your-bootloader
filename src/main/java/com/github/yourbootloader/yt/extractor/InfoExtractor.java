package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.extractor.dto.Pair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.yourbootloader.yt.extractor.YoutubeIE._VALID_URL;

public abstract class InfoExtractor {

    boolean ready;
    String xForwardedForIp;
    final YoutubeDLService downloader;

    @Autowired
    protected InfoExtractor(YoutubeDLService downloader) {
        this.ready = false;
        this.xForwardedForIp = null;
        this.downloader = downloader;
    }

    protected JSONObject parseJson(String json, String videoId) {
        return new JSONObject(json);
    }

    protected String searchRegex(List<String> patterns, String string, String name) {
        for (String p : patterns) {
            Pattern pattern = Pattern.compile(p);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        throw new NotImplementedException();
    }

    protected abstract boolean suitable(String url);

    protected String matchId(String url) {
        Pattern pattern = Pattern.compile(_VALID_URL);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group("id");
        }
        throw new RuntimeException("Not match!");
    }

    protected String httpScheme() {
        return downloader.getPreferInsecure() != null
                ? "http"
                : "https";
    }

    protected String downloadWebpage(String url, String videoId, int tries) {
        boolean success = false;
        int tryCount = 0;
        Pair<String, ResponseEntity<String>> res = null;

        while (!success) {
            try {
                res = this.downloadWebpageHandle(url, videoId);
                success = true;
            } catch (Exception exc) {
                tryCount += 1;
                if (tryCount >= tries) {
                    break;
                }
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return res.getOne();
    }

    private Pair<String, ResponseEntity<String>> downloadWebpageHandle(String url, String videoId) {
        // TODO Strip hashes from the URL (#1038)
        ResponseEntity<String> urlh = this.requestWebpage(url, videoId);
        if (urlh == null) {
            return null;
        }
        String content = webpageReadContent(urlh, url, videoId);
        return new Pair<>(content, urlh);
    }

    private String webpageReadContent(ResponseEntity<String> urlh, String url, String videoId) {
        String contentType = Objects.requireNonNull(urlh.getHeaders().getContentType()).toString();
        byte[] webpageBytes = Objects.requireNonNull(urlh.getBody()).getBytes();
        String encoding = this.guessEncodingFromContent(contentType, webpageBytes);
        String content = new String(webpageBytes, Charset.forName(encoding));

        this.checkBlocked(content);

        return content;
    }

    private void checkBlocked(String content) {
        // TODO implements
    }

    private String guessEncodingFromContent(String contentType, byte[] webpageBytes) {
        String content = new String(webpageBytes);
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

    private ResponseEntity<String> requestWebpage(String url, String videoId) {
        // TODO CertificateError
        return downloader.urlopen(url);
    }

    public Map<String, Object> extract(String url) {
        Map<String, Object> info = Collections.emptyMap();
        for (int i = 0; i < 2; i++) {
            info = realExtract(url);
        }
        return info;
    }


    public abstract Map<String, Object> realExtract(String url);
}
