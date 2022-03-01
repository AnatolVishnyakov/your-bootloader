package com.github.yourbootloader.scheduler;

import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadFolderCleaner {

    private final YDProperties ydProperties;

    void clean() {
        String downloadPath = ydProperties.getDownloadPath();
        File downloadFolder = new File(downloadPath);
        if (downloadFolder.isDirectory()) {
            File[] files = downloadFolder.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    file.deleteOnExit();
                    log.info("File {} was deleted.", file.getName());
                }
            }
        }
    }
}
