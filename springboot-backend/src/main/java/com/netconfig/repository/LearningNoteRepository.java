package com.netconfig.repository;

import com.netconfig.entity.LearningNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LearningNoteRepository extends JpaRepository<LearningNote, Long> {

    List<LearningNote> findByTargetIdAndParentIdIsNullOrderByCreatedAtDesc(String targetId);

    List<LearningNote> findByParentIdOrderByCreatedAtAsc(Long parentId);

    List<LearningNote> findByParentId(Long parentId);

    void deleteByTargetIdAndUsername(String targetId, String username);

    void deleteByTargetId(String targetId);
}
