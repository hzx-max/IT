package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.CategoryExclusion;
import com.netconfig.entity.CategoryLabel;
import com.netconfig.repository.CategoryExclusionRepository;
import com.netconfig.repository.CategoryLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryLabelRepository labelRepository;
    private final CategoryExclusionRepository exclusionRepository;

    @GetMapping
    public Map<String, String> listLabels() {
        Map<String, String> result = new LinkedHashMap<>();
        labelRepository.findAll().forEach(l -> result.put(l.getCatKey(), l.getCatLabel()));
        return result;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> saveLabel(@RequestBody Map<String, String> body) {
        String catKey = body.get("cat_key");
        String catLabel = body.get("cat_label");
        if (catKey == null || catLabel == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing fields"));
        }
        CategoryLabel label = labelRepository.findById(catKey).orElse(new CategoryLabel());
        label.setCatKey(catKey);
        label.setCatLabel(catLabel);
        labelRepository.save(label);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{catKey}")
    public ApiResponse<Void> deleteLabel(@PathVariable String catKey) {
        labelRepository.deleteById(catKey);
        return ApiResponse.success();
    }

    @GetMapping("/exclusions")
    public List<String> listExclusions() {
        return exclusionRepository.findAll().stream().map(CategoryExclusion::getCatKey).toList();
    }

    @PostMapping("/exclusions")
    public ResponseEntity<ApiResponse<Void>> addExclusion(@RequestBody Map<String, String> body) {
        String catKey = body.get("cat_key");
        if (catKey == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing cat_key"));
        }
        if (!exclusionRepository.existsById(catKey)) {
            CategoryExclusion ex = new CategoryExclusion();
            ex.setCatKey(catKey);
            exclusionRepository.save(ex);
        }
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/exclusions/{catKey}")
    public ApiResponse<Void> removeExclusion(@PathVariable String catKey) {
        exclusionRepository.deleteById(catKey);
        return ApiResponse.success();
    }
}
