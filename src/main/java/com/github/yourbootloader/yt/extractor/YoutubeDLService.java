package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class YoutubeDLService {

    private final RestTemplate restTemplate;
    private Map<String, Object> params;

    public YoutubeDLService() {
        this.restTemplate = new RestTemplate();
        this.params = new HashMap<String, Object>() {{
            put("youtube_include_dash_manifest", true);
        }};
    }

    public String getPreferInsecure() {
        return null;
    }

    public ResponseEntity<String> urlopen(String url) {
        return restTemplate.getForEntity(url, String.class);
    }

    public Object getParams(String key) {
        return params.get(key);
    }

    public Map<String, Object> extractInfo(String url, boolean isDownload, Map<String, Object> extraInfo, boolean isProcess) {
//        Map<String, Object> ieResult = youtubeIE.extract(url);
//        if (ieResult == null || ieResult.isEmpty()) {
//            return Collections.emptyMap();
//        }
//
//        if (isProcess) {
//            return processIeResult(ieResult, isDownload, extraInfo);
//        }

        return Collections.emptyMap();
    }

    private Map<String, Object> processIeResult(Map<String, Object> ieResult, boolean isDownload, Map<String, Object> extraInfo) {
        String resultType = (String) ieResult.getOrDefault("_type", "video");
        switch (resultType) {
            case "video":
                return processVideoResult(ieResult, isDownload);
            default:
                throw new MethodNotImplementedException("Result type doesnt implements!");
        }
    }

    private Map<String, Object> processVideoResult(Map<String, Object> infoDict, boolean isDownload) {
        String type = (String) infoDict.getOrDefault("_type", "video");
        if (!type.equals("video")) {
            throw new RuntimeException("Incorrect type");
        }

        if (!infoDict.containsKey("id")) {
            throw new RuntimeException("Missing \"id\" field in extractor result");
        }
        if (!infoDict.containsKey("title")) {
            throw new RuntimeException("Missing \"title\" field in extractor result");
        }

        // TODO many skip

        return Collections.emptyMap();
    }
}
