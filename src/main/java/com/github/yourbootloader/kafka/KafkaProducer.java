package com.github.yourbootloader.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    public static final String YOUTUBE_URL_TOPIC = "youtube-url-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void produce(String message) {
        log.info(String.format("#### -> Producing message -> %s", message));
        kafkaTemplate.send(YOUTUBE_URL_TOPIC, message);
    }
}
