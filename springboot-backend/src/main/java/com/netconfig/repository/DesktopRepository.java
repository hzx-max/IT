package com.netconfig.repository;

import com.netconfig.entity.Desktop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesktopRepository extends JpaRepository<Desktop, String> {
    List<Desktop> findAllByOrderByCreatedAtDesc();
}
