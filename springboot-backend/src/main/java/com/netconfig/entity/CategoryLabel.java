package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category_labels")
public class CategoryLabel {
    @Id
    @Column(name = "cat_key")
    private String catKey;

    @Column(name = "cat_label", nullable = false)
    private String catLabel;
}
