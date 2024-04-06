package com.dhu.service;

import com.dhu.dto.NoteAddFormDTO;
import com.dhu.dto.NoteDTO;

import java.util.List;

public interface NoteService {
    //插入笔记
    boolean insertNote(NoteAddFormDTO noteAddFormDTO);
    //查询笔记
    List<NoteDTO> queryNotes(Integer paperId);
    //根据paperId删除笔记
    boolean deleteNotesByPaper(Integer paperId);
}
