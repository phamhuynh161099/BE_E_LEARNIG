package com.sakata.boilerplate.service;

import com.sakata.boilerplate.dto.FFmpegProgress;
import com.sakata.boilerplate.models.TrackingVideo;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FFmpegProgressService {

    private final ConcurrentHashMap<String, TrackingVideo> progressMap = new ConcurrentHashMap<>();

    // Patterns để parse output FFmpeg
    private static final Pattern FRAME_PATTERN   = Pattern.compile("frame=\\s*(\\d+)");
    private static final Pattern FPS_PATTERN     = Pattern.compile("fps=\\s*([\\d.]+)");
    private static final Pattern TIME_PATTERN    = Pattern.compile("time=(\\d+):(\\d+):([\\d.]+)");
    private static final Pattern SPEED_PATTERN   = Pattern.compile("speed=\\s*([\\d.]+)x");

    public TrackingVideo initJob(String jobId, String resolution, long totalDuration, String videoId) {
        TrackingVideo p = new TrackingVideo();
        p.setJobId(jobId);
        p.setVideoId(videoId);
        p.setStatus("QUEUED");
        p.setPercentage(0);
        p.setResolution(resolution);
        p.setTotalDuration(totalDuration);
        progressMap.put(jobId, p);
        return p;
    }

    /**
     * Gọi mỗi khi có 1 dòng output từ FFmpeg stderr
     */
    public void parseLine(String jobId, String line) {
        TrackingVideo p = progressMap.get(jobId);
        if (p == null) return;

        p.setStatus("PROCESSING");

        // Parse time (quan trọng nhất)
        Matcher timeMatcher = TIME_PATTERN.matcher(line);
        if (timeMatcher.find()) {
            long hours   = Long.parseLong(timeMatcher.group(1));
            long minutes = Long.parseLong(timeMatcher.group(2));
            double secs  = Double.parseDouble(timeMatcher.group(3));
            long encoded = hours * 3600 + minutes * 60 + (long) secs;

            p.setCurrentTime(String.format("%02d:%02d:%05.2f", hours, minutes, secs));
            p.setTimeSeconds(encoded);

            if (p.getTotalDuration() > 0) {
                int pct = (int) (encoded * 100 / p.getTotalDuration());
                p.setPercentage(Math.min(pct, 99));
            }
        }

        // Parse fps
        Matcher fpsMatcher = FPS_PATTERN.matcher(line);
        if (fpsMatcher.find()) {
            p.setFps(Double.parseDouble(fpsMatcher.group(1)));
        }

        // Parse speed & ước tính thời gian còn lại
        Matcher speedMatcher = SPEED_PATTERN.matcher(line);
        if (speedMatcher.find()) {
            double speed = Double.parseDouble(speedMatcher.group(1));
            p.setSpeed(speed);

            // Ước tính: còn (totalDuration - encoded) giây video
            // Với tốc độ `speed`x, thời gian thực cần = remaining / speed
            if (speed > 0 && p.getTotalDuration() > 0) {
                long remaining = p.getTotalDuration() - p.getTimeSeconds();
                p.setEstimatedSecondsLeft((long) (remaining / speed));
            }
        }
    }

    public TrackingVideo markCompleted(String jobId, String outputPath) {
        TrackingVideo p = progressMap.get(jobId);
        if (p != null) {
            p.setStatus("COMPLETED");
            p.setPercentage(100);
            p.setEstimatedSecondsLeft(0);
            p.setOutputPath(outputPath);
        }

        return p;
    }

    public TrackingVideo markFailed(String jobId, String errorMessage) {
        TrackingVideo p = progressMap.get(jobId);
        if (p != null) {
            p.setStatus("FAILED");
            p.setErrorMessage(errorMessage);
        }

        return p;
    }

    public TrackingVideo getProgress(String jobId) {
        return progressMap.get(jobId);
    }

    public Collection<TrackingVideo> getAllProgress() {
        return progressMap.values();
    }
}