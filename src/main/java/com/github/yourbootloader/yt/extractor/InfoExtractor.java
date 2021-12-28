package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
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
        throw new NotImplementedException();
    }

    protected String searchRegex(List<String> pattern, String string, String name) {
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

    protected String downloadWebpage(String url, String videoId) {
        throw new MethodNotImplementedException();
    }
}
