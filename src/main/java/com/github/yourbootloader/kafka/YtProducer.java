package com.github.yourbootloader.kafka;

import com.github.yourbootloader.config.YtKafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
//@Component
@RequiredArgsConstructor
public class YtProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void produce(T message) {
        String partition = String.valueOf(ThreadLocalRandom.current().nextInt(YtKafkaConfig.YT_PARTITION_COUNT));
        kafkaTemplate.send(YtKafkaConfig.YT_TOPIC, partition, message);
        log.info("Message {} produce in kafka.", message);
    }
}
