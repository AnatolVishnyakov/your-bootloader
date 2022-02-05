package com.github.yourbootloader.utils;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StreamCollectors {
    public static <T> Collector<T, ?, T> atMostOneRow() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.isEmpty()) {
                        throw new IllegalStateException("Collection is empty!");
                    } else if (list.size() != 1) {
                        throw new IllegalStateException("At most one row!");
                    }
                    return list.get(0);
                }
        );
    }
}
