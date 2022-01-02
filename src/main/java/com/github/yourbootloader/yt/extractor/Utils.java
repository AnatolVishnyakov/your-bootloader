package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.extractor.dto.Pair;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Utils {

    public Pair<String, Map<Object, Object>> unsmuggleUrl(String smugUrl) {
        if (!smugUrl.contains("#__youtubedl_smuggle")) {
            return new Pair<>(smugUrl, Collections.emptyMap());
        }
        throw new UnsupportedOperationException("Not implemented unsmuggle");
    }


    public static int parseDuration(String duration) {
        return 0;
    }

    @SneakyThrows
    public static Map<String, String> parseQs(String signatureCipher) {
        Map<String, String> result = new HashMap<>();
        String[] queries = signatureCipher.split("&");
        for (String query : queries) {
            String[] params = query.split("=");
            result.put(params[0], URLDecoder.decode(params[1], StandardCharsets.UTF_8.toString()));
        }
        return result;
    }
}
