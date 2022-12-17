package com.github.yourbootloader.yt.download;

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

@Slf4j
public class YtDownloadAsyncHandler extends TransferCompletionHandler {

    private final File outputFile;
    private final OutputStream outputStream;
    private final TransferListener listener;

    public YtDownloadAsyncHandler(File file, TransferListener listener) throws IOException {
        this.outputFile = file;
        this.outputStream = Files.newOutputStream(outputFile.toPath(), StandardOpenOption.APPEND);
        this.listener = listener;
        this.addTransferListener(listener);
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
        outputStream.write(content.getBodyPartBytes());
        return super.onBodyPartReceived(content);
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        outputStream.close();
        if (listener instanceof TelegramProgressListener) {
            ((TelegramProgressListener) listener).onRequestResponseCompleted(outputFile);
        }
        return super.onCompleted(response);
    }
}