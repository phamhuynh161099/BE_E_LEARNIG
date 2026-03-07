package com.sakata.boilerplate.controller;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.sakata.boilerplate.audit.AuditService;
import com.sakata.boilerplate.dto.FFmpegProgress;
import com.sakata.boilerplate.models.TrackingVideo;
import com.sakata.boilerplate.models.Video;
import com.sakata.boilerplate.service.FFmpegProgressService;
import com.sakata.boilerplate.service.VideoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;

@RestController
@RequestMapping("/api/videos")
@Slf4j
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final FFmpegProgressService progressService;
    /**
     * Upload video
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return ResponseEntity.badRequest().body("File must be a video");
            }

            // Lưu video
            Video video = videoService.saveOriginalVideo(file);
            videoService.processVideo(video.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Video uploaded successfully",
                    "videoId", video.getId(),
                    "status", video.getStatus()));

        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading video: " + e.getMessage());
        }
    }

    @GetMapping("/stream/{id}/{quality}")
    public ResponseEntity<String> streamM3u8(
            @PathVariable Long id,
            @PathVariable String quality,
            HttpServletRequest request) {

        try {
            Video video = videoService.getVideo(id);
            if (video == null)
                return ResponseEntity.notFound().build();

            String m3u8Path = quality.equals("720")
                    ? video.getEncoded720Path()
                    : video.getEncoded1080Path();

            if (m3u8Path == null)
                return ResponseEntity.status(HttpStatus.PROCESSING).build();

            File m3u8File = new File(m3u8Path);
            if (!m3u8File.exists())
                return ResponseEntity.notFound().build();

            // Đọc nội dung file gốc
            String content = new String(Files.readAllBytes(m3u8File.toPath()));

            // Thêm quality/ vào trước tên file .ts
            // "1772585593129_demo_input0.ts" → "720/1772585593129_demo_input0.ts"
            content = content.replaceAll(
                    "(?m)^(?!#)(.+\\.ts)$",
                    quality + "/$1");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    // .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(content);

        } catch (Exception e) {
            log.error("Error serving m3u8: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Endpoint 2: Trả file .ts segment
    @GetMapping("/stream/{id}/{quality}/{filename}")
    public ResponseEntity<Resource> streamSegment(
            @PathVariable Long id,
            @PathVariable String quality,
            @PathVariable String filename) {

        try {
            Video video = videoService.getVideo(id);
            if (video == null)
                return ResponseEntity.notFound().build();

            String m3u8Path = quality.equals("720")
                    ? video.getEncoded720Path()
                    : video.getEncoded1080Path();

            if (m3u8Path == null)
                return ResponseEntity.status(HttpStatus.PROCESSING).build();

            // Lấy thư mục chứa .m3u8 → tìm .ts cùng thư mục
            File segmentFile = new File(
                    new File(m3u8Path).getParent(), filename);

            if (!segmentFile.exists()) {
                log.error("Segment not found: {}", segmentFile.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp2t"))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                    .body(new FileSystemResource(segmentFile));

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error serving segment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping
    // @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<?> getAllVideos() {
        try {
            var data = videoService.getAllVideos();

            Map<String, Object> response = new HashMap<>();
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

     // -- Poll progress (REST) -------------------------------------------

    @GetMapping("/progress/{jobId}")
    public ResponseEntity<TrackingVideo> getProgress(@PathVariable String jobId) {
        System.out.println(":::>>progress"+jobId);
        TrackingVideo p = progressService.getProgress(jobId);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // -- Stream progress (SSE) - real-time, không cần frontend polling ---

    @GetMapping(value = "/progress/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable String jobId) {
        SseEmitter emitter = new SseEmitter(300_000L); // timeout 5 phút

        System.out.println(">>>streamProgress-start"+jobId);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    TrackingVideo p = progressService.getProgress(jobId);
                    if (p == null) { Thread.sleep(500); continue; }

                     System.out.println(">>>streamProgress-data"+p);
                    emitter.send(p); // gửi object JSON tới client

                    if ("COMPLETED".equals(p.getStatus()) || "FAILED".equals(p.getStatus())) {
                        emitter.complete();
                        break;
                    }
                    Thread.sleep(1000); // gửi mỗi 1 giây
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}