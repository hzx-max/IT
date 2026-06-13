package com.netconfig.repository;

import com.netconfig.entity.NoteReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteReactionRepository extends JpaRepository<NoteReaction, Long> {

    Optional<NoteReaction> findByNoteIdAndUserId(Long noteId, String userId);

    List<NoteReaction> findByNoteIdInAndUserId(List<Long> noteIds, String userId);

    List<NoteReaction> findByNoteIdAndReactionType(Long noteId, String reactionType);

    long countByNoteIdAndReactionType(Long noteId, String reactionType);

    void deleteByNoteId(Long noteId);

    void deleteByNoteIdIn(List<Long> noteIds);
}
