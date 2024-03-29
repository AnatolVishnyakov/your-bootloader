package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotQueryService;
import com.github.yourbootloader.bot.dto.VideoInfoDto;
import com.github.yourbootloader.utils.UserContextHolder;
import com.github.yourbootloader.yt.extractor.YoutubeIE;
import com.github.yourbootloader.yt.extractor.YtVideoInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
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

    @Override
    public boolean canHandle(Update message) {
        return message.hasMessage() &&
                Pattern.compile(YoutubeIE._VALID_URL)
                        .matcher(message.getMessage().getText())
                        .matches();
    }

    @Override
    public void handle(Bot bot, Update update) {
        log.info("Youtube parsing url.");

        Chat chat = update.getMessage().getChat();
        Message message = update.getMessage();
        String url = message.getText();
        log.info("Youtube url: {}", url);
        YtVideoInfo info = botQueryService.getVideoInfo(url);
        List<Map<String, Object>> formats = StreamEx.of(info.getFormats())
                .filter(format -> format.get("ext") != null && format.get("format_note").equals("tiny"))
                .sorted(Comparator.comparing(m -> {
                    String formatNote = (String) m.get("format_note");
                    if (formatNote.matches("\\d+p")) {
                        return Integer.parseInt(formatNote.replaceAll("p", ""));
                    }
                    return 0;
                }))
                .collect(Collectors.toList());

        List<VideoInfoDto> videosInfo = new ArrayList<>();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (int i = 0; i < formats.size(); i++) {
            Map<String, Object> format = formats.get(i);
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();

            String filesizeInString;
            DataSize filesize = DataSize.ofBytes(Optional.ofNullable(((Long) format.getOrDefault("filesize", 0))).orElse(0L));
            if (filesize.toMegabytes() > 0) {
                filesizeInString = filesize.toMegabytes() + " Mb";
            } else {
                filesizeInString = filesize.toKilobytes() + " Kb";
            }
            inlineKeyboardButton.setText(String.format("audio [%s / %s / %s]", format.get("format_note"), format.get("ext"), filesizeInString));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("format_id", format.get("format_id"));
            jsonObject.put("filesize", format.get("filesize"));
            log.debug("Json: {}", jsonObject);
            videosInfo.add(
                    new VideoInfoDto(
                            ((Integer) format.get("format_id")),
                            info.getTitle(),
                            ((Long) format.get("filesize")),
                            ((String) format.get("url"))
                    )
            );
            log.info("format id: {}, video url: {}", format.get("format_id"), format.get("url"));

            inlineKeyboardButton.setCallbackData(String.valueOf(format.get("format_id")));
            rowInline.add(inlineKeyboardButton);
            rowsInline.add(rowInline);
        }

        UserContextHolder.setContext(chat, videosInfo);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat.getId().toString());
        sendMessage.setText("Select format:");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unexpected error", e);
            sendNotification(bot, update.getMessage().getChatId(), "Повторите отправку url!");
        }
    }
}
