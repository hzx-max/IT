package com.netconfig.service;

import com.netconfig.dto.LinuxDTO;
import com.netconfig.entity.Linux;
import com.netconfig.repository.LinuxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinuxService {

    private final LinuxRepository linuxRepository;

    public List<LinuxDTO> findAll() {
        return linuxRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public LinuxDTO findById(String id) {
        return linuxRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public LinuxDTO create(LinuxDTO dto) {
        Linux entity = toEntity(dto);
        if (entity.getId() == null || entity.getId().isEmpty()) {
            entity.setId("linux_" + System.currentTimeMillis() + "_" + (char) ('a' + new Random().nextInt(26)));
        }
        entity.setCreatedAt(JsonUtil.nowLocal());
        Linux saved = linuxRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public LinuxDTO update(String id, LinuxDTO dto) {
        Linux entity = linuxRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        entity.setCreatedAt(JsonUtil.nowLocal());
        Linux saved = linuxRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        linuxRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        linuxRepository.deleteAllById(ids);
    }

    private LinuxDTO toDTO(Linux e) {
        LinuxDTO d = new LinuxDTO();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setVendor(e.getVendor());
        d.setCat(e.getCat());
        d.setTopo(JsonUtil.toList(e.getTopo()));
        d.setDesc(e.getDesc());
        d.setDetail(e.getDetail());
        Map<String, String> configsMap = JsonUtil.toMap(e.getConfigs());
        d.setConfigs(configsMap);
        d.setComments(JsonUtil.toMap(e.getComments()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setImages(JsonUtil.toStringList(e.getImages()));
        d.setVideos(JsonUtil.toStringList(e.getVideos()));
        d.setFiles(JsonUtil.toList(e.getFiles()));
        d.setCreatedAt(e.getCreatedAt());
        if (configsMap.containsKey(e.getVendor())) {
            d.setConfig(configsMap.get(e.getVendor()));
        } else if (!configsMap.isEmpty()) {
            d.setConfig(configsMap.values().iterator().next());
        }
        return d;
    }

    private Linux toEntity(LinuxDTO d) {
        Linux e = new Linux();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(Linux e, LinuxDTO d) {
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

    private String buildConfigsJson(LinuxDTO d) {
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
