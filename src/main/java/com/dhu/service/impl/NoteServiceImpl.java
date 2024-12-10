package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhu.dao.NoteDao;
import com.dhu.dao.UserDao;
import com.dhu.dto.NoteAddFormDTO;
import com.dhu.dto.NoteDTO;
import com.dhu.entity.Note;
import com.dhu.entity.User;
import com.dhu.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteDao noteDao;
    @Autowired
    private UserDao userDao;

    @Override
    public boolean insertNote(NoteAddFormDTO noteAddFormDTO) {
        Note note = new Note();
        BeanUtil.copyProperties(noteAddFormDTO, note);
        note.setBuildTime(LocalDateTime.now());
        return noteDao.insert(note) > 0;
    }

    @Override
    public List<NoteDTO> queryNotes(Integer paperId) {
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getPaperId, paperId).orderByDesc(Note::getBuildTime);
        List<Note> notes = noteDao.selectList(noteWrapper);
        List<NoteDTO> res = new ArrayList<>();
        for (Note note : notes) {
            NoteDTO noteDTO = new NoteDTO();
            BeanUtil.copyProperties(note, noteDTO);
            User user = userDao.selectById(note.getUserId());
            noteDTO.setUserName(user==null?"已删除用户":user.getName());
            res.add(noteDTO);
        }
        return res;
    }

    @Override
    public boolean deleteNotesByPaper(Integer paperId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getPaperId, paperId);
        return noteDao.delete(wrapper) >= 0;
    }
}
