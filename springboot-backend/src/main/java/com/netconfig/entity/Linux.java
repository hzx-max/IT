package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "linux")
public class Linux {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private String cat;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String topo;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String desc;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String detail;

    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String configs;

    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String comments;

    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String docs;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String images;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String videos;

    @Column(name = "created_at")
    private String createdAt;
}
