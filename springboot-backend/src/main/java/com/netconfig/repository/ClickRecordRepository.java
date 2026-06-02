package com.netconfig.repository;

import com.netconfig.entity.ClickRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClickRecordRepository extends JpaRepository<ClickRecord, Long> {

    Optional<ClickRecord> findByModuleAndItemId(String module, String itemId);

    @Query("SELECT c.module, SUM(c.count) FROM ClickRecord c GROUP BY c.module ORDER BY SUM(c.count) DESC")
    List<Object[]> sumByModule();

    List<ClickRecord> findTop10ByOrderByCountDesc();

    List<ClickRecord> findTop10ByModuleOrderByCountDesc(String module);
}