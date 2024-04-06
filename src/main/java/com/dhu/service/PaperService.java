package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.PaperDTO;
import com.dhu.dto.TranslationDTO;
import com.dhu.entity.Paper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PaperService {

    //获取单个论文
    PaperDTO querySingle(Integer paperId);

    //获取知识库的Paper列表
    IPage<PaperDTO> queryPapers(int current, int size, Integer kbId,String search);

    //上传论文
    boolean uploadPaper(Integer kbId, Integer builderId, MultipartFile files);

    //删除论文
    boolean deletePaper(Integer paperId);

    //删除知识库中所有论文
    boolean deletePaperByKb(Integer kbId);

    //修改论文
    boolean updatePaper(Paper paper);

    //批量删除一个知识库中的论文
    boolean deletePapersByKb(List<Integer> paperIds, Integer kbId);

    //查询知识库中论文数量
    long countTeamKnowledgeBases(Integer kbId);

    //下载论文
    boolean download(Integer paperId, HttpServletResponse response);

    //预览论文
    boolean preview(Integer paperId, HttpServletResponse response);

    //翻译
    String translate(TranslationDTO translationDTO);
}
