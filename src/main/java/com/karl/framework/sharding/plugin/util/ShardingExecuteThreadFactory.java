package com.karl.framework.sharding.plugin.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author karl.zhong
 */
public class ShardingExecuteThreadFactory implements ThreadFactory {
    private AtomicInteger threadCount = new AtomicInteger(0);
    private static final String THREAD_NAME_PREFIX = "Sharding-Execute-Thread-";

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, THREAD_NAME_PREFIX + threadCount.getAndIncrement());
    }
}
