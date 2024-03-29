package com.github.yourbootloader.yt.exception;

public class MethodNotImplementedException extends RuntimeException {
    public MethodNotImplementedException() {
        super("Not implemented method!");
    }

    public MethodNotImplementedException(String message) {
        super(message);
    }
}
