package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "desktop")
public class Desktop {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String category;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String symptom;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String solution;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String topo;

    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String docs;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String images;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String videos;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String files;

    @Column(name = "created_at")
    private String createdAt;
}
