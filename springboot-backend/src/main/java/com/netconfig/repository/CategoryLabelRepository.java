package com.netconfig.repository;

import com.netconfig.entity.CategoryLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryLabelRepository extends JpaRepository<CategoryLabel, String> {
}
