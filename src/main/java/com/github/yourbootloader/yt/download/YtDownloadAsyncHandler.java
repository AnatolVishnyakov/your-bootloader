package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.yt.download.listener.TelegramProgressListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.asynchttpclient.handler.TransferListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

@Slf4j
public class YtDownloadAsyncHandler extends TransferCompletionHandler {

    private final File outputFile;
    private final OutputStream outputStream;
    private final List<TransferListener> listeners;

    public YtDownloadAsyncHandler(File file, List<TransferListener> listeners) throws IOException {
        this.outputFile = file;
        this.outputStream = Files.newOutputStream(outputFile.toPath(), StandardOpenOption.APPEND);
        this.listeners = listeners;
        listeners.forEach(this::addTransferListener);
    }

    @Override
    @SneakyThrows
    public State onBodyPartReceived(HttpResponseBodyPart content) {
        if (content.length() != 0) {
            outputStream.write(content.getBodyPartBytes());
        }
        return super.onBodyPartReceived(content);
    }

    @Override
    @SneakyThrows
    public Response onCompleted(Response response) {
        outputStream.close();
        Optional<TransferListener> telegramTransferListener = listeners.stream()
                .filter(l -> l instanceof TelegramProgressListener)
                .findFirst();
        telegramTransferListener.ifPresent(transferListener -> ((TelegramProgressListener) transferListener)
                .onRequestResponseCompleted(outputFile));
        return super.onCompleted(response);
    }

    @Override
    @SneakyThrows
    public void onThrowable(Throwable t) {
        outputStream.close();
        super.onThrowable(t);
    }
}