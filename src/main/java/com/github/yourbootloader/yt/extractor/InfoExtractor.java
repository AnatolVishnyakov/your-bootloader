package com.github.yourbootloader.yt.extractor;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

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
}
