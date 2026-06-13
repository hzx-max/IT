package com.netconfig.repository;

import com.netconfig.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByIdIn(List<Long> ids);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Favorite> findByUserIdAndModuleAndItemId(String userId, String module, String itemId);
    boolean existsByUserIdAndModuleAndItemId(String userId, String module, String itemId);
    void deleteByUserIdAndModuleAndItemId(String userId, String module, String itemId);
}
