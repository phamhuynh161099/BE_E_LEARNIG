// package com.sakata.boilerplate.service;

// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class VideoEncodingService {

//     @Value("${ffmpeg.path}")
//     private String ffmpegPath;

//     @Value("${video.storage.original}")
//     private String originalPath;

//     @Value("${video.storage.encoded}")
//     private String encodedPath;

//     /**
//      * Encode video sang 720p
//      */
//     public String encodeTo720p(String inputPath, String outputFileName) throws IOException, InterruptedException {
//         String outputPath = encodedPath + "/720p/" + outputFileName;

//         // ProcessBuilder processBuilder = new ProcessBuilder(
//         // ffmpegPath,
//         // "-i", inputPath,
//         // "-vf", "scale=-2:720", // Giữ tỷ lệ khung hình
//         // "-c:v", "libx264", // Video codec
//         // "-preset", "medium", // Tốc độ encode
//         // "-crf", "23", // Chất lượng (18-28, thấp hơn = chất lượng cao hơn)
//         // "-c:a", "aac", // Audio codec
//         // "-b:a", "128k", // Audio bitrate
//         // "-movflags", "+faststart", // Cho phép streaming tốt hơn
//         // "-y", // Ghi đè file nếu tồn tại
//         // outputPath);

//         ProcessBuilder processBuilder = new ProcessBuilder(
//                 ffmpegPath,
//                 "-i", inputPath,
//                 "-vf", "scale=-2:1080", // Resize về chiều cao 1080p, giữ tỷ lệ khung hình
//                 "-c:v", "libx264", // Video codec H.264
//                 "-crf", "23", // Chất lượng (Constant Rate Factor)
//                 "-preset", "veryfast", // Tốc độ encode (veryfast tốn ít CPU hơn medium)
//                 "-c:a", "aac", // Audio codec
//                 "-b:a", "128k", // Audio bitrate
//                 "-profile:v", "high", // Profile H.264 High (tương thích thiết bị hiện đại)
//                 "-level", "4.0", // Level encode
//                 "-start_number", "0", // Đánh số segment bắt đầu từ 0
//                 "-hls_time", "10", // Thời lượng mỗi file .ts (segment) là 10 giây
//                 "-hls_list_size", "0", // 0 nghĩa là giữ tất cả các segment trong file m3u8 (VOD)
//                 "-f", "hls", // Định dạng output là HLS
//                 outputPath // Đường dẫn file output (.m3u8)
//         );

//         // Rất quan trọng: Gộp luồng lỗi vào luồng chính để đọc log nếu FFmpeg bị lỗi
//         processBuilder.redirectErrorStream(true);

//         System.out.println("ffmpegPath: " + inputPath);
//         System.out.println("outputPath: " + outputPath);

//         return executeFFmpeg(processBuilder, outputPath);
//     }

//     /**
//      * Encode video sang 1080p
//      */
//     public String encodeTo1080p(String inputPath, String outputFileName) throws IOException, InterruptedException {
//         String outputPath = encodedPath + "/1080p/" + outputFileName;

//         // ProcessBuilder processBuilder = new ProcessBuilder(
//         // "ffmpeg",
//         // "-i", inputPath,
//         // "-vf", "scale=-2:1080",
//         // "-c:v", "libx264",
//         // "-preset", "medium",
//         // "-crf", "23",
//         // "-c:a", "aac",
//         // "-b:a", "192k",
//         // "-movflags", "+faststart",
//         // "-y",
//         // outputPath);

//         ProcessBuilder processBuilder = new ProcessBuilder(
//                 "ffmpeg",
//                 "-i", inputPath, // QUAN TRỌNG: Phải có file đầu vào
//                 "-vf", "scale=-2:720", // Resize về 720p, giữ tỷ lệ khung hình
//                 "-c:v", "libx264", // Video codec
//                 "-crf", "24", // Chất lượng (24 thấp hơn 23 một chút để giảm dung lượng)
//                 "-preset", "veryfast", // Tốc độ encode nhanh
//                 "-c:a", "aac", // Audio codec
//                 "-b:a", "128k", // Audio bitrate
//                 "-profile:v", "main", // Profile Main (nhẹ hơn High, tương thích rộng hơn)
//                 "-level", "3.1", // Level 3.1 (phù hợp cho 720p)
//                 "-start_number", "0", // Bắt đầu segment từ số 0
//                 "-hls_time", "10", // Mỗi segment dài 10 giây
//                 "-hls_list_size", "0", // Giữ lại toàn bộ danh sách segment (VOD)
//                 "-f", "hls", // Định dạng output HLS
//                 outputPath // File đích (.m3u8)
//         );

//         // Đừng quên dòng này để bắt lỗi nếu có
//         processBuilder.redirectErrorStream(true);

//         return executeFFmpeg(processBuilder, outputPath);
//     }

//     /**
//      * Thực thi FFmpeg command
//      */
//     private String executeFFmpeg(ProcessBuilder processBuilder, String outputPath)
//             throws IOException, InterruptedException {

//         processBuilder.redirectErrorStream(true);
//         Process process = processBuilder.start();

//         // Đọc output để debug
//         try (BufferedReader reader = new BufferedReader(
//                 new InputStreamReader(process.getInputStream()))) {
//             String line;
//             while ((line = reader.readLine()) != null) {
//                 log.debug("FFmpeg: {}", line);
//             }
//         }

//         int exitCode = process.waitFor();
//         if (exitCode != 0) {
//             throw new RuntimeException("FFmpeg encoding failed with exit code: " + exitCode);
//         }

//         return outputPath;
//     }

//     /**
//      * Lấy thông tin video (duration, resolution, etc.)
//      */
//     public String getVideoDuration(String videoPath) throws IOException, InterruptedException {
//         ProcessBuilder processBuilder = new ProcessBuilder(
//                 ffmpegPath.replace("ffmpeg", "ffprobe"),
//                 "-v", "error",
//                 "-show_entries", "format=duration",
//                 "-of", "default=noprint_wrappers=1:nokey=1",
//                 videoPath);

//         processBuilder.redirectErrorStream(true);
//         Process process = processBuilder.start();

//         StringBuilder output = new StringBuilder();
//         try (BufferedReader reader = new BufferedReader(
//                 new InputStreamReader(process.getInputStream()))) {
//             String line;
//             while ((line = reader.readLine()) != null) {
//                 output.append(line);
//             }
//         }

//         process.waitFor();
//         return output.toString().trim();
//     }
// }

package com.sakata.boilerplate.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VideoEncodingService {

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${video.storage.original}")
    private String originalPath;

    @Value("${video.storage.encoded}")
    private String encodedPath;

    /**
     * Encode video sang 720p (HLS)
     */
    public String encodeTo720p(String inputFileName, String outputFileName, String folderId)
            throws IOException, InterruptedException {
        // 1. Xác định thư mục chứa file gốc (Input)
        // inputFileName: tên file (ví dụ: 123_video.mp4)
        Path inputDir = Paths.get(originalPath).toAbsolutePath().normalize();
        Path inputFile = inputDir.resolve(inputFileName);

        // 2. Xác định thư mục đích (Output) -> ./uploads/encoded/720p
        Path outputDir = Paths.get(encodedPath, folderId, "720p").toAbsolutePath().normalize();
        Path outputFile = outputDir.resolve(outputFileName); // file .m3u8

        // 3. QUAN TRỌNG: Tạo thư mục đích nếu chưa tồn tại (Tránh lỗi Exit code -2)
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            log.info("Đã tạo thư mục: " + outputDir);
        }

        // 4. Cấu hình lệnh FFmpeg
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i", inputFile.toString(), // Dùng đường dẫn tuyệt đối
                "-vf", "scale=-2:720", // Resize về 720p
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
                outputFile.toString() // Dùng đường dẫn tuyệt đối
        );

        log.info("Bắt đầu encode 720p: " + inputFile.getFileName());
        return executeFFmpeg(processBuilder, outputFile.toString());
    }

    /**
     * Encode video sang 1080p (HLS)
     */
    public String encodeTo1080p(String inputFileName, String outputFileName, String folderId)
            throws IOException, InterruptedException {
        // 1. Xác định Input
        Path inputDir = Paths.get(originalPath).toAbsolutePath().normalize();
        Path inputFile = inputDir.resolve(inputFileName);

        // 2. Xác định Output -> ./uploads/encoded/1080p
        Path outputDir = Paths.get(encodedPath, folderId, "1080p").toAbsolutePath().normalize();
        Path outputFile = outputDir.resolve(outputFileName);

        // 3. Tạo thư mục
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            log.info("Đã tạo thư mục: " + outputDir);
        }

        // 4. Cấu hình lệnh FFmpeg (Sử dụng biến ffmpegPath thay vì hardcode)
        ProcessBuilder processBuilder = new ProcessBuilder(
                ffmpegPath,
                "-i", inputFile.toString(),
                "-vf", "scale=-2:1080", // Resize về 1080p
                "-c:v", "libx264",
                "-crf", "23",
                "-preset", "veryfast",
                "-c:a", "aac",
                "-b:a", "192k", // Bitrate cao hơn cho 1080p
                "-profile:v", "high",
                "-level", "4.1", // Level cao hơn cho 1080p
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                outputFile.toString());

        log.info("Bắt đầu encode 1080p: " + inputFile.getFileName());
        return executeFFmpeg(processBuilder, outputFile.toString());
    }

    /**
     * Thực thi lệnh FFmpeg và đọc log lỗi
     */
    private String executeFFmpeg(ProcessBuilder processBuilder, String outputPath)
            throws IOException, InterruptedException {

        // In ra câu lệnh sắp chạy để kiểm tra đường dẫn
        System.out.println("DEBUG COMMAND: " + String.join(" ", processBuilder.command()));

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // SỬA DÒNG NÀY: In ra System.err để chắc chắn thấy lỗi
                System.err.println("FFMPEG LOG: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg encoding failed with exit code: " + exitCode);
        }

        return outputPath;
    }

    /**
     * Lấy thông tin duration
     */
    public String getVideoDuration(String inputFileName) throws IOException, InterruptedException {
        // Xử lý path ffprobe dựa trên ffmpeg path
        String ffprobeCmd = "ffprobe";
        if (ffmpegPath.contains("/") || ffmpegPath.contains("\\")) {
            // Nếu là đường dẫn file exe cụ thể
            ffprobeCmd = ffmpegPath.replace("ffmpeg", "ffprobe");
        }

        // Xác định file input
        Path inputDir = Paths.get(originalPath).toAbsolutePath().normalize();
        Path inputFile = inputDir.resolve(inputFileName);

        ProcessBuilder processBuilder = new ProcessBuilder(
                ffprobeCmd,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                inputFile.toString());

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        process.waitFor();
        return output.toString().trim();
    }
}