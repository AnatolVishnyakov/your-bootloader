package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubeIETest {

    private final YoutubeIE youtubeIE;
    @MockBean
    private YoutubeDLService youtubeDLService;

    @Test
    @SneakyThrows
    void extractorInfo() {
        String url = "https://www.youtube.com/watch?v=OCJi5hVdiZU";
        Path folderData = Paths.get("src/test/resources/testdata/");

        String webPageUrl = "https://www.youtube.com/watch?v=OCJi5hVdiZU&bpctr=9999999999&has_verified=1";
        String webPageContent = new String(
                Files.readAllBytes(folderData.resolve("webpages").resolve("yt_web_page.html"))
        );
        Mockito.when(youtubeDLService.urlopen(webPageUrl))
                .thenReturn(
                        ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "text/html; charset=utf-8")
                                .body(webPageContent)
                );

        String playerUrl = "https://www.youtube.com/s/player/0abde7de/player_ias.vflset/en_US/base.js";
        String playerContent = new String(
                Files.readAllBytes(folderData.resolve("player_code").resolve("yt_player_base.js"))
        );
        Mockito.when(youtubeDLService.urlopen(playerUrl))
                .thenReturn(
                        ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "text/javascript")
                                .body(playerContent)
                );
        Mockito.when(youtubeDLService.getParams("youtube_include_dash_manifest"))
                .thenReturn(false);

        YtVideoInfo ytVideoInfo = youtubeIE.realExtract(url);
        assertEquals("OCJi5hVdiZU", ytVideoInfo.getId());
        assertEquals("Architects - \"Animals\" (Orchestral Version) - Live at Abbey Road", ytVideoInfo.getTitle());
        assertEquals(23, ytVideoInfo.getFormats().size());
    }
}