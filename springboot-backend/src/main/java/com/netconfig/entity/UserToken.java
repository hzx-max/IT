package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_tokens")
public class UserToken {
    @Id
    private String token;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String role;

    @Column(name = "expires_at", nullable = false)
    private String expiresAt;
}