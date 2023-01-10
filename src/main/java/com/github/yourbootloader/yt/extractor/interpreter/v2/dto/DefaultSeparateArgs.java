package com.github.yourbootloader.yt.extractor.interpreter.v2.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class DefaultSeparateArgs {

    String delimiter = ",";

    Integer maxSplit = null;

    List<String> skipDelimiters = null;

    public DefaultSeparateArgs() {
    }

    public DefaultSeparateArgs(String delimiter, Integer maxSplit, List<String> skipDelimiters) {
        this.delimiter = delimiter;
        this.maxSplit = maxSplit;
        this.skipDelimiters = skipDelimiters;
    }
}
