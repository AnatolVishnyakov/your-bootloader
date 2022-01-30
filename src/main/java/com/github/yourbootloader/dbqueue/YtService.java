package com.github.yourbootloader.dbqueue;

import ru.yoomoney.tech.dbqueue.config.QueueService;
import ru.yoomoney.tech.dbqueue.config.QueueShard;
import ru.yoomoney.tech.dbqueue.config.TaskLifecycleListener;
import ru.yoomoney.tech.dbqueue.config.ThreadLifecycleListener;

import javax.annotation.Nonnull;
import java.util.List;

public class YtService extends QueueService {
    public YtService(@Nonnull List<QueueShard<?>> queueShards, @Nonnull ThreadLifecycleListener threadLifecycleListener, @Nonnull TaskLifecycleListener taskLifecycleListener) {
        super(queueShards, threadLifecycleListener, taskLifecycleListener);
    }
}
