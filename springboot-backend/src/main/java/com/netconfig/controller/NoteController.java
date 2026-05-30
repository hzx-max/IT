package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{cmdId}")
    public Map<String, String> get(@PathVariable String cmdId) {
        return Map.of("content", noteService.getContent(cmdId));
    }

    @PutMapping("/{cmdId}")
    public ResponseEntity<ApiResponse<Void>> save(@PathVariable String cmdId, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        noteService.save(cmdId, content);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
