package com.netconfig.repository;

import com.netconfig.entity.PendingChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PendingChangeRepository extends JpaRepository<PendingChange, Long> {
    List<PendingChange> findByStatusOrderByCreatedAtDesc(PendingChange.ChangeStatus status);
    List<PendingChange> findAllByOrderByCreatedAtDesc();
}