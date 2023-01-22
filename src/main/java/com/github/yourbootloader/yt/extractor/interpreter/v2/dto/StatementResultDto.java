package com.github.yourbootloader.yt.extractor.interpreter.v2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StatementResultDto {

    private final Object results;

    private final boolean shouldReturn;

}
