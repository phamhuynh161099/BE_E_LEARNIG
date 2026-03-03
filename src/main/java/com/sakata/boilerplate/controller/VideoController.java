package com.sakata.boilerplate.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sakata.boilerplate.audit.AuditService;
import com.sakata.boilerplate.models.Video;
import com.sakata.boilerplate.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/videos")
@Slf4j
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

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


            // Bắt đầu xử lý async
            System.out.println(">>>VIDEO"+video);

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

}