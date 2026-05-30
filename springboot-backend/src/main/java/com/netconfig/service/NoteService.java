package com.netconfig.service;

import com.netconfig.entity.Note;
import com.netconfig.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;

    public String getContent(String cmdId) {
        return noteRepository.findById(cmdId)
                .map(Note::getContent)
                .orElse("");
    }

    @Transactional
    public void save(String cmdId, String content) {
        Note note = noteRepository.findById(cmdId).orElse(new Note());
        note.setCmdId(cmdId);
        note.setContent(content != null ? content : "");
        noteRepository.save(note);
    }
}
