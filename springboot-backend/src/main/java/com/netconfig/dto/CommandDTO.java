package com.netconfig.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CommandDTO {
    private String id;
    private String title;
    private String vendor;
    private String cat;
    private List<Map<String, Object>> topo;
    private String desc;
    private String detail;
    private Map<String, String> configs;
    private Map<String, String> comments;
    private Map<String, String> docs;
    private Map<String, String> verification;
    private String createdAt;
}
