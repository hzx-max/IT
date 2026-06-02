package com.netconfig.repository;

import com.netconfig.entity.Fault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaultRepository extends JpaRepository<Fault, String> {
    List<Fault> findAllByOrderByCreatedAtDesc();
}
