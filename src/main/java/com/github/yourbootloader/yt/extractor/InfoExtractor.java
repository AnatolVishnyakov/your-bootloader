package com.github.yourbootloader.yt.extractor;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class InfoExtractor {

    final YoutubeDLService downloader;

    @Autowired
    protected InfoExtractor(YoutubeDLService downloader) {
        this.downloader = downloader;
    }

    protected JSONObject parseJson(String json, String videoId) {
        throw new NotImplementedException();
    }

    protected String searchRegex(String pattern, String string, String name) {
        throw new NotImplementedException();
    }
}
