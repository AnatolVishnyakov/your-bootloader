package com.github.yourbootloader.bot;

import com.github.yourbootloader.config.BotConfig;
import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
import com.github.yourbootloader.yt.download.StreamDownloader;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final ApplicationContext context;
    private Long chatId;

    public Bot(BotConfig config, ApplicationContext context) {
        this.config = config;
        this.context = context;
    }

    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: {}", update.getMessage());

        Message message = update.getMessage();
        chatId = message.getChatId();
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
        sendNotification(message.getChatId(), getFilesize(format));
        StreamDownloader downloader = context.getBean(StreamDownloader.class);
        File downloadFile;
        try {
            downloadFile = downloader.realDownload(3,
                    ((String) format.get("url")),
                    ((String) format.get("title")),
                    ((Integer) format.get("filesize")).longValue(),
                    ((DefaultHttpHeaders) format.get("http_headers"))
            );
        } catch (Exception e) {
            sendNotification(message.getChatId(), "Возникла непредвиденная ошибка: " + e.getMessage());
            return;
        }

        SendAudio SendAudioRequest = new SendAudio();
        SendAudioRequest.setChatId(String.valueOf(message.getChatId()));
        SendAudioRequest.setAudio(new InputFile(downloadFile));
        SendAudioRequest.setCaption(downloadFile.getName());
        try {
            execute(SendAudioRequest);
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
            sendNotification(message.getChatId(), "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
        }
    }

    public Long getChatId() {
        return chatId;
    }

    private String getFilesize(Map<String, Object> format) {
        DataSize filesize = DataSize.ofBytes(((Integer) format.get("filesize")).longValue());
        if (filesize.toMegabytes() != 0) {
            return filesize.toMegabytes() + " Mb";
        }
        return filesize.toKilobytes() + " Kb";
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
