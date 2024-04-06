package com.dhu.controller;

import com.dhu.dto.NoteAddFormDTO;
import com.dhu.service.NoteService;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/note")
public class NoteController {
    @Autowired
    private NoteService noteService;

    //获取note信息列表
    @GetMapping("/list")
    Result getList(@RequestParam("paper_id") Integer paperId) {
        return Result.nullFilterData("notes", noteService.queryNotes(paperId));
    }
    
    //插入note
    @PutMapping("/insert")
    Result insert(@RequestBody NoteAddFormDTO noteAddFormDTO) {
        return Result.verifySave(noteService.insertNote(noteAddFormDTO));
    }
}
