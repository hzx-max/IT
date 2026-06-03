package com.netconfig.service;

import com.netconfig.dto.FaultDTO;
import com.netconfig.entity.Fault;
import com.netconfig.repository.FaultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaultService {

    private final FaultRepository faultRepository;

    public List<FaultDTO> findAll() {
        return faultRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public FaultDTO findById(String id) {
        return faultRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public FaultDTO create(FaultDTO dto) {
        Fault entity = toEntity(dto);
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId("fault_" + System.currentTimeMillis() + "_" + (char) ('a' + new Random().nextInt(26)));
        }
        entity.setCreatedAt(JsonUtil.nowLocal());
        Fault saved = faultRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public FaultDTO update(String id, FaultDTO dto) {
        Fault entity = faultRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Fault saved = faultRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        faultRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        faultRepository.deleteAllById(ids);
    }

    private FaultDTO toDTO(Fault e) {
        FaultDTO d = new FaultDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setCategory(e.getCategory());
        d.setSymptom(e.getSymptom());
        d.setCause(e.getCause());
        d.setSolution(e.getSolution());
        d.setTopo(JsonUtil.toList(e.getTopo()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setImages(JsonUtil.toStringList(e.getImages()));
        d.setVideos(JsonUtil.toStringList(e.getVideos()));
        d.setFiles(JsonUtil.toList(e.getFiles()));
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }

    private Fault toEntity(FaultDTO d) {
        Fault e = new Fault();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(Fault e, FaultDTO d) {
        e.setTitle(d.getTitle());
        e.setCategory(d.getCategory() != null ? d.getCategory() : "");
        e.setSymptom(d.getSymptom() != null ? d.getSymptom() : "");
        e.setCause(d.getCause() != null ? d.getCause() : "");
        e.setSolution(d.getSolution() != null ? d.getSolution() : "");
        e.setTopo(JsonUtil.toJson(d.getTopo()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
        e.setImages(JsonUtil.toJson(d.getImages()));
        e.setVideos(JsonUtil.toJson(d.getVideos()));
        e.setFiles(JsonUtil.toJson(d.getFiles()));
    }
}
