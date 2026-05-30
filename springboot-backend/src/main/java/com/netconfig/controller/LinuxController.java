package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.LinuxDTO;
import com.netconfig.service.LinuxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxController {

    private final LinuxService linuxService;

    @GetMapping
    public List<LinuxDTO> list() {
        return linuxService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinuxDTO> get(@PathVariable String id) {
        LinuxDTO dto = linuxService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LinuxDTO>> create(@RequestBody LinuxDTO dto) {
        if (dto.getId() == null || dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required fields"));
        }
        LinuxDTO created = linuxService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LinuxDTO>> update(@PathVariable String id, @RequestBody LinuxDTO dto) {
        LinuxDTO updated = linuxService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        linuxService.delete(id);
        return ApiResponse.success();
    }
}
