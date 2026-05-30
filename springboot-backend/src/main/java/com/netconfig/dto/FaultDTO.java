package com.netconfig.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FaultDTO {
    private String id;
    private String title;
    private String category;
    private String symptom;
    private String cause;
    private String solution;
    private List<Map<String, Object>> topo;
    private Map<String, String> docs;
    private String createdAt;
}
