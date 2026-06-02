package com.netconfig.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CommandDTO {
    private String id;
    private String title;
    private String cat;
    private List<Map<String, Object>> topo;
    private String desc;
    private String detail;
    private List<ConfigItem> configs;
    private List<Map<String, Object>> files;
    private String createdAt;

    @Data
    public static class ConfigItem {
        private String id;
        private String vendor;
        private String config;
        private String comment;
        private String doc;
        private String verificationCmd;
        private List<Map<String, Object>> verificationImages;
    }
}
