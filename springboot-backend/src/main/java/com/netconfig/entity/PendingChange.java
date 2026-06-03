package com.netconfig.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_changes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String module; // 模块: cmd, fault, desktop, linux, office, ai

    @Column(nullable = false)
    private String operation; // 操作: CREATE, UPDATE, DELETE

    @Column(name = "entity_id")
    private Long entityId; // 实体ID (UPDATE/DELETE时需要)

    @Column(columnDefinition = "TEXT")
    private String payload; // 请求体JSON

    @Column(name = "submitter_id", nullable = false)
    private Long submitterId;

    @Column(name = "submitter_name", nullable = false)
    private String submitterName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeStatus status = ChangeStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    public enum ChangeStatus {
        PENDING, APPROVED, REJECTED
    }
}