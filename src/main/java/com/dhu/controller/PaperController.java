package com.dhu.controller;

import com.dhu.dto.TranslationDTO;
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

    //获取单个论文
    @GetMapping("/{paperId}")
    Result get(@PathVariable Integer paperId) {
        if (paperId == null || paperId <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("paper", paperService.querySingle(paperId));
    }

    //获取知识库的Paper列表
    @GetMapping("/query")
    Result queryList(@RequestParam int current, @RequestParam int size, @RequestParam("kb_id") Integer kbId, @RequestParam String search) {
        if (current <= 0 || size <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("papers", paperService.queryPapers(current, size, kbId, search));
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
    @GetMapping("/count_kb")
    Result countTeamKnowledgeBases(Integer kbId) {
        return Result.nullFilterData("count", paperService.countByKb(kbId));
    }

    //查询个人论文数量
    @GetMapping("/count")
    Result countPaper() {
        return Result.nullFilterData("count", paperService.countByUserPrivate(UserHolder.getUser().getId()));
    }

    //下载论文
    @GetMapping("/download")
    public void download(@RequestParam("paper_id") Integer paperId, HttpServletResponse response) {
        paperService.download(paperId, response);
    }

    //预览论文
    @GetMapping("/preview")
    public void preview(@RequestParam("paper_id") Integer paperId, HttpServletResponse response) {
        paperService.preview(paperId, response);
    }

    //生成问题
    @GetMapping("questions")
    public Result questions(@RequestParam("paper_id") Integer paperId){
        return Result.nullFilterData("questions",paperService.getQuestions(paperId));
    }

    //翻译接口
    @PostMapping("/translate")
    public Result translate(@RequestBody TranslationDTO translationDTO) {
        return Result.nullFilterData("text", paperService.translate(translationDTO));
    }

    //echarts学习统计接口
    @GetMapping("study_count")
    public Result studyStatic(@RequestParam("kb_id")String kbId){
        return Result.nullFilterData("study",paperService.countStudy(kbId));
    }
}
