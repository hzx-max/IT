package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "faults")
public class Fault {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String category;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String symptom;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String cause;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String solution;

    @Column(columnDefinition = "TEXT DEFAULT '[]'")
    private String topo;

    @Column(columnDefinition = "TEXT DEFAULT '{}'")
    private String docs;

    @Column(name = "created_at")
    private String createdAt;
}
