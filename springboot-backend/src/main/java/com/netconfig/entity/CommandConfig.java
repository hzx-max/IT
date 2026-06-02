package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "command_configs")
public class CommandConfig {
    @Id
    private String id;

    @Column(name = "topic_id", nullable = false)
    private String topicId;

    @Column(nullable = false)
    private String vendor;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String config;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String comment;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String doc;

    @Column(name = "verification_cmd", columnDefinition = "TEXT DEFAULT ''")
    private String verificationCmd;

    @Column(name = "verification_images", columnDefinition = "TEXT DEFAULT '[]'")
    private String verificationImages;
}
