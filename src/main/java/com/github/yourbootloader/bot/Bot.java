package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.dto.VideoInfoDto;
import com.github.yourbootloader.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final BotQueryService botQueryService;
    private final BotCommandService botCommandService;

    private final ThreadLocal<Map<Long, List<VideoInfoDto>>> threadLocal = new ThreadLocal<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived: {}", update.getMessage());

        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            Message message = update.getMessage();
            String url = message.getText();
            log.info("Youtube url: {}", url);

            if (url.equals("/test")) {
//                url = "https://www.youtube.com/watch?v=_zJrhqUBF5o";
                url = "https://youtu.be/IPUUbVhvqrE";
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

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("<->");

            List<VideoInfoDto> videosInfo = new ArrayList<>();
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
                DataSize filesize = DataSize.ofBytes(Optional.ofNullable(((Long) format.getOrDefault("filesize", 0))).orElse(0L));
                if (filesize.toMegabytes() > 0) {
                    filesizeInString = filesize.toMegabytes() + " Mb";
                } else {
                    filesizeInString = filesize.toKilobytes() + " Kb";
                }
                inlineKeyboardButton.setText(String.format("%s [%s / %s]", format.get("format_note"), format.get("ext"), filesizeInString));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("format_id", format.get("format_id"));
                jsonObject.put("filesize", format.get("filesize"));
                log.debug("Json: {}", jsonObject);
                videosInfo.add(
                        new VideoInfoDto(
                                ((Integer) format.get("format_id")),
                                (String) info.get("title"),
                                ((Long) format.get("filesize")),
                                ((String) format.get("url"))
                        )
                );

                inlineKeyboardButton.setCallbackData(jsonObject.toString());
                rowInline.add(inlineKeyboardButton);
            }

            if (threadLocal.get() == null) {
                threadLocal.set(new HashMap<Long, List<VideoInfoDto>>() {{
                    put(chatId, videosInfo);
                }});
            } else if (!threadLocal.get().containsKey(chatId)) {
                threadLocal.get().put(chatId, videosInfo);
            }

            markupInline.setKeyboard(rowsInline);
            sendMessage.setReplyMarkup(markupInline);

            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Unexpected error", e);
                sendNotification(update.getMessage().getChatId(), "Повторите отправку url!");
            }
        } else if (update.hasCallbackQuery()) {
            log.info("Callback query: {}", update.getCallbackQuery().toString());
            Chat chat = update.getCallbackQuery().getMessage().getChat();
            JSONObject callbackData = new JSONObject(update.getCallbackQuery().getData());
            log.debug("Callback Json: {}", callbackData);
            List<VideoInfoDto> videosInfo = threadLocal.get().get(chat.getId());
            int formatId = callbackData.optInt("format_id");
            long filesize = callbackData.optLong("filesize");
            List<VideoInfoDto> collect = videosInfo.stream()
                    .filter(v -> v.getFormatId().equals(formatId))
                    .collect(Collectors.toList());
            VideoInfoDto videoInfoDto = collect.get(0);
            String url = videoInfoDto.getUrl();
            String title = videoInfoDto.getTitle();
            if (collect.size() > 1) {
                log.warn("Formats greater than 1 {} / {}", title, formatId);
            }
            if (url.isEmpty()) {
                sendNotification(chat.getId(), "Повторите отправку url!");
                return;
            }
            executorService.execute(() -> {
                botCommandService.download(chat, url, title, filesize);
                threadLocal.get().remove(chat.getId());
            });
        }
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
