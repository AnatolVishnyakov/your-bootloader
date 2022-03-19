package com.github.yourbootloader.yt.extractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfoExtractorTest {

    private final InfoExtractor extractor = new InfoExtractor(null) {
        @Override
        protected boolean suitable(String url) {
            return false;
        }

        @Override
        public YtVideoInfo realExtract(String url) {
            return null;
        }
    };

    @Test
    void matchId() {
        String id = extractor.matchId("https://youtu.be/XfxaQclK123");
        assertEquals("XfxaQclK123", id);
    }
}