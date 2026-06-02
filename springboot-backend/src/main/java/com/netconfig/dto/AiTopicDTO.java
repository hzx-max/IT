package com.netconfig.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiTopicDTO {
    private String id;
    private String title;
    private String category;
    private String scenario;
    private String prompt;
    private String config;
    private String desc;
    private String detail;
    private List<Map<String, Object>> topo;
    private List<String> images;
    private List<String> videos;
    private String createdAt;
}