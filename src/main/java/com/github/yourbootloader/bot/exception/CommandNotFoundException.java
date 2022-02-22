package com.github.yourbootloader.bot.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String command) {
        super("Command " + command + " not found!");
    }
}
