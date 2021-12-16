package com.github.yourbootloader.bot;

import com.github.yourbootloader.config.BotConfig;
import com.github.yourbootloader.yt.StreamDownloader;
import com.github.yourbootloader.yt.YoutubePageParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

        if (url.equals("/test")) {
            url = "https://www.youtube.com/watch?v=_zJrhqUBF5o";
        }

        YoutubePageParser youtubePageParser = context.getBean(YoutubePageParser.class, url);
        List<Map<String, Object>> formats = youtubePageParser.parse();

        Map<String, Object> format = formats.stream()
                .filter(m -> m.get("ext").equals("m4a") && m.get("format_note").equals("tiny"))
                .findFirst().orElseThrow(RuntimeException::new);

        sendNotification(message.getChatId(), "Идет скачивание аудио: " + format.get("title"));
        System.out.println(DataSize.ofBytes(((Integer) format.get("filesize")).longValue()).toMegabytes() + " Mb");
        StreamDownloader downloader = context.getBean(StreamDownloader.class, format.get("url"), format.get("title"), Collections.emptyMap());
        File downloadFile = downloader.realDownload(3);

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(String.valueOf(message.getChatId()));
        sendDocumentRequest.setDocument(new InputFile(downloadFile));
        sendDocumentRequest.setCaption(downloadFile.getName());
        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
            sendNotification(message.getChatId(), "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void sendNotification(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        execute(message);
    }

    public String getBotUsername() {
        return config.getBotUserName();
    }

    public String getBotToken() {
        return config.getToken();
    }
}
