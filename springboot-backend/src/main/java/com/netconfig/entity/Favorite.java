package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "module", "item_id"})
})
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 20)
    private String module;

    @Column(name = "item_id", nullable = false, length = 50)
    private String itemId;

    @Column(name = "item_title", length = 200)
    private String itemTitle;

    @Column(name = "module_label", length = 50)
    private String moduleLabel;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "item_path", length = 200)
    private String itemPath;

    @Column(name = "created_at")
    private String createdAt;
}
