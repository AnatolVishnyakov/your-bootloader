package com.github.yourbootloader.yt.extractor.interpreter;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class LocalNameSpace {

    private final Map<String, Object> variables;

    public LocalNameSpace() {
        variables = new HashMap<>();
    }

    public <T> T get(String key) {
        Object value = variables.get(key);
        return ((Value) value).getValue();
    }

    public <T> void set(String key, T value) {
        variables.computeIfPresent(key, (s, o) -> new Value(value));
        variables.putIfAbsent(key, new Value(value));
    }

    private static class Value {

        private final Class clazz;

        private final Object value;

        public Value(Object value) {
            this.clazz = value.getClass();
            this.value = value;
        }

        public <T> T getValue() {
            if (clazz == String.class) {
                return (T) value;
            }
            return null;
        }
    }
}
