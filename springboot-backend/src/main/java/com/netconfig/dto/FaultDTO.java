package com.netconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class FaultDTO {
    private String id;
    @NotBlank @Size(max = 200)
    private String title;
    private String category;
    private String symptom;
    private String cause;
    private String solution;
    private List<Map<String, Object>> topo;
    private Map<String, String> docs;
    private List<String> images;
    private List<String> videos;
    private List<Map<String, Object>> files;
    private String createdAt;
}
