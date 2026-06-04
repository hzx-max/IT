package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.ClickRecord;
import com.netconfig.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/clicks")
@RequiredArgsConstructor
public class ClickController {

    private final ClickRecordRepository clickRecordRepository;
    private final CommandTopicRepository commandTopicRepository;
    private final FaultRepository faultRepository;
    private final DesktopRepository desktopRepository;
    private final LinuxRepository linuxRepository;
    private final OfficeRepository officeRepository;
    private final AiTopicRepository aiTopicRepository;

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
        list = filterOrphaned(list);
        return ApiResponse.success(list);
    }

    @GetMapping("/top10/{module}")
    public ApiResponse<List<ClickRecord>> top10ByModule(@PathVariable String module) {
        List<ClickRecord> list = clickRecordRepository.findTop10ByModuleOrderByCountDesc(module);
        list = filterOrphaned(list);
        return ApiResponse.success(list);
    }

    /**
     * 过滤掉孤立的点击记录（引用的数据已不存在）
     */
    private List<ClickRecord> filterOrphaned(List<ClickRecord> list) {
        List<ClickRecord> valid = new ArrayList<>();
        for (ClickRecord cr : list) {
            if (itemExists(cr.getModule(), cr.getItemId())) {
                valid.add(cr);
            } else {
                clickRecordRepository.delete(cr);
            }
        }
        return valid;
    }

    private boolean itemExists(String module, String itemId) {
        switch (module) {
            case "cmd": return commandTopicRepository.existsById(itemId);
            case "fault": return faultRepository.existsById(itemId);
            case "desktop": return desktopRepository.existsById(itemId);
            case "linux": return linuxRepository.existsById(itemId);
            case "office": return officeRepository.existsById(itemId);
            case "ai": return aiTopicRepository.existsById(itemId);
            default: return false;
        }
    }
}