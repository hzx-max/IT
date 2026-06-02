package com.netconfig.repository;

import com.netconfig.entity.Linux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LinuxRepository extends JpaRepository<Linux, String> {
    List<Linux> findAllByOrderByCreatedAtDesc();
}
