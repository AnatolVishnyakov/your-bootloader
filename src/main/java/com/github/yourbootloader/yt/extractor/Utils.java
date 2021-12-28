package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.extractor.dto.Pair;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Map;

@UtilityClass
public class Utils {

    public Pair<String, Map<Object, Object>> unsmuggleUrl(String smugUrl) {
        if (!smugUrl.contains("#__youtubedl_smuggle")) {
            return new Pair<>(smugUrl, Collections.emptyMap());
        }
        throw new UnsupportedOperationException("Not implemented unsmuggle");
    }
}
