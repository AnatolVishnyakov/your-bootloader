package com.github.yourbootloader.bot;

import com.github.yourbootloader.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final BotQueryService botQueryService;
    private final BotCommandService botCommandService;

    private final ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<>();

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: {}", update.getMessage());

        if (update.hasMessage()) {
            Message message = update.getMessage();
            String url = message.getText();

            if (url.equals("/test")) {
                url = "https://www.youtube.com/watch?v=_zJrhqUBF5o";
            }

            Map<String, Object> info = botQueryService.getVideoInfo(url);
            List<Map<String, Object>> formats = ((List<Map<String, Object>>) info.get("formats"))
                    .stream()
                    .sorted(Comparator.comparing(m -> {
                        String formatNote = (String) m.get("format_note");
                        if (formatNote.matches("\\d+p")) {
                            return Integer.parseInt(formatNote.replaceAll("p", ""));
                        }
                        return 0;
                    }))
                    .collect(Collectors.toList());

            if (threadLocal.get() == null) {
                threadLocal.set(info);
            }

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("<->");

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            for (int i = 0; i < formats.size(); i++) {
                if (i % 2 == 0) {
                    rowsInline.add(rowInline);
                    rowInline = new ArrayList<>();
                }
                Map<String, Object> format = formats.get(i);
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

                String filesizeInString;
                DataSize filesize = DataSize.ofBytes(Optional.ofNullable(((Integer) format.getOrDefault("filesize", 0))).orElse(0));
                if (filesize.toMegabytes() > 0) {
                    filesizeInString = filesize.toMegabytes() + " Mb";
                } else {
                    filesizeInString = filesize.toKilobytes() + " Kb";
                }
                inlineKeyboardButton.setText(String.format("%s [%s / %s]", format.get("format_note"), format.get("ext"), filesizeInString));
                inlineKeyboardButton.setCallbackData(((Integer) format.get("format_id")).toString());
                rowInline.add(inlineKeyboardButton);
            }
            markupInline.setKeyboard(rowsInline);
            sendMessage.setReplyMarkup(markupInline);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            System.out.println("Callback query: " + update.getCallbackQuery().toString());
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Integer formatId = Integer.parseInt(callbackQuery.getData());
            Map<String, Object> info = threadLocal.get();
            Map<String, Object> format = ((List<Map<String, Object>>) info.get("formats")).stream()
                    .filter(f -> f.get("format_id").equals(formatId))
                    .findFirst()
                    .orElse(Collections.emptyMap());
            String url = (String) format.get("url");
            String filename = (String) info.get("title");
            Long filesize = ((Integer) format.get("filesize")).longValue();
            botCommandService.download(url, filename, filesize);
        }

//        YoutubePageParser youtubePageParser = context.getBean(YoutubePageParser.class, url);
//        List<Map<String, Object>> formats = youtubePageParser.parse();
//
//        Map<String, Object> format = formats.stream()
//                .filter(m -> m.get("ext").equals("m4a") && m.get("format_note").equals("tiny"))
//                .findFirst().orElseThrow(RuntimeException::new);
//
//        sendNotification(message.getChatId(), "Идет скачивание аудио: " + format.get("title"));
//        sendNotification(message.getChatId(), getFilesize(format));
//        StreamDownloader downloader = context.getBean(StreamDownloader.class);
//        File downloadFile;
//        try {
//            downloadFile = downloader.realDownload(3,
//                    ((String) format.get("url")),
//                    ((String) format.get("title")),
//                    ((Integer) format.get("filesize")).longValue(),
//                    ((DefaultHttpHeaders) format.get("http_headers"))
//            );
//        } catch (Exception e) {
//            sendNotification(message.getChatId(), "Возникла непредвиденная ошибка: " + e.getMessage());
//            return;
//        }
//
//        SendAudio SendAudioRequest = new SendAudio();
//        SendAudioRequest.setChatId(String.valueOf(message.getChatId()));
//        SendAudioRequest.setAudio(new InputFile(downloadFile));
//        SendAudioRequest.setCaption(downloadFile.getName());
//        try {
//            execute(SendAudioRequest);
//        } catch (TelegramApiException e) {
//            log.error("Возникла непредвиденная ошибка", e);
//            sendNotification(message.getChatId(), "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
//        }
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
