package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "click_records")
public class ClickRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String module;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "item_title")
    private String itemTitle;

    @Column(nullable = false)
    private Long count = 1L;
}