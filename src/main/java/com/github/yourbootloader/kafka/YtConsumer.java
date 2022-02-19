package com.github.yourbootloader.kafka;

import com.github.yourbootloader.bot.BotCommandService;
import com.github.yourbootloader.bot.dto.YtMessage;
import com.github.yourbootloader.config.YtKafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.telegram.telegrambots.meta.api.objects.Chat;

@Slf4j
//@Component
//@EnableKafka
@RequiredArgsConstructor
public class YtConsumer {

    private final BotCommandService botCommandService;

    @KafkaListener(topics = YtKafkaConfig.YT_TOPIC, containerFactory = "kafkaListenerYtMessageContainerFactory")
    public void ytMessageListener(ConsumerRecord<String, YtMessage> consumerRecord, @Payload YtMessage payload) {
        log.info("Message {} consume from kafka.", payload);
        Chat chat = payload.getChat();
        String url = payload.getUrl();
        String title = payload.getTitle();
        Long filesize = payload.getFilesize();
//        botCommandService.download(chat, url, title, filesize);
    }
}
