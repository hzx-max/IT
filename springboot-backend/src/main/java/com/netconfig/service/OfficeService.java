package com.netconfig.service;

import com.netconfig.dto.OfficeDTO;
import com.netconfig.entity.Office;
import com.netconfig.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeRepository officeRepository;

    public List<OfficeDTO> findAll() {
        return officeRepository.findAll().stream()
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
        Office saved = officeRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public OfficeDTO update(String id, OfficeDTO dto) {
        Office entity = officeRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        Office saved = officeRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        officeRepository.deleteById(id);
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
        d.setConfigs(JsonUtil.toMap(e.getConfigs()));
        d.setComments(JsonUtil.toMap(e.getComments()));
        d.setDocs(JsonUtil.toMap(e.getDocs()));
        d.setCreatedAt(JsonUtil.utcToLocal(e.getCreatedAt()));
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
        e.setConfigs(JsonUtil.toJson(d.getConfigs()));
        e.setComments(JsonUtil.toJson(d.getComments()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
    }
}
