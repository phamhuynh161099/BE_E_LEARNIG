package com.sakata.boilerplate.models.vo;

import java.time.LocalDateTime;

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
public class TrackingVideoVO {
    private Long id;    
    private String originalFileName;
    private String uniqueFileName;
    private String originalPath;
    private String encoded720Path;
    private String encoded1080Path;
    private String status; // UPLOADING, PROCESSING, COMPLETED, FAILED
    private Long fileSize;
    private String duration;
    private LocalDateTime createdAt;


    private String jobId;
    private String videoId;
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
