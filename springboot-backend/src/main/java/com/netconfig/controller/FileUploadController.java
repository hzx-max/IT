package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = new File("uploads").getAbsolutePath();

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
        "png", "jpg", "jpeg", "gif", "webp", "bmp",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
        "txt", "md", "csv",
        "zip", "rar", "7z", "tar", "gz"
    );

    private static final java.util.Set<String> ALLOWED_MIME_PREFIXES = java.util.Set.of(
        "image/", "application/pdf", "application/vnd.openxmlformats-officedocument",
        "application/vnd.ms-", "application/msword", "application/msexcel",
        "application/zip", "application/x-rar", "application/x-7z",
        "application/gzip", "text/plain", "text/markdown", "text/csv"
    );

    @PostMapping
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> upload(@RequestParam("files") List<MultipartFile> files) {
        List<Map<String, String>> results = new ArrayList<>();
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String originalName = file.getOriginalFilename();

                // 校验文件扩展名
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
                }
                if (!ALLOWED_EXTENSIONS.contains(ext)) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("不支持的文件类型: " + ext));
                }

                // 校验 MIME 类型
                String contentType = file.getContentType();
                if (contentType != null) {
                    boolean mimeOk = false;
                    for (String prefix : ALLOWED_MIME_PREFIXES) {
                        if (contentType.startsWith(prefix)) { mimeOk = true; break; }
                    }
                    if (!mimeOk && !contentType.equals("application/octet-stream")) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("不支持的文件 MIME 类型: " + contentType));
                    }
                }

                String filename = UUID.randomUUID().toString() + "." + ext;
                Path filePath = uploadPath.resolve(filename);
                file.transferTo(filePath.toFile());
                Map<String, String> info = new LinkedHashMap<>();
                info.put("filename", filename);
                info.put("originalName", originalName != null ? originalName : filename);
                info.put("type", contentType != null ? contentType : "application/octet-stream");
                info.put("url", "/uploads/" + filename);
                info.put("size", String.valueOf(file.getSize()));
                results.add(info);
            }
            return ResponseEntity.ok(ApiResponse.success(results));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
}