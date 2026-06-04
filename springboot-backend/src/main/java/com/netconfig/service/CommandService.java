package com.netconfig.service;

import com.netconfig.dto.CommandDTO;
import com.netconfig.entity.CommandConfig;
import com.netconfig.entity.CommandTopic;
import com.netconfig.repository.ClickRecordRepository;
import com.netconfig.repository.CommandConfigRepository;
import com.netconfig.repository.CommandTopicRepository;
import com.netconfig.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final CommandTopicRepository topicRepository;
    private final CommandConfigRepository configRepository;
    private final NoteRepository noteRepository;
    private final ClickRecordRepository clickRecordRepository;

    public List<CommandDTO> findAll() {
        List<CommandTopic> topics = topicRepository.findAllByOrderByCreatedAtDesc();
        return topics.stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    public CommandDTO findById(String id) {
        return topicRepository.findById(id).map(this::toDetailDTO).orElse(null);
    }

    @Transactional
    public CommandDTO create(CommandDTO dto) {
        CommandTopic topic = new CommandTopic();
        topic.setId(dto.getId() != null ? dto.getId() : generateId());
        topic.setTitle(dto.getTitle());
        topic.setCat(dto.getCat());
        topic.setTopo(JsonUtil.toJson(dto.getTopo()));
        topic.setDesc(dto.getDesc() != null ? dto.getDesc() : "");
        topic.setDetail(dto.getDetail() != null ? dto.getDetail() : "");
        topic.setFiles(JsonUtil.toJson(dto.getFiles()));
        topic.setCreatedAt(JsonUtil.nowLocal());
        CommandTopic saved = topicRepository.save(topic);

        if (dto.getConfigs() != null) {
            for (CommandDTO.ConfigItem item : dto.getConfigs()) {
                saveConfig(saved.getId(), item);
            }
        }
        return toDetailDTO(saved);
    }

    @Transactional
    public CommandDTO update(String id, CommandDTO dto) {
        CommandTopic topic = topicRepository.findById(id).orElse(null);
        if (topic == null) return null;
        topic.setTitle(dto.getTitle());
        topic.setCat(dto.getCat());
        topic.setTopo(JsonUtil.toJson(dto.getTopo()));
        topic.setDesc(dto.getDesc() != null ? dto.getDesc() : "");
        topic.setDetail(dto.getDetail() != null ? dto.getDetail() : "");
        topic.setFiles(JsonUtil.toJson(dto.getFiles()));
        topic.setCreatedAt(JsonUtil.nowLocal());
        topicRepository.save(topic);

        configRepository.deleteByTopicId(id);
        if (dto.getConfigs() != null) {
            for (CommandDTO.ConfigItem item : dto.getConfigs()) {
                saveConfig(id, item);
            }
        }
        return toDetailDTO(topic);
    }

    @Transactional
    public void delete(String id) {
        noteRepository.deleteById(id);
        configRepository.deleteByTopicId(id);
        topicRepository.deleteById(id);
        clickRecordRepository.deleteByModuleAndItemId("cmd", id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        noteRepository.deleteAllById(ids);
        configRepository.deleteByTopicIdIn(ids);
        topicRepository.deleteAllById(ids);
        for (String id : ids) {
            clickRecordRepository.deleteByModuleAndItemId("cmd", id);
        }
    }

    private void saveConfig(String topicId, CommandDTO.ConfigItem item) {
        CommandConfig cfg = new CommandConfig();
        cfg.setId(item.getId() != null ? item.getId() : "cfg_" + item.getVendor() + "_" + topicId);
        cfg.setTopicId(topicId);
        cfg.setVendor(item.getVendor() != null ? item.getVendor() : "");
        cfg.setConfig(item.getConfig() != null ? item.getConfig() : "");
        cfg.setComment(item.getComment() != null ? item.getComment() : "");
        cfg.setDoc(item.getDoc() != null ? item.getDoc() : "");
        cfg.setVerificationCmd(item.getVerificationCmd() != null ? item.getVerificationCmd() : "");
        cfg.setVerificationImages(JsonUtil.toJson(item.getVerificationImages()));
        configRepository.save(cfg);
    }

    private CommandDTO toSummaryDTO(CommandTopic t) {
        CommandDTO d = new CommandDTO();
        d.setId(t.getId());
        d.setTitle(t.getTitle());
        d.setCat(t.getCat());
        d.setTopo(JsonUtil.toList(t.getTopo()));
        d.setDesc(t.getDesc());
        d.setCreatedAt(t.getCreatedAt());
        List<CommandConfig> cfgs = configRepository.findByTopicIdOrderByVendor(t.getId());
        d.setConfigs(cfgs.stream().map(this::toConfigItem).collect(Collectors.toList()));
        return d;
    }

    private CommandDTO toDetailDTO(CommandTopic t) {
        CommandDTO d = new CommandDTO();
        d.setId(t.getId());
        d.setTitle(t.getTitle());
        d.setCat(t.getCat());
        d.setTopo(JsonUtil.toList(t.getTopo()));
        d.setDesc(t.getDesc());
        d.setDetail(t.getDetail());
        d.setFiles(JsonUtil.toList(t.getFiles()));
        d.setCreatedAt(t.getCreatedAt());
        List<CommandConfig> cfgs = configRepository.findByTopicIdOrderByVendor(t.getId());
        d.setConfigs(cfgs.stream().map(this::toConfigItem).collect(Collectors.toList()));
        return d;
    }

    private CommandDTO.ConfigItem toConfigItem(CommandConfig c) {
        CommandDTO.ConfigItem item = new CommandDTO.ConfigItem();
        item.setId(c.getId());
        item.setVendor(c.getVendor());
        item.setConfig(c.getConfig());
        item.setComment(c.getComment());
        item.setDoc(c.getDoc());
        item.setVerificationCmd(c.getVerificationCmd());
        item.setVerificationImages(JsonUtil.toList(c.getVerificationImages()));
        return item;
    }

    private String generateId() {
        return "topic_" + System.currentTimeMillis() + "_" + (char) ('a' + new Random().nextInt(26));
    }
}
