package com.github.yourbootloader.yt.extractor.interpreter.v2.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class StatementResultDto {

    private final List<?> results;

    private final boolean shouldReturn;

}
