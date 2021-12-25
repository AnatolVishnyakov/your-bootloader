package com.github.yourbootloader.bot.event;

import lombok.Value;
import org.springframework.util.unit.DataSize;

@Value
public class ProgressIndicatorEvent {

    DataSize contentSize;

    long fileSize;

    int blockSize;

}
