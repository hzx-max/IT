package com.netconfig.service;

import com.netconfig.dto.AiTopicDTO;
import com.netconfig.entity.AiTopic;
import com.netconfig.repository.AiTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiTopicService {

    private final AiTopicRepository aiTopicRepository;

    public List<AiTopicDTO> findAll() {
        return aiTopicRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AiTopicDTO findById(String id) {
        return aiTopicRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public AiTopicDTO create(AiTopicDTO dto) {
        AiTopic entity = toEntity(dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        AiTopic saved = aiTopicRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public AiTopicDTO update(String id, AiTopicDTO dto) {
        AiTopic entity = aiTopicRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        AiTopic saved = aiTopicRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        aiTopicRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        aiTopicRepository.deleteAllById(ids);
    }

    private AiTopicDTO toDTO(AiTopic e) {
        AiTopicDTO d = new AiTopicDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setCategory(e.getCategory());
        d.setScenario(e.getScenario());
        d.setPrompt(e.getPrompt());
        d.setConfig(e.getConfig());
        d.setDesc(e.getDesc());
        d.setDetail(e.getDetail());
        d.setTopo(JsonUtil.toList(e.getTopo()));
        d.setImages(JsonUtil.toStringList(e.getImages()));
        d.setVideos(JsonUtil.toStringList(e.getVideos()));
        d.setFiles(JsonUtil.toList(e.getFiles()));
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }

    private AiTopic toEntity(AiTopicDTO d) {
        AiTopic e = new AiTopic();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(AiTopic e, AiTopicDTO d) {
        e.setTitle(d.getTitle());
        e.setCategory(d.getCategory() != null ? d.getCategory() : "");
        e.setScenario(d.getScenario() != null ? d.getScenario() : "");
        e.setPrompt(d.getPrompt() != null ? d.getPrompt() : "");
        e.setConfig(d.getConfig() != null ? d.getConfig() : "");
        e.setDesc(d.getDesc() != null ? d.getDesc() : "");
        e.setDetail(d.getDetail() != null ? d.getDetail() : "");
        e.setTopo(JsonUtil.toJson(d.getTopo()));
        e.setImages(JsonUtil.toJson(d.getImages()));
        e.setVideos(JsonUtil.toJson(d.getVideos()));
        e.setFiles(JsonUtil.toJson(d.getFiles()));
    }
}