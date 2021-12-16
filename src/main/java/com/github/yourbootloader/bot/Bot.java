package com.github.yourbootloader.bot;

import com.github.yourbootloader.config.BotConfig;
import com.github.yourbootloader.StreamDownloader;
import com.github.yourbootloader.YoutubePageParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final ApplicationContext context;

    public Bot(BotConfig config, ApplicationContext context) {
        this.config = config;
        this.context = context;
    }

    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: {}", update.getMessage());

        Message message = update.getMessage();
        String url = message.getText();

        YoutubePageParser youtubePageParser = context.getBean(YoutubePageParser.class, url);
        List<Map<String, Object>> formats = youtubePageParser.parse();

        Map<String, Object> format = formats.get(2);
        System.out.println(DataSize.ofBytes(((Integer) format.get("filesize")).longValue()).toMegabytes() + " Mb");
        StreamDownloader downloader = context.getBean(StreamDownloader.class, format.get("url"), UUID.randomUUID().toString(), Collections.emptyMap());
        downloader.realDownload(3);

//        update.getUpdateId();
//        SendMessage.SendMessageBuilder builder = SendMessage.builder();
//        String messageText;
//        String chatId;
//        if (update.getMessage() != null) {
//            chatId = update.getMessage().getChatId().toString();
//            builder.chatId(chatId);
//            messageText = update.getMessage().getText();
//        } else {
//            chatId = update.getChannelPost().getChatId().toString();
//            builder.chatId(chatId);
//            messageText = update.getChannelPost().getText();
//        }
//
//        if (messageText.contains("/hello")) {
//            builder.text("Привет");
//            try {
//                execute(builder.build());
//            } catch (TelegramApiException e) {
//                log.debug(e.toString());
//            }
//        }
//
//        if (messageText.contains("/chartId")) {
//            builder.text("ID Канала : " + chatId);
//            try {
//                execute(builder.build());
//            } catch (TelegramApiException e) {
//                log.debug(e.toString());
//            }
//        }
    }


    public String getBotUsername() {
        return config.getBotUserName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
