package com.netconfig.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "note_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"note_id", "user_id"})
})
public class NoteReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "reaction_type", nullable = false)
    private String reactionType;

    @Column(name = "created_at")
    private String createdAt;
}
