package com.github.yourbootloader.scheduler;

import com.github.yourbootloader.yt.download.YtDownloadClient;
import com.github.yourbootloader.yt.extractor.YoutubeIE;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TasksConfiguration {

    private final YoutubeIE youtubeIE;
    private final YtDownloadClient ytDownloadClient;
    private final DownloadFolderCleaner downloadFolderCleaner;

    @SneakyThrows
//    @Scheduled(fixedDelay = 1_000)
    public void downloadTask() {
        String url = "https://www.youtube.com/watch?v=OnQhVL1o2CU";

//        Map<String, Object> info = youtubeIE.realExtract(url);
//        List<Map<String, Object>> formats = ((List<Map<String, Object>>) info.get("formats"))
//                .stream()
//                .sorted(Comparator.comparing(m -> {
//                    String formatNote = (String) m.get("format_note");
//                    if (formatNote.matches("\\d+p")) {
//                        return Integer.parseInt(formatNote.replaceAll("p", ""));
//                    }
//                    return 0;
//                }))
//                .collect(Collectors.toList());
//
//        Map<String, Object> map = formats.get(0);
//        ytDownloadClient.realDownload(0, ((String) map.get("url")), UUID.randomUUID().toString(), ((Long) map.get("filesize")));
    }

    @SneakyThrows
    @Scheduled(fixedDelay = (60 * 60 * 24 * 1_000) * 3)
    void clearDownloadFolder() {
        log.info("Clear download folder!");
        downloadFolderCleaner.clean();
    }
}
