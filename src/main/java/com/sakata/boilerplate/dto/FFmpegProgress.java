package com.sakata.boilerplate.dto;

import lombok.Data;

@Data
public class FFmpegProgress {
    private String videoId;
    private String jobId;
    private String status; // QUEUED, PROCESSING, COMPLETED, FAILED
    private int percentage; // 0–100
    private String currentTime; // "00:01:23.00"
    private long timeSeconds; // giây đã encode
    private long totalDuration; // tổng giây của video
    private double fps;
    private double speed; // 1.5x = nhanh hơn realtime 1.5 lần
    private long estimatedSecondsLeft; // ước tính còn bao nhiêu giây
    private String errorMessage;
    private String resolution; // "720p" hoặc "1080p"
    private String outputPath;
}