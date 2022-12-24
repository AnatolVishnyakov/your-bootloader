package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.yt.download.listener.TelegramProgressListener;
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
    public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
        outputStream.write(content.getBodyPartBytes());
        return super.onBodyPartReceived(content);
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        outputStream.close();
        if (listeners instanceof TelegramProgressListener) {
            ((TelegramProgressListener) listeners).onRequestResponseCompleted(outputFile);
        }
        return super.onCompleted(response);
    }
}