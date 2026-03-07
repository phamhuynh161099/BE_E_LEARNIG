package com.sakata.boilerplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoEncodingService {

    private final FFmpegProgressService progressService;
    private final TrackingVideoService trackingVideoService;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${video.storage.original}")
    private String originalPath;

    @Value("${video.storage.encoded}")
    private String encodedPath;

    // -------------------------------------------------------------------------
    // PUBLIC API
    // -------------------------------------------------------------------------

    public String encodeTo720p(String inputFileName, String outputFileName,
            String videoId, String jobId)
            throws IOException, InterruptedException {

        Path inputFile = resolveInput(inputFileName);
        Path outputFile = resolveOutput(videoId, "720p", outputFileName);

        long duration = getVideoDurationSeconds(inputFileName);
        var initialTrackingVideo = progressService.initJob(jobId, "720p", duration, videoId);
        trackingVideoService.initialTrackingVideo(initialTrackingVideo);

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputFile.toString(),
                "-vf", "scale=-2:720",
                "-c:v", "libx264",
                "-crf", "23",
                "-preset", "veryfast",
                "-c:a", "aac",
                "-b:a", "128k",
                "-profile:v", "main",
                "-level", "3.1",
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                outputFile.toString());

        log.info("Bắt đầu encode 720p [{}]: {}", jobId, inputFile.getFileName());
        return executeFFmpeg(pb, outputFile.toString(), jobId);
    }

    public String encodeTo1080p(String inputFileName, String outputFileName,
            String videoId, String jobId)
            throws IOException, InterruptedException {

        Path inputFile = resolveInput(inputFileName);
        Path outputFile = resolveOutput(videoId, "1080p", outputFileName);

        long duration = getVideoDurationSeconds(inputFileName);

        var initialTrackingVideo = progressService.initJob(jobId, "1080p", duration, videoId);
        trackingVideoService.initialTrackingVideo(initialTrackingVideo);

        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputFile.toString(),
                "-vf", "scale=-2:1080",
                "-c:v", "libx264",
                "-crf", "23",
                "-preset", "veryfast",
                "-c:a", "aac",
                "-b:a", "192k",
                "-profile:v", "high",
                "-level", "4.1",
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                outputFile.toString());

        log.info("Bắt đầu encode 1080p [{}]: {}", jobId, inputFile.getFileName());
        return executeFFmpeg(pb, outputFile.toString(), jobId);
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------------

    private String executeFFmpeg(ProcessBuilder pb, String outputPath, String jobId)
            throws IOException, InterruptedException {

        log.debug("CMD: {}", String.join(" ", pb.command()));

        // KHÔNG dùng redirectErrorStream vì FFmpeg in progress ra stderr
        pb.redirectErrorStream(false);
        Process process = pb.start();

        // Đọc stdout (thường rỗng với FFmpeg)
        Thread stdoutThread = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    log.debug("FFmpeg stdout: {}", line);
                }
            } catch (IOException ignored) {
            }
        });

        // Đọc stderr → parse progress (FFmpeg in tiến độ vào stderr)
        Thread stderrThread = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                    progressService.parseLine(jobId, line); // << cập nhật progress
                }
            } catch (IOException ignored) {
            }
        });

        stdoutThread.start();
        stderrThread.start();

        int exitCode = process.waitFor();
        stdoutThread.join();
        stderrThread.join();

        if (exitCode != 0) {
            String msg = "FFmpeg exit code: " + exitCode;
            var updatedTrackingVideo = progressService.markFailed(jobId, msg);
            trackingVideoService.updateDymamic(updatedTrackingVideo);
            throw new RuntimeException(msg);
        }

        var updatedTrackingVideo = progressService.markCompleted(jobId, outputPath);
        trackingVideoService.updateDymamic(updatedTrackingVideo);
        
        log.info("Encode hoàn tất [{}]: {}", jobId, outputPath);
        return outputPath;
    }

    private Path resolveInput(String inputFileName) {
        return Paths.get(originalPath).toAbsolutePath().normalize().resolve(inputFileName);
    }

    private Path resolveOutput(String folderId, String resolution, String outputFileName)
            throws IOException {
        Path dir = Paths.get(encodedPath, folderId, resolution).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            log.info("Tạo thư mục: {}", dir);
        }
        return dir.resolve(outputFileName);
    }

    public long getVideoDurationSeconds(String inputFileName)
            throws IOException, InterruptedException {
        String ffprobe = ffmpegPath.contains("/") || ffmpegPath.contains("\\")
                ? ffmpegPath.replace("ffmpeg", "ffprobe")
                : "ffprobe";

        Path inputFile = resolveInput(inputFileName);

        ProcessBuilder pb = new ProcessBuilder(
                ffprobe,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                inputFile.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes()).trim();
        p.waitFor();

        try {
            return (long) Double.parseDouble(out);
        } catch (NumberFormatException e) {
            log.warn("Không parse được duration: '{}', dùng 0", out);
            return 0;
        }
    }
}