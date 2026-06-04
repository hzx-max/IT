package com.netconfig.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class OfficeDTO {
    private String id;
    @NotBlank @Size(max = 200)
    private String title;
    @NotBlank @Size(max = 50)
    private String vendor;
    @NotBlank @Size(max = 50)
    private String cat;
    private List<Map<String, Object>> topo;
    private String desc;
    private String detail;
    private String config;
    private Map<String, String> configs;
    private Map<String, String> comments;
    private Map<String, String> docs;
    private List<String> images;
    private List<String> videos;
    private List<Map<String, Object>> files;
    private String createdAt;
}
