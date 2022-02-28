package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YoutubeSignatureTest {

    private static final String DIGITS = "0123456789";
    private static final String ASCII_LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
    private static final String WHITESPACE = " \\t\\n\\r\\v\\f";
    private static final String PRINTABLE = DIGITS + ASCII_LETTERS + PUNCTUATION + WHITESPACE;

    private final YoutubeIE youtubeIE;
    private final ResourceLoader resourceLoader;
    private final RestTemplate restTemplate;

    public static Stream<Arguments> signatures() {
        return Stream.of(
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-vflHOr_nV.js",
                        86,
                        ">=<;:/.-[+*)('&%$#\"!ZYX0VUTSRQPONMLKJIHGFEDCBA\\yxwvutsrqponmlkjihgfedcba987654321"
                ),
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-vfldJ8xgI.js",
                        85,
                        "3456789a0cdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRS[UVWXYZ!\"#$%&'()*+,-./:;<=>?@"
                ),
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-vfle-mVwz.js",
                        90,
                        "]\\[@?>=<;:/.-,+*)('&%$#\"hZYXWVUTSRQPONMLKJIHGFEDCBAzyxwvutsrqponmlkjiagfedcb39876"
                ),
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vfl0Cbn9e.js",
                        84,
                        "O1I3456789abcde0ghijklmnopqrstuvwxyzABCDEFGHfJKLMN2PQRSTUVW@YZ!\"#$%&'()*+,-./:;<="
                ),
//                Arguments.of(
//                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vflXGBaUN.js",
//                        "2ACFC7A61CA478CD21425E5A57EBD73DDC78E22A.2094302436B2D377D14A3BBA23022D023B8BC25AA",
//                        "A52CB8B320D22032ABB3A41D773D2B6342034902.A22E87CDD37DBE75A5E52412DC874AC16A7CFCA2"
//                ),
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vflBb0OQx.js",
                        84,
                        "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQ0STUVWXYZ!\"#$%&'()*+,@./:;<=>"
                ),
                Arguments.of(
                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vfl9FYC6l.js",
                        83,
                        "123456789abcdefghijklmnopqr0tuvwxyzABCDETGHIJKLMNOPQRS>UVWXYZ!\"#$%&'()*+,-./:;<=F"
                )
//                Arguments.of(
//                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vflCGk6yw/html5player.js",
//                        "4646B5181C6C3020DF1D9C7FCFEA.AD80ABF70C39BD369CCCAE780AFBB98FA6B6CB42766249D9488C288",
//                        "82C8849D94266724DC6B6AF89BBFA087EACCD963.B93C07FBA084ACAEFCF7C9D1FD0203C6C1815B6B"
//                ),
//                Arguments.of(
//                        "https://s.ytimg.com/yts/jsbin/html5player-en_US-vflKjOTVq/html5player.js",
//                        "312AA52209E3623129A412D56A40F11CB0AF14AE.3EE09501CB14E3BCDC3B2AE808BF3F1D14E7FBF12",
//                        "112AA5220913623229A412D56A40F11CB0AF14AE.3EE0950FCB14EEBCDC3B2AE808BF331D14E7FBF3"
//                )
        );
    }

    @ParameterizedTest
    @MethodSource("signatures")
    @SneakyThrows
    void testSignature(String url, Object sigInput, String expectedSig) {
        Matcher matcher = Pattern.compile(".*-([a-zA-Z0-9_-]+)(?:/watch_as3|/html5player)?\\.[a-z]+$").matcher(url);
        if (!matcher.find()) {
            throw new IllegalStateException();
        }

        String testId = matcher.group(1);
        String baseName = format("player-%s.js", testId);

        Path folderJsData = Paths.get("src/test/resources/testdata/");
        if (!Files.exists(folderJsData)) {
            Files.createDirectory(folderJsData);
        }

        Path jsFile = folderJsData.resolve(baseName);
        if (Files.deleteIfExists(jsFile)) {
            Files.createFile(jsFile);
        }

        String jsCode = restTemplate.getForObject(url, String.class);
        Files.write(jsFile, Objects.requireNonNull(jsCode).getBytes(StandardCharsets.UTF_8));

        String jscode = String.join("", Files.readAllLines(jsFile));

        Function<String, String> func = youtubeIE.parseSigJs(jscode);
        String srcSig = (sigInput instanceof Integer)
                ? PRINTABLE.substring(0, ((Integer) sigInput))
                : ((String) sigInput);
        String gotSig = func.apply(srcSig);
        assertEquals(expectedSig, gotSig);
    }
}
