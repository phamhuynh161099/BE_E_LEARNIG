package com.sakata.boilerplate.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sakata.boilerplate.mapper.primary.UserMapper;
import com.sakata.boilerplate.mapper.primary.VideoMapper;
import com.sakata.boilerplate.models.Video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "primaryTransactionManager")
public class VideoService {

    // MySQL (primary)
    private final VideoMapper videoMapper;

    private final VideoEncodingService encodingService;

    @Value("${video.storage.original}")
    private String originalPath;

    /**
     * Lưu video gốc
     * Version 1, fixed folder
     */
    public Object saveOriginalVideoV1(MultipartFile file) throws IOException {
        // Tạo tên file unique
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = originalPath + "/" + fileName;

        // Lưu file
        File dest = new File(filePath);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);

        Video video = Video.builder()
                .originalFileName(file.getOriginalFilename())
                .originalPath(filePath)
                .fileSize(file.getSize())
                .status("UPLOADING")
                .build();

        var affectedRowsSave = videoMapper.saveInitializeVideo(video); // → MySQL
        return affectedRowsSave;
        // return videoRepository.save(video);
    }

    /**
     * Lưu video gốc
     * Version 2, link linh động
     * 🚪https://gemini.google.com/app/4bbde3621e371520?hl=vi
     */
    public Video saveOriginalVideo(MultipartFile file) throws IOException {
        // 1. Xử lý đường dẫn thư mục: Chuyển từ tương đối (./) sang tuyệt đối (D:/...)
        Path uploadPath = Paths.get(originalPath).toAbsolutePath().normalize();

        // 2. Tạo thư mục nếu chưa tồn tại (Thay thế cho mkdirs cũ)
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Tạo tên file unique
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // 4. Tạo đường dẫn file đích (Tuyệt đối) để lưu vào ổ cứng
        Path targetLocation = uploadPath.resolve(fileName);

        // 5. Lưu file vật lý (Dùng toFile() để chuyển Path thành File cho hàm
        // transferTo)
        file.transferTo(targetLocation.toFile());

        // --- Xử lý Database ---

        // Lưu ý: Trong DB nên lưu đường dẫn tương đối hoặc chỉ tên file
        // để sau này deploy lên server khác không bị lỗi đường dẫn cứng.
        // Ở đây tôi lưu "originalPath + / + fileName" như ý định ban đầu của bạn.
        String dbFilePath = originalPath + "/" + fileName;

        Video video = Video.builder()
                .originalFileName(file.getOriginalFilename())
                .originalPath(dbFilePath) // Lưu đường dẫn string vào DB
                .fileSize(file.getSize())
                .status("UPLOADING")
                .build();

        var affectedRowsSave = videoMapper.saveInitializeVideo(video);

        // Trả về ID hoặc Object video để frontend biết video nào vừa được tạo
        // (Thường trả về video.getId() sẽ hữu ích hơn affectedRows)
        return video;
    }

    /**
     * Xử lý encode video (chạy async)
     */
    @Async
    public void processVideo(Long videoId) {
        try {
            Video video = videoMapper.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            video.setStatus("PROCESSING");
            videoMapper.updateVideoDynamic(video);

            String originalPath = video.getOriginalPath();
            // String outputFileName = videoId + ".mp4";
            
            // Lấy duration
            String duration = encodingService.getVideoDuration(originalPath);
            video.setDuration(duration);


            /**
             * Xử lý output name
             */
            String folderId = video.getId().toString();

            String storedPath = video.getOriginalPath();
            String realFileName = Paths.get(storedPath).getFileName().toString();
            String outputFileName = realFileName.replace(".mp4", "") + ".m3u8";

            // Encode 720p
            log.info("Starting 720p encoding for video {}", videoId);
            String path720 = encodingService.encodeTo720p(realFileName, outputFileName,folderId);
            video.setEncoded720Path(path720);
            videoMapper.updateVideoDynamic(video);

            // Encode 1080p
            log.info("Starting 1080p encoding for video {}", videoId);
            String path1080 = encodingService.encodeTo1080p(realFileName,
                    outputFileName,folderId);
            video.setEncoded1080Path(path1080);

            video.setStatus("COMPLETED");
            videoMapper.updateVideoDynamic(video);

            log.info("Video {} encoding completed", videoId);

        } catch (Exception e) {
            log.error("Error processing video {}: {}", videoId, e.getMessage(), e);
            Video video = videoMapper.findById(videoId).orElse(null);
            if (video != null) {
                video.setStatus("FAILED");
                videoMapper.updateVideoDynamic(video);
            }
        }
    }

    /**
     * Lấy thông tin video theo ID
     */
    // public Video getVideo(Long id) {
    // return videoRepository.findById(id).orElse(null);
    // }
}
