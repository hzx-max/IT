package com.netconfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netconfig.dto.*;
import com.netconfig.entity.PendingChange;
import com.netconfig.repository.PendingChangeRepository;
import com.netconfig.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class PendingChangeController {

    private final PendingChangeRepository pendingChangeRepository;
    private final CommandService commandService;
    private final FaultService faultService;
    private final DesktopService desktopService;
    private final LinuxService linuxService;
    private final OfficeService officeService;
    private final AiTopicService aiTopicService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 管理员提交待审核的变更
     */
    @PostMapping("/pending-change")
    @Transactional
    public ApiResponse<Map<String, Object>> submitChange(@RequestBody Map<String, Object> body) {
        String module = (String) body.get("module");
        String operation = (String) body.get("operation");

        String entityId = null;
        if (body.get("entityId") != null) {
            entityId = String.valueOf(body.get("entityId"));
        }

        Object rawPayload = body.get("payload");
        String payloadJson = null;
        try {
            payloadJson = rawPayload != null ? objectMapper.writeValueAsString(rawPayload) : null;
        } catch (Exception e) {
            return ApiResponse.error("无效的请求数据");
        }

        if (module == null || operation == null) {
            return ApiResponse.error("module and operation are required");
        }

        String submitterId = (String) body.get("submitterId");
        String submitterName = (String) body.get("submitterName");

        PendingChange change = new PendingChange();
        change.setModule(module);
        change.setOperation(operation);
        change.setEntityId(entityId);
        change.setPayload(payloadJson);
        change.setSubmitterId(submitterId);
        change.setSubmitterName(submitterName);
        change.setStatus(PendingChange.ChangeStatus.PENDING);
        change.setCreatedAt(LocalDateTime.now());

        pendingChangeRepository.save(change);

        Map<String, Object> result = new HashMap<>();
        result.put("id", change.getId());
        result.put("message", "已提交审核，等待超级管理员确认");
        return ApiResponse.success(result);
    }

    /**
     * 获取所有待审核变更
     */
    @GetMapping("/pending-changes")
    public ApiResponse<List<PendingChange>> getPendingChanges() {
        return ApiResponse.success(pendingChangeRepository.findByStatusOrderByCreatedAtDesc(
                PendingChange.ChangeStatus.PENDING));
    }

    /**
     * 批准变更
     */
    @PostMapping("/pending-change/{id}/approve")
    @Transactional
    public ApiResponse<Map<String, Object>> approveChange(@PathVariable Long id,
                                                          @RequestHeader(value = "X-Approver-Name", defaultValue = "super-admin") String approverName) {
        Optional<PendingChange> opt = pendingChangeRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("变更不存在");

        PendingChange change = opt.get();
        if (change.getStatus() != PendingChange.ChangeStatus.PENDING) {
            return ApiResponse.error("该变更已处理");
        }

        try {
            executeChange(change);
        } catch (Exception e) {
            return ApiResponse.error("执行失败: " + e.getMessage());
        }

        change.setStatus(PendingChange.ChangeStatus.APPROVED);
        change.setApprovedAt(LocalDateTime.now());
        change.setApprovedBy(approverName);
        pendingChangeRepository.save(change);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "已批准");
        return ApiResponse.success(result);
    }

    /**
     * 拒绝变更
     */
    @PostMapping("/pending-change/{id}/reject")
    @Transactional
    public ApiResponse<Map<String, Object>> rejectChange(@PathVariable Long id,
                                                         @RequestHeader(value = "X-Approver-Name", defaultValue = "super-admin") String approverName) {
        Optional<PendingChange> opt = pendingChangeRepository.findById(id);
        if (opt.isEmpty()) return ApiResponse.error("变更不存在");

        PendingChange change = opt.get();
        if (change.getStatus() != PendingChange.ChangeStatus.PENDING) {
            return ApiResponse.error("该变更已处理");
        }

        change.setStatus(PendingChange.ChangeStatus.REJECTED);
        change.setApprovedAt(LocalDateTime.now());
        change.setApprovedBy(approverName);
        pendingChangeRepository.save(change);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "已拒绝");
        return ApiResponse.success(result);
    }

    @SuppressWarnings("unchecked")
    private void executeChange(PendingChange change) throws Exception {
        String module = change.getModule();
        String operation = change.getOperation();
        String payloadJson = change.getPayload();

        Map<String, Object> payload = new HashMap<>();
        if (payloadJson != null && !payloadJson.isEmpty()) {
            payload = objectMapper.readValue(payloadJson, Map.class);
        }

        String idStr = change.getEntityId() != null ? change.getEntityId() : (payload != null ? (String) payload.get("id") : null);

        switch (operation) {
            case "CREATE":
                doCreate(module, payload);
                break;
            case "UPDATE":
                if (idStr == null) throw new IllegalArgumentException("缺少entityId");
                doUpdate(module, idStr, payload);
                break;
            case "DELETE":
                if (idStr == null) throw new IllegalArgumentException("缺少entityId");
                doDelete(module, idStr);
                break;
            default:
                throw new IllegalArgumentException("未知操作: " + operation);
        }
    }

    private void doCreate(String module, Map<String, Object> payload) {
        switch (module) {
            case "cmd": commandService.create(objectMapper.convertValue(payload, CommandDTO.class)); break;
            case "fault": faultService.create(objectMapper.convertValue(payload, FaultDTO.class)); break;
            case "desktop": desktopService.create(objectMapper.convertValue(payload, DesktopDTO.class)); break;
            case "linux": linuxService.create(objectMapper.convertValue(payload, LinuxDTO.class)); break;
            case "office": officeService.create(objectMapper.convertValue(payload, OfficeDTO.class)); break;
            case "ai": aiTopicService.create(objectMapper.convertValue(payload, AiTopicDTO.class)); break;
            default: throw new IllegalArgumentException("未知模块: " + module);
        }
    }

    private void doUpdate(String module, String id, Map<String, Object> payload) {
        switch (module) {
            case "cmd": commandService.update(id, objectMapper.convertValue(payload, CommandDTO.class)); break;
            case "fault": faultService.update(id, objectMapper.convertValue(payload, FaultDTO.class)); break;
            case "desktop": desktopService.update(id, objectMapper.convertValue(payload, DesktopDTO.class)); break;
            case "linux": linuxService.update(id, objectMapper.convertValue(payload, LinuxDTO.class)); break;
            case "office": officeService.update(id, objectMapper.convertValue(payload, OfficeDTO.class)); break;
            case "ai": aiTopicService.update(id, objectMapper.convertValue(payload, AiTopicDTO.class)); break;
            default: throw new IllegalArgumentException("未知模块: " + module);
        }
    }

    private void doDelete(String module, String id) {
        switch (module) {
            case "cmd": commandService.delete(id); break;
            case "fault": faultService.delete(id); break;
            case "desktop": desktopService.delete(id); break;
            case "linux": linuxService.delete(id); break;
            case "office": officeService.delete(id); break;
            case "ai": aiTopicService.delete(id); break;
            default: throw new IllegalArgumentException("未知模块: " + module);
        }
    }
}