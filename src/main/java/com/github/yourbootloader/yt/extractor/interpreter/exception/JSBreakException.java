package com.github.yourbootloader.yt.extractor.interpreter.exception;

public class JSBreakException extends RuntimeException {
    public JSBreakException() {
        super("Invalid break");
    }
}
