package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.FaultDTO;
import com.netconfig.service.FaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faults")
@RequiredArgsConstructor
public class FaultController {

    private final FaultService faultService;

    @GetMapping
    public List<FaultDTO> list() {
        return faultService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaultDTO> get(@PathVariable String id) {
        FaultDTO dto = faultService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FaultDTO>> create(@RequestBody FaultDTO dto) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing field: title"));
        }
        FaultDTO created = faultService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FaultDTO>> update(@PathVariable String id, @RequestBody FaultDTO dto) {
        FaultDTO updated = faultService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        faultService.delete(id);
        return ApiResponse.success();
    }
}
