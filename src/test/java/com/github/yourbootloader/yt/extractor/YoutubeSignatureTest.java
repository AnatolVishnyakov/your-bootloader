package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Files;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YoutubeSignatureTest {

    private final YoutubeIE youtubeIE;
    private final ResourceLoader resourceLoader;

    @Test
    @SneakyThrows
    void testSignature() {
        String url = "https://s.ytimg.com/yts/jsbin/html5player-en_US-vflKjOTVq/html5player.js";
        String sigInput = "312AA52209E3623129A412D56A40F11CB0AF14AE.3EE09501CB14E3BCDC3B2AE808BF3F1D14E7FBF12";
        String expectedSig = "112AA5220913623229A412D56A40F11CB0AF14AE.3EE0950FCB14EEBCDC3B2AE808BF331D14E7FBF3";

        Matcher matcher = Pattern.compile(".*-([a-zA-Z0-9_-]+)(?:/watch_as3|/html5player)?\\.[a-z]+$").matcher(url);
        if (!matcher.find()) {
            throw new IllegalStateException();
        }
        String testId = matcher.group(1);
        String baseName = format("player-%s.js", testId);
        Resource jsFileResource = resourceLoader.getResource("classpath:" + baseName);
        String jscode = String.join("", Files.readAllLines(jsFileResource.getFile().toPath()));

        Function<String, String> func = youtubeIE.parseSigJs(jscode);
        String gotSig = func.apply(sigInput);
        assertEquals(expectedSig, gotSig);
    }
}
