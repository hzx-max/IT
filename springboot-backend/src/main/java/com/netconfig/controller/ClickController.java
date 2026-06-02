package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.ClickRecord;
import com.netconfig.repository.ClickRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clicks")
@RequiredArgsConstructor
public class ClickController {

    private final ClickRecordRepository clickRecordRepository;

    @Transactional
    @PostMapping("/record")
    public ApiResponse<ClickRecord> record(@RequestBody Map<String, String> body) {
        String module = body.get("module");
        String itemId = body.get("itemId");
        String itemTitle = body.getOrDefault("itemTitle", "");

        if (module == null || itemId == null) {
            return ApiResponse.error("module and itemId are required");
        }

        ClickRecord record = clickRecordRepository.findByModuleAndItemId(module, itemId)
                .orElseGet(() -> {
                    ClickRecord r = new ClickRecord();
                    r.setModule(module);
                    r.setItemId(itemId);
                    r.setItemTitle(itemTitle);
                    r.setCount(0L);
                    return r;
                });
        record.setCount(record.getCount() + 1);
        if (itemTitle != null && !itemTitle.isEmpty()) {
            record.setItemTitle(itemTitle);
        }
        ClickRecord saved = clickRecordRepository.save(record);
        return ApiResponse.success(saved);
    }

    @GetMapping("/stats")
    public ApiResponse<List<Map<String, Object>>> stats() {
        List<Object[]> rows = clickRecordRepository.sumByModule();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("module", row[0]);
            m.put("count", row[1]);
            result.add(m);
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/top10")
    public ApiResponse<List<ClickRecord>> top10() {
        List<ClickRecord> list = clickRecordRepository.findTop10ByOrderByCountDesc();
        return ApiResponse.success(list);
    }

    @GetMapping("/top10/{module}")
    public ApiResponse<List<ClickRecord>> top10ByModule(@PathVariable String module) {
        List<ClickRecord> list = clickRecordRepository.findTop10ByModuleOrderByCountDesc(module);
        return ApiResponse.success(list);
    }
}