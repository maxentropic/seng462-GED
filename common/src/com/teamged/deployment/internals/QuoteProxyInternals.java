package com.teamged.deployment.internals;

/**
 * Created by DanielF on 2016-03-08.
 */
public class QuoteProxyInternals {

    private Integer smallPool;
    private Integer threadPool;
    private Integer prefetchPort;

    public Integer getSmallPoolSize() {
        return smallPool;
    }

    public Integer getThreadPoolSize() {
        return threadPool;
    }

    public Integer getPrefetchPort() {
        return prefetchPort;
    }
}
