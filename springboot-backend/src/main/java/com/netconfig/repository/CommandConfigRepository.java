package com.netconfig.repository;

import com.netconfig.entity.CommandConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandConfigRepository extends JpaRepository<CommandConfig, String> {
    List<CommandConfig> findByTopicIdOrderByVendor(String topicId);
    void deleteByTopicId(String topicId);
    void deleteByTopicIdIn(List<String> topicIds);
}
