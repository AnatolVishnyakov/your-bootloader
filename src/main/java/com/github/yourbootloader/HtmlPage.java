package com.github.yourbootloader;

import lombok.Getter;

@Getter
public class HtmlPage {
    private final String videoId;
    private final String html;
    private final String urlh;

    public HtmlPage(String videoId, String html, String urlh) {
        this.videoId= videoId;
        this.html = html;
        this.urlh = urlh;
    }
}
