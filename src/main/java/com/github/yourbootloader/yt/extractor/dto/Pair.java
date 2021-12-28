package com.github.yourbootloader.yt.extractor.dto;

import lombok.Value;

@Value
public class Pair<T1, T2> {
    T1 one;

    T2 two;
}
