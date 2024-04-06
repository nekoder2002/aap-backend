package com.dhu.controller;

import com.dhu.constants.BaseConstants;
import com.dhu.dto.KbAddFormDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.service.KnowledgeBaseService;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/kb")
public class KnowLedgeBaseController {
    @Autowired
    KnowledgeBaseService knowledgeBaseService;

    //获取知识库信息列表
    @GetMapping("/list")
    Result getList() {
        return Result.nullFilterData("kbs", knowledgeBaseService.queryKbLimit(UserHolder.getUser().getId(), BaseConstants.QUICK_SEARCH_NUM));
    }

    //根据id获取知识库信息
    @GetMapping("/{kbId}")
    Result get(@PathVariable Integer kbId) {
        if (kbId == null || kbId <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("kb", knowledgeBaseService.querySingle(kbId, UserHolder.getUser().getId()));
    }

    //获取个人的知识库列表
    @GetMapping("/query")
    Result queryList(@RequestParam int current, @RequestParam int size, @RequestParam String search) {
        if (current <= 0 || size <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("kbs", knowledgeBaseService.queryUserKnowledgeBases(current, size, UserHolder.getUser().getId(), search));
    }

    //获取团队知识库列表
    @GetMapping("/query_team")
    Result queryListByTeam(@RequestParam int current, @RequestParam int size, @RequestParam("team_id") Integer teamId, @RequestParam String search) {
        if (current <= 0 || size <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("kbs", knowledgeBaseService.queryTeamKnowledgeBases(current, size, teamId, UserHolder.getUser().getId(), search));
    }

    //插入知识库
    @PutMapping("/insert")
    Result insertKnowledgeBase(@RequestBody KbAddFormDTO kbAddForm) {
        return Result.verifySave(knowledgeBaseService.insertKnowledgeBase(kbAddForm));
    }

    //删除知识库
    @DeleteMapping("/delete")
    Result delete(@RequestParam("kb_id") Integer kbId) {
        return Result.verifyDelete(knowledgeBaseService.deleteKnowledgeBase(kbId));
    }

    //修改知识库
    @PostMapping("/update")
    Result update(@RequestBody KnowledgeBase kb) {
        kb.setBelongsToTeam(null);
        kb.setBelongsToTeam(null);
        kb.setBuildTime(null);
        kb.setTeamId(null);
        kb.setBuilderId(null);
        return Result.verifyUpdate(knowledgeBaseService.updateKnowledgeBase(kb));
    }

    //批量删除知识库
    @DeleteMapping("/multdel")
    Result deleteKnowledgeBases(@RequestBody List<Integer> kbIds) {
        return Result.verifyDelete(knowledgeBaseService.deleteKnowledgeBases(kbIds));
    }

    //查询团队的知识库数量
    Result countTeamKnowledgeBases(Integer teamId) {
        return null;
    }

    //查询个人的知识库数量
    Result countUserKnowledgeBases(Integer userId) {
        return null;
    }
}