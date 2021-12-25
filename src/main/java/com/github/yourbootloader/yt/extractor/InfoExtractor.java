package com.github.yourbootloader.yt.extractor;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class InfoExtractor {

    final YoutubeDLService downloader;

    @Autowired
    protected InfoExtractor(YoutubeDLService downloader) {
        this.downloader = downloader;
    }
}
