package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ai_topics")
public class AiTopic {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String category;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String scenario;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String prompt;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String config;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String desc;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String detail;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String topo;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String images;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String videos;

    @Column(name = "created_at")
    private String createdAt;
}