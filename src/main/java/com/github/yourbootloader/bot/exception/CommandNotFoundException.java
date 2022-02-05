package com.github.yourbootloader.bot.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException() {
        super("Command not found!");
    }
}
