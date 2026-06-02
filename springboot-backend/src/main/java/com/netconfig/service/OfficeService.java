package com.netconfig.service;

import com.netconfig.dto.OfficeDTO;
import com.netconfig.entity.Office;
import com.netconfig.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeRepository officeRepository;

    public List<OfficeDTO> findAll() {
        return officeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OfficeDTO findById(String id) {
        return officeRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public OfficeDTO create(OfficeDTO dto) {
        Office entity = toEntity(dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Office saved = officeRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public OfficeDTO update(String id, OfficeDTO dto) {
        Office entity = officeRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Office saved = officeRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        officeRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        officeRepository.deleteAllById(ids);
    }

    private OfficeDTO toDTO(Office e) {
        OfficeDTO d = new OfficeDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setVendor(e.getVendor());
        d.setCat(e.getCat());
        d.setTopo(JsonUtil.toList(e.getTopo()));
        d.setDesc(e.getDesc());
        d.setDetail(e.getDetail());
        Map<String, String> configsMap = JsonUtil.toMap(e.getConfigs());
        d.setConfigs(configsMap);
        if (configsMap != null && !configsMap.isEmpty()) {
            String vendor = e.getVendor() != null ? e.getVendor() : "default";
            String cfg = configsMap.get(vendor);
            if (cfg == null && !configsMap.isEmpty()) {
                cfg = configsMap.values().iterator().next();
            }
            d.setConfig(cfg != null ? cfg : "");
        }
        d.setComments(JsonUtil.toMap(e.getComments()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setImages(JsonUtil.toStringList(e.getImages()));
        d.setVideos(JsonUtil.toStringList(e.getVideos()));
        d.setFiles(JsonUtil.toList(e.getFiles()));
        d.setCreatedAt(e.getCreatedAt());
        return d;
    }

    private Office toEntity(OfficeDTO d) {
        Office e = new Office();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(Office e, OfficeDTO d) {
        e.setTitle(d.getTitle());
        e.setVendor(d.getVendor());
        e.setCat(d.getCat());
        e.setTopo(JsonUtil.toJson(d.getTopo()));
        e.setDesc(d.getDesc() != null ? d.getDesc() : "");
        e.setDetail(d.getDetail() != null ? d.getDetail() : "");
        e.setConfigs(buildConfigsJson(d));
        e.setComments(JsonUtil.toJson(d.getComments()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
        e.setImages(JsonUtil.toJson(d.getImages()));
        e.setVideos(JsonUtil.toJson(d.getVideos()));
        e.setFiles(JsonUtil.toJson(d.getFiles()));
    }

    private String buildConfigsJson(OfficeDTO d) {
        if (d.getConfigs() != null && !d.getConfigs().isEmpty()) {
            return JsonUtil.toJson(d.getConfigs());
        }
        if (d.getConfig() != null && !d.getConfig().isEmpty()) {
            Map<String, String> map = new LinkedHashMap<>();
            String vendor = d.getVendor() != null ? d.getVendor() : "default";
            map.put(vendor, d.getConfig());
            return JsonUtil.toJson(map);
        }
        return "{}";
    }
}
