package com.github.yourbootloader.yt.download.v2;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.asynchttpclient.handler.TransferCompletionHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Slf4j
public class YtDownloadAsyncHandler extends TransferCompletionHandler {

    private OutputStream outputStream;

    public void setFile(File outputFile) throws IOException {
        this.outputStream = Files.newOutputStream(outputFile.toPath(), StandardOpenOption.APPEND);
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
        outputStream.write(content.getBodyPartBytes());
        return super.onBodyPartReceived(content);
    }

    @Override
    public Response onCompleted(Response response) throws Exception {
        outputStream.close();
        return super.onCompleted(response);
    }
}