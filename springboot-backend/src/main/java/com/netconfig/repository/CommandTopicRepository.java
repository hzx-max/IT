package com.netconfig.repository;

import com.netconfig.entity.CommandTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandTopicRepository extends JpaRepository<CommandTopic, String> {
    List<CommandTopic> findAllByOrderByCreatedAtDesc();
}
