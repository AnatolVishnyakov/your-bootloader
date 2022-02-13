package com.github.yourbootloader.config;

import org.springframework.lang.Nullable;

public class YtRoutingDataSource //extends AbstractRoutingDataSource
{

    private static final ThreadLocal<Route> ROUTE = new ThreadLocal<Route>();

    public enum Route {
        MASTER, REPLICA
    }

    public static void clearReplicaRoute() {
        ROUTE.remove();
    }

    public static void setReplicaRoute() {
        ROUTE.set(Route.REPLICA);
    }

    public static boolean isReplica() {
        return ROUTE.get() == Route.REPLICA;
    }

    @Nullable
//    @Override
    protected Object determineCurrentLookupKey() {
        return ROUTE.get();
    }
}
