package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import org.springframework.util.unit.DataSize;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

public class StreamDownloader {
    private final String url;

    public StreamDownloader(String url) {
        this.url = url;
    }

    private void tryConnect() {
        int chunkSize = 0;


    }

    @SneakyThrows
    public void download() {
        ReadableByteChannel readableByteChannel = Channels.newChannel(new URI(url).toURL().openStream());
        FileOutputStream fileOutputStream = new FileOutputStream("D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\" + UUID.randomUUID() + ".mp3");
        FileChannel fileChannel = fileOutputStream.getChannel();

        int pos = 0;
        while (true) {
            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, pos, 4 * 1024);
            if (pos == -1) {
                break;
            }
            pos += 4 * 1024;

            System.out.println(DataSize.ofBytes(pos).toKilobytes());
        }
    }
}
