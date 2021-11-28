package com.github.yourbootloader.refactoring;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class DownloadContext {
    private String fileName;
    private String tmpFileName;
    private Object stream;
    private String openMode;
    private long resumeLen;
    private Long dataLen;
    private long blockSize;
    private Instant startTime;
    private long chunkSize;
    private boolean isResume;
    private boolean hasRange;
    private Object data;
}
