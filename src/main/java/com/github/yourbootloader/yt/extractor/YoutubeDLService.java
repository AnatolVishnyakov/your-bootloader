package com.github.yourbootloader.yt.extractor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class YoutubeDLService {

    private final RestTemplate restTemplate;

    public YoutubeDLService() {
        this.restTemplate = new RestTemplate();
    }

    public String getPreferInsecure() {
        return null;
    }

    public ResponseEntity<String> urlopen(String url) {
        return restTemplate.getForEntity(url, String.class);
    }
}
