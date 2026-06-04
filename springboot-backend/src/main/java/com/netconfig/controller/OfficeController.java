package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.OfficeDTO;
import com.netconfig.service.OfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/office")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @GetMapping
    public List<OfficeDTO> list() {
        return officeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficeDTO> get(@PathVariable String id) {
        OfficeDTO dto = officeService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OfficeDTO>> create(@Valid @RequestBody OfficeDTO dto) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required fields"));
        }
        OfficeDTO created = officeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OfficeDTO>> update(@PathVariable String id, @RequestBody OfficeDTO dto) {
        OfficeDTO updated = officeService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        officeService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        officeService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}
