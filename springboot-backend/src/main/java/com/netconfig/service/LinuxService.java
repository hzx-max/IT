package com.netconfig.service;

import com.netconfig.dto.LinuxDTO;
import com.netconfig.entity.Linux;
import com.netconfig.repository.LinuxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinuxService {

    private final LinuxRepository linuxRepository;

    public List<LinuxDTO> findAll() {
        return linuxRepository.findAll().stream()
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
        Linux saved = linuxRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public LinuxDTO update(String id, LinuxDTO dto) {
        Linux entity = linuxRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        Linux saved = linuxRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        linuxRepository.deleteById(id);
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
        d.setConfigs(JsonUtil.toMap(e.getConfigs()));
        d.setComments(JsonUtil.toMap(e.getComments()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setCreatedAt(JsonUtil.utcToLocal(e.getCreatedAt()));
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
        e.setConfigs(JsonUtil.toJson(d.getConfigs()));
        e.setComments(JsonUtil.toJson(d.getComments()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
    }
}
