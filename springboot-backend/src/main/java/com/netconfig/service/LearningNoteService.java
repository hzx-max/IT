package com.netconfig.service;

import com.netconfig.entity.LearningNote;
import com.netconfig.entity.NoteReaction;
import com.netconfig.repository.LearningNoteRepository;
import com.netconfig.repository.NoteReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningNoteService {

    private final LearningNoteRepository learningNoteRepository;
    private final NoteReactionRepository noteReactionRepository;

    public List<LearningNote> getByTargetId(String targetId) {
        return learningNoteRepository.findByTargetIdAndParentIdIsNullOrderByCreatedAtDesc(targetId);
    }

    public List<LearningNote> getByParentId(Long parentId) {
        return learningNoteRepository.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    @Transactional
    public LearningNote create(String targetId, String username, String content) {
        LearningNote note = new LearningNote();
        note.setTargetId(targetId);
        note.setUsername(username);
        note.setContent(content != null ? content : "");
        note.setLikeCount(0);
        note.setDislikeCount(0);
        return learningNoteRepository.save(note);
    }

    @Transactional
    public LearningNote reply(Long parentId, String targetId, String username, String content) {
        LearningNote note = new LearningNote();
        note.setTargetId(targetId);
        note.setUsername(username);
        note.setContent(content != null ? content : "");
        note.setLikeCount(0);
        note.setDislikeCount(0);
        note.setParentId(parentId);
        return learningNoteRepository.save(note);
    }

    @Transactional
    public LearningNote updateById(Long id, String username, String content) {
        LearningNote note = learningNoteRepository.findById(id).orElse(null);
        if (note == null || !note.getUsername().equals(username)) {
            return null;
        }
        note.setContent(content != null ? content : "");
        return learningNoteRepository.save(note);
    }

    @Transactional
    public boolean deleteById(Long id, String username, String role) {
        LearningNote note = learningNoteRepository.findById(id).orElse(null);
        if (note == null) {
            return false;
        }
        boolean isOwner = note.getUsername().equals(username);
        boolean isSuperAdmin = "SUPER_ADMIN".equals(role);
        if (!isOwner && !isSuperAdmin) {
            return false;
        }

        List<Long> idsToDelete = learningNoteRepository.findByParentId(id)
                .stream()
                .map(LearningNote::getId)
                .collect(Collectors.toList());
        idsToDelete.add(id);
        noteReactionRepository.deleteByNoteIdIn(idsToDelete);
        learningNoteRepository.deleteAllById(idsToDelete);
        return true;
    }

    @Transactional
    public Map<String, Object> like(Long id, String userId) {
        LearningNote note = learningNoteRepository.findById(id).orElse(null);
        if (note == null) return Map.of("ok", false, "error", "笔记不存在");

        NoteReaction existing = noteReactionRepository.findByNoteIdAndUserId(id, userId).orElse(null);
        if (existing != null) {
            if ("like".equals(existing.getReactionType())) {
                return Map.of("ok", false, "error", "已点赞");
            }
            if ("dislike".equals(existing.getReactionType())) {
                note.setDislikeCount(Math.max(0, note.getDislikeCount() - 1));
                existing.setReactionType("like");
                noteReactionRepository.save(existing);
            }
        } else {
            NoteReaction reaction = new NoteReaction();
            reaction.setNoteId(id);
            reaction.setUserId(userId);
            reaction.setReactionType("like");
            reaction.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            noteReactionRepository.save(reaction);
        }

        note.setLikeCount(note.getLikeCount() + 1);
        learningNoteRepository.save(note);
        return Map.of("ok", true, "likeCount", note.getLikeCount(), "dislikeCount", note.getDislikeCount());
    }

    @Transactional
    public Map<String, Object> dislike(Long id, String userId) {
        LearningNote note = learningNoteRepository.findById(id).orElse(null);
        if (note == null) return Map.of("ok", false, "error", "笔记不存在");

        NoteReaction existing = noteReactionRepository.findByNoteIdAndUserId(id, userId).orElse(null);
        if (existing != null) {
            if ("dislike".equals(existing.getReactionType())) {
                return Map.of("ok", false, "error", "已踩");
            }
            if ("like".equals(existing.getReactionType())) {
                note.setLikeCount(Math.max(0, note.getLikeCount() - 1));
                existing.setReactionType("dislike");
                noteReactionRepository.save(existing);
            }
        } else {
            NoteReaction reaction = new NoteReaction();
            reaction.setNoteId(id);
            reaction.setUserId(userId);
            reaction.setReactionType("dislike");
            reaction.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            noteReactionRepository.save(reaction);
        }

        note.setDislikeCount(note.getDislikeCount() + 1);
        learningNoteRepository.save(note);
        return Map.of("ok", true, "likeCount", note.getLikeCount(), "dislikeCount", note.getDislikeCount());
    }

    public Map<Long, String> getUserReactions(List<Long> noteIds, String userId) {
        if (userId == null || noteIds.isEmpty()) return Map.of();
        return noteReactionRepository.findByNoteIdInAndUserId(noteIds, userId)
                .stream()
                .collect(Collectors.toMap(NoteReaction::getNoteId, NoteReaction::getReactionType));
    }
}
