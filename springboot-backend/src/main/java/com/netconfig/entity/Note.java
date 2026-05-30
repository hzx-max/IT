package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notes")
public class Note {
    @Id
    @Column(name = "cmd_id")
    private String cmdId;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String content;
}
