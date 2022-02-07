package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotCommandService;
import com.github.yourbootloader.bot.BotQueryService;
import com.github.yourbootloader.bot.command.cache.CommandCache;
import com.github.yourbootloader.bot.dto.VideoInfoDto;
import com.github.yourbootloader.domain.users.UsersCommandService;
import com.github.yourbootloader.yt.extractor.YoutubeIE;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YtParseUrlCommand implements Command {

    private final BotQueryService botQueryService;
    private final BotCommandService botCommandService;
    private final UsersCommandService usersCommandService;
    private final CommandCache commandCache;

    @Override
    public boolean canHandle(Update message) {
        return message.hasMessage() &&
                Pattern.compile(YoutubeIE._VALID_URL)
                        .matcher(message.getMessage().getText())
                        .matches();
    }

    @Override
    public void handle(Bot bot, Update update) {
        log.info("Youtube download command");

        Long chatId = update.getMessage().getChatId();
        Message message = update.getMessage();
        String url = message.getText();
        log.info("Youtube url: {}", url);

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
//                jsonObject.put("id", UUID.randomUUID());
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

        commandCache.save(chatId, videosInfo);
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unexpected error", e);
            sendNotification(bot, update.getMessage().getChatId(), "Повторите отправку url!");
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
    private void sendNotification(Bot bot, Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }
}
