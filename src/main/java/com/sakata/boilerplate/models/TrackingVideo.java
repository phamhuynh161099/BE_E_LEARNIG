package com.sakata.boilerplate.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Database table = tracking_video
public class TrackingVideo {
    private String jobId;
    private String videoId;
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
