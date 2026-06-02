package com.netconfig.repository;

import com.netconfig.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    List<SearchHistory> findTop5ByModuleOrderBySearchedAtDesc(String module);
    void deleteByModuleAndKeyword(String module, String keyword);
    long countByModule(String module);
    void deleteByIdLessThanEqual(Long id);
}
