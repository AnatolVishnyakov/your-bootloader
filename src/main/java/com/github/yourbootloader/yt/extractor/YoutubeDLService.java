package com.github.yourbootloader.yt.extractor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}
