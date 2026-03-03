package com.sakata.boilerplate.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    private Long id;    
    private String originalFileName;
    private String originalPath;
    private String encoded720Path;
    private String encoded1080Path;
    private String status; // UPLOADING, PROCESSING, COMPLETED, FAILED
    private Long fileSize;
    private String duration;
    private LocalDateTime createdAt;
}