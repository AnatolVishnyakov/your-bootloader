package com.github.yourbootloader.dbqueue;

import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.EnqueueResult;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;

import javax.annotation.Nonnull;

public class YtProducer implements QueueProducer<String> {
    @Override
    public EnqueueResult enqueue(@Nonnull EnqueueParams<String> enqueueParams) {
        return null;
    }

    @Nonnull
    @Override
    public TaskPayloadTransformer<String> getPayloadTransformer() {
        return null;
    }
}
