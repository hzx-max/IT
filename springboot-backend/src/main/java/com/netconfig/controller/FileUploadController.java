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

    private static final String UPLOAD_DIR = "uploads";

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
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + ext;
                Path filePath = uploadPath.resolve(filename);
                file.transferTo(filePath.toFile());
                Map<String, String> info = new LinkedHashMap<>();
                info.put("filename", filename);
                info.put("originalName", originalName != null ? originalName : filename);
                String contentType = file.getContentType();
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