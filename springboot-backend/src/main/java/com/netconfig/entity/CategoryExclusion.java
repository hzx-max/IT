package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category_exclusions")
public class CategoryExclusion {
    @Id
    @Column(name = "cat_key")
    private String catKey;
}
