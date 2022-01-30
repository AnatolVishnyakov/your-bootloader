package com.github.yourbootloader.dbqueue;

import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.api.Task;
import ru.yoomoney.tech.dbqueue.api.TaskExecutionResult;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;

import javax.annotation.Nonnull;

public class YtConsumer implements QueueConsumer<String> {
    @Nonnull
    @Override
    public TaskExecutionResult execute(@Nonnull Task<String> task) {
        return null;
    }

    @Nonnull
    @Override
    public QueueConfig getQueueConfig() {
        return null;
    }

    @Nonnull
    @Override
    public TaskPayloadTransformer<String> getPayloadTransformer() {
        return null;
    }
}
