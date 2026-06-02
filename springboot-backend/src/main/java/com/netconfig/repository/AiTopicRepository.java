package com.netconfig.repository;

import com.netconfig.entity.AiTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiTopicRepository extends JpaRepository<AiTopic, String> {
    List<AiTopic> findAllByOrderByCreatedAtDesc();
}