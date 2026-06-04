package com.netconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiTopicDTO {
    private String id;
    @NotBlank @Size(max = 200)
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
    private List<Map<String, Object>> files;
    private String createdAt;
}