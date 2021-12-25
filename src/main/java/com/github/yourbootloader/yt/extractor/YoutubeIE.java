package com.github.yourbootloader.yt.extractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YoutubeIE extends YoutubeBaseInfoExtractor {

    @Autowired
    protected YoutubeIE(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
    }
}
