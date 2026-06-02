package com.netconfig.repository;

import com.netconfig.entity.CategoryExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryExclusionRepository extends JpaRepository<CategoryExclusion, String> {
}
