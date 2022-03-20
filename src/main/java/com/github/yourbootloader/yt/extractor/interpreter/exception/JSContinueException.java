package com.github.yourbootloader.yt.extractor.interpreter.exception;

public class JSContinueException extends RuntimeException {
    public JSContinueException() {
        super("Invalid continue");
    }
}
