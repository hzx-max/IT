package com.netconfig.repository;

import com.netconfig.entity.Linux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinuxRepository extends JpaRepository<Linux, String> {
}
