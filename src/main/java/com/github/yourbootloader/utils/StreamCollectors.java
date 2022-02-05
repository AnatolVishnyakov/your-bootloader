package com.github.yourbootloader.utils;

import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StreamCollectors {
    public static <T> Collector<T, ?, T> atMostOneRow(Supplier<RuntimeException> supplier) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.isEmpty()) {
                        throw supplier.get();
                    } else if (list.size() != 1) {
                        throw new IllegalStateException("At most one row!");
                    }
                    return list.get(0);
                }
        );
    }
}
