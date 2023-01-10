package com.github.yourbootloader.yt.extractor.interpreter.v2.dto;

import lombok.Value;

import java.util.List;

@Value
public class DefaultSeparateArgs {

    String delim = ",";

    Integer maxSplit = null;

    List<String> skipDelims = null;

}
