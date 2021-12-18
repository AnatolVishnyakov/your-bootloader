package com.github.yourbootloader.bot.event;

import lombok.Value;

@Value
public class ProgressIndicatorEvent {

    long fileSize;

    int blockSize;

}
