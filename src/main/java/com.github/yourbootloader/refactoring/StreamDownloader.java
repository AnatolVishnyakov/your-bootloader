package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
public class StreamDownloader {
    private String url;
    private String fileName;

    public StreamDownloader(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }

    private void establishConnection(DownloadContext context) {
    }

    @SneakyThrows
    private void download() {
        String filePath = "D:\\IdeaProjects\\your-bootloader\\src\\main\\resources\\archive\\" + UUID.randomUUID() + ".mp3";
        FileOutputStream stream = new FileOutputStream(filePath);

        AsyncHttpClient client = Dsl.asyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setRequestTimeout(600_000).setReadTimeout(600_000).setConnectTimeout(600_000));
        client.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {
            @Override
            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                printContentWritten();
                stream.getChannel().write(bodyPart.getBodyByteBuffer());
                return State.CONTINUE;
            }

            @Override
            public FileOutputStream onCompleted(Response response) {
                System.out.println("Completed download!");
                return stream;
            }

            @SneakyThrows
            private void printContentWritten() {
                long fileSize = Files.size(new File(filePath).toPath());
                DataSize dataSize = DataSize.ofBytes(fileSize);
                if (dataSize.toMegabytes() == 0) {
                    log.info(dataSize.toKilobytes() + " Kb");
                } else {
                    log.info(dataSize.toMegabytes() + " Mb (" + dataSize.toKilobytes() + " Kb)");
                }
            }
        }).get();
    }

    public void realDownload(int retries) {
        this.url = url;
        this.fileName = fileName;

        DownloadContext context = DownloadContext.builder()
                .dataLen(0L)
                .resumeLen(0L) // TODO надо вычислять
                .blockSize(1024L) // TODO надо вычислять
                .chunkSize(1024L) // TODO надо вычислять
                .build();

        int count = 0;

        while (count <= retries) {
            try {
                establishConnection(context);
                download();
            } catch (Exception exc) {
                count++;
                if (count <= retries) {
                    log.error("Exit downloading by error. Attempt {} of {}.", count, retries);
                }
                log.error("[download] Got server HTTP error: {}. Retrying (attempt {} of {})...", exc.getMessage(), count, retries);
            }
        }
    }
}
