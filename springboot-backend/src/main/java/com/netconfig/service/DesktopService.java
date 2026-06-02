package com.netconfig.service;

import com.netconfig.dto.DesktopDTO;
import com.netconfig.entity.Desktop;
import com.netconfig.repository.DesktopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesktopService {

    private final DesktopRepository desktopRepository;

    public List<DesktopDTO> findAll() {
        return desktopRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DesktopDTO findById(String id) {
        return desktopRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public DesktopDTO create(DesktopDTO dto) {
        Desktop entity = toEntity(dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Desktop saved = desktopRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public DesktopDTO update(String id, DesktopDTO dto) {
        Desktop entity = desktopRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Desktop saved = desktopRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        desktopRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        desktopRepository.deleteAllById(ids);
    }

    private DesktopDTO toDTO(Desktop e) {
        DesktopDTO d = new DesktopDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setCategory(e.getCategory());
        d.setSymptom(e.getSymptom());
        d.setSolution(e.getSolution());
        d.setTopo(JsonUtil.toList(e.getTopo()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setImages(JsonUtil.toStringList(e.getImages()));
        d.setVideos(JsonUtil.toStringList(e.getVideos()));
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }

    private Desktop toEntity(DesktopDTO d) {
        Desktop e = new Desktop();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(Desktop e, DesktopDTO d) {
        e.setTitle(d.getTitle());
        e.setCategory(d.getCategory() != null ? d.getCategory() : "");
        e.setSymptom(d.getSymptom() != null ? d.getSymptom() : "");
        e.setSolution(d.getSolution() != null ? d.getSolution() : "");
        e.setTopo(JsonUtil.toJson(d.getTopo()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
        e.setImages(JsonUtil.toJson(d.getImages()));
        e.setVideos(JsonUtil.toJson(d.getVideos()));
    }
}
