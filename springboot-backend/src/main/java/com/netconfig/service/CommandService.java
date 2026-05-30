package com.netconfig.service;

import com.netconfig.dto.CommandDTO;
import com.netconfig.entity.Command;
import com.netconfig.repository.CommandRepository;
import com.netconfig.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandService {

    private final CommandRepository commandRepository;
    private final NoteRepository noteRepository;

    public List<CommandDTO> findAll() {
        return commandRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CommandDTO findById(String id) {
        return commandRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Transactional
    public CommandDTO create(CommandDTO dto) {
        Command entity = toEntity(dto);
        Command saved = commandRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public CommandDTO update(String id, CommandDTO dto) {
        Command entity = commandRepository.findById(id).orElse(null);
        if (entity == null) return null;
        updateEntity(entity, dto);
        Command saved = commandRepository.save(entity);
        return toDTO(saved);
    }

    @Transactional
    public void delete(String id) {
        noteRepository.deleteById(id);
        commandRepository.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<String> ids) {
        noteRepository.deleteAllById(ids);
        commandRepository.deleteAllById(ids);
    }

    private CommandDTO toDTO(Command e) {
        CommandDTO d = new CommandDTO();
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
        d.setVerification(JsonUtil.toMap(e.getVerification()));
        d.setCreatedAt(JsonUtil.utcToLocal(e.getCreatedAt()));
        return d;
    }

    private Command toEntity(CommandDTO d) {
        Command e = new Command();
        e.setId(d.getId());
        updateEntity(e, d);
        return e;
    }

    private void updateEntity(Command e, CommandDTO d) {
        e.setTitle(d.getTitle());
        e.setVendor(d.getVendor());
        e.setCat(d.getCat());
        e.setTopo(JsonUtil.toJson(d.getTopo()));
        e.setDesc(d.getDesc() != null ? d.getDesc() : "");
        e.setDetail(d.getDetail() != null ? d.getDetail() : "");
        e.setConfigs(JsonUtil.toJson(d.getConfigs()));
        e.setComments(JsonUtil.toJson(d.getComments()));
        e.setDocs(JsonUtil.toJson(d.getDocs()));
        e.setVerification(JsonUtil.toJson(d.getVerification()));
    }
}
