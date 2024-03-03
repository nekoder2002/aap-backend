package com.dhu.controller;

import com.dhu.entity.Paper;
import com.dhu.service.PaperService;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/paper")
public class PaperController {
    @Autowired
    private PaperService paperService;

    //获取知识库的Paper列表
    @GetMapping("/query")
    Result queryList(@RequestParam int current, @RequestParam int size, @RequestParam("kb_id") Integer kbId) {
        if (current <= 0 || size <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("papers", paperService.queryPapers(current, size, kbId));
    }

    //上传论文
    @PostMapping("/insert")
    Result insert(Integer kbId, MultipartFile file) {
        if (kbId == null || kbId <= 0 || file == null) {
            return Result.saveErr().setMsg("上传参数错误");
        }
        return Result.verifySave(paperService.uploadPaper(kbId, UserHolder.getUser().getId(), file));
    }

    //删除论文
    @DeleteMapping("/delete")
    Result deletePaper(@RequestParam("paper_id") Integer paperId) {
        return Result.verifyDelete(paperService.deletePaper(paperId));
    }

    //修改论文
    @PostMapping("/update")
    Result updatePaper(@RequestBody Paper paper) {
        paper.setBuildTime(null);
        paper.setBuilderId(null);
        return Result.verifyUpdate(paperService.updatePaper(paper));
    }

    //批量删除一个知识库中的论文
    @DeleteMapping("/multdel")
    Result deletePapersByKb(@RequestBody List<Integer> paperIds, @RequestParam("kb_id") Integer kbId) {
        return Result.verifyDelete(paperService.deletePapersByKb(paperIds, kbId));
    }

    //查询知识库中论文数量
    Result countTeamKnowledgeBases(Integer kbId) {
        return null;
    }

    //下载论文
    @GetMapping("/download")
    public void download(@RequestParam("paper_id") Integer paperId, HttpServletResponse response) {
        paperService.download(paperId,response);
    }
}
