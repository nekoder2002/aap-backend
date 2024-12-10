package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.InterfaceUrlConstants;
import com.dhu.dao.KnowledgeBaseDao;
import com.dhu.dao.PaperDao;
import com.dhu.dao.UserDao;
import com.dhu.dto.EchartDTO;
import com.dhu.dto.PaperDTO;
import com.dhu.dto.TranslationDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.entity.Paper;
import com.dhu.entity.User;
import com.dhu.exception.HttpException;
import com.dhu.exception.NotExistException;
import com.dhu.service.*;
import com.dhu.utils.BaseUtils;
import com.dhu.utils.ContentUtils;
import com.dhu.utils.HttpHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PaperServicreImpl implements PaperService {
    @Autowired
    private SchedulePaperRelationService schedulePaperRelationService;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private NoteService noteService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private LogService logService;
    @Resource
    private HttpHelper httpHelper;

    @Override
    public PaperDTO querySingle(Integer paperId) {
        Paper paper = paperDao.selectById(paperId);
        if (paper == null) {
            throw new NotExistException("查询的论文不存在，请重试");
        }
        paper.setVisit(paper.getVisit() + 1);
        PaperDTO dto = new PaperDTO();
        BeanUtil.copyProperties(paper, dto);
        JSONArray jsonArray = JSONArray.parseArray(paper.getFreq());
        List<EchartDTO> list = jsonArray.toJavaList(EchartDTO.class);
        dto.setFreqList(list);
        User user = userDao.selectById(paper.getBuilderId());
        dto.setBuilderName(user==null?"已删除用户":user.getName());
        paperDao.updateById(paper);
        return dto;
    }

    @Override
    public IPage<PaperDTO> queryPapers(int current, int size, Integer kbId, String search) {
        IPage<Paper> page = new Page<>(current, size);
        IPage<PaperDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId).like(StringUtils.hasText(search), Paper::getName, search).orderByDesc(Paper::getId);
        paperDao.selectPage(page, wrapper);
        List<Paper> list = page.getRecords();
        List<PaperDTO> dtoList = new ArrayList<>();
        for (Paper paper : list) {
            PaperDTO dto = new PaperDTO();
            BeanUtil.copyProperties(paper, dto);
            dto.setFreqList(List.of());
            User user = userDao.selectById(paper.getBuilderId());
            dto.setBuilderName(user==null?"已删除用户":user.getName());
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public boolean uploadPaper(Integer kbId, Integer builderId, MultipartFile file) {
        KnowledgeBase kb = knowledgeBaseDao.selectById(kbId);
        if (kb == null) {
            throw new NotExistException("上传的知识库不存在，请重试");
        }
        //生成uuid
        String uuid = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("knowledge_base_name", kb.getIndexUUID());
        data.put("override", true);
        data.put("to_vector_store", true);
        data.put("chunk_size", 250);
        data.put("chunk_overlap", 50);
        data.put("zh_title_enhance", true);
        data.put("not_refresh_vs_cache", false);
        data.put("filename_dict", String.format("{'%s':'%s'}", uuid + BaseConstants.PAPER_TYPE, file.getOriginalFilename()));
        File pFile = BaseUtils.toFile(file);
        List<EchartDTO> freq = ContentUtils.getWordList(ContentUtils.readPDF(pFile));
        String result = httpHelper.upload(InterfaceUrlConstants.UPLOAD_FILE, uuid, pFile, data);
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200 && object.getJSONObject("data").getJSONObject("failed_files").getInnerMap().isEmpty()) {
            //插入数据库
            LocalDateTime now = LocalDateTime.now();
            Paper paper = new Paper();
            paper.setName(file.getOriginalFilename());
            paper.setIndexUUID(uuid);
            paper.setBuildTime(now);
            paper.setBuilderId(builderId);
            paper.setKnowledgeBaseId(kbId);
            paper.setVisit(0);
            paper.setFreq(JSON.toJSONString(freq));
            //临时添加
            logService.log("上传论文<" + paper.getName() + ">到知识库<" + kb.getName() + '>', kb.getTeamId());
            return paperDao.insert(paper) > 0;
        } else {
            throw new HttpException("接口访问：插入部分文件失败，论文不符合标准规范");
        }
    }

    @Override
    public boolean deletePaper(Integer paperId) {
        // 查找 paper 所在的知识库
        Paper paper = paperDao.selectById(paperId);
        if (paper == null) {
            throw new NotExistException("删除的论文不存在，请重试");
        }
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("删除的知识库不存在，请重试");
        }
        // 调用接口
        JSONObject json = new JSONObject();
        JSONArray fileArray = new JSONArray();
        fileArray.add(paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("file_names", fileArray);
        //  delete_content：boolean,表示是否从知识库中删除文件源;
        // not_refresh_vs_cache:boolean,表示是否从向量库中删除；
        json.fluentPut("delete_content", true);
        json.fluentPut("not_refresh_vs_cache", false);
        String result = httpHelper.post(InterfaceUrlConstants.DEL_FILE, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            //删除论文
            logService.log("删除论文<" + paper.getName() + ">在知识库<" + kb.getName() + '>', kb.getTeamId());
            //删除论文
            return schedulePaperRelationService.deletePaperRelationsByPaperId(paperId) && noteService.deleteNotesByPaper(paperId) && chatService.deleteChatByPaper(paperId) && paperDao.deleteById(paperId) > 0;
        } else {
            throw new HttpException("接口访问：新建数据库失败");
        }
    }

    @Override
    public boolean deletePaperByKb(Integer kbId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId);
        List<Paper> papers = paperDao.selectList(wrapper);
        for (Paper paper : papers) {
            chatService.deleteChatByPaper(paper.getId());
            noteService.deleteNotesByPaper(paper.getId());
            schedulePaperRelationService.deletePaperRelationsByPaperId(paper.getId());
        }
        return paperDao.delete(wrapper) >= 0;
    }

    @Override
    public boolean updatePaper(Paper paper) {
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        logService.log("更新论文<" + paper.getName() + ">在知识库<" + kb.getName() + '>', kb.getTeamId());
        return paperDao.updateById(paper) > 0;
    }

    @Override
    public boolean deletePapersByKb(List<Integer> paperIds, Integer kbId) {
        KnowledgeBase kb = knowledgeBaseDao.selectById(kbId);
        if (kb == null) {
            throw new NotExistException("删除的知识库不存在，请重试");
        }
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Paper::getId, paperIds).eq(Paper::getKnowledgeBaseId, kbId);
        List<Paper> papers = paperDao.selectList(wrapper);
        if (papers.size() < paperIds.size()) {
            throw new NotExistException("删除的知识库论文列表中存在不存在的");
        }
        List<String> deleteUUIDs = new ArrayList<>();
        for (Paper paper : papers) {
            deleteUUIDs.add(paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        }
        // 调用接口
        JSONObject json = new JSONObject();
        JSONArray fileArray = new JSONArray();
        fileArray.addAll(deleteUUIDs);
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("file_names", fileArray);
        //  delete_content：boolean,表示是否从知识库中删除文件源;
        // not_refresh_vs_cache:boolean,表示是否从向量库中删除；
        json.fluentPut("delete_content", true);
        json.fluentPut("not_refresh_vs_cache", false);
        String result = httpHelper.post(InterfaceUrlConstants.DEL_FILE, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            //删除数据库
            for (Integer id : paperIds) {
                Paper paper = paperDao.selectById(id);
                logService.log("删除论文<" + paper.getName() + ">在知识库<" + kb.getName() + '>', kb.getTeamId());
                chatService.deleteChatByPaper(id);
                noteService.deleteNotesByPaper(id);
            }
            return paperDao.deleteBatchIds(paperIds) > 0;
        } else {
            //获取失败的文件列表
            List<String> failedFiles = object.getJSONObject("data").getJSONArray("failed_files").toJavaList(String.class);
            //剔除未删除的文件列表
            List<String> deleteIds = deleteUUIDs.stream().filter(id -> !failedFiles.contains(id)).toList();
            for (String id : deleteIds) {
                chatService.deleteChatByPaper(Integer.parseInt(id));
                noteService.deleteNotesByPaper(Integer.parseInt(id));
            }
            return paperDao.deleteBatchIds(deleteIds) > 0;
        }
    }

    @Override
    public long countByKb(Integer kbId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId);
        return paperDao.selectCount(wrapper);
    }

    @Override
    public long countByUserPrivate(Integer userId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getBuilderId, userId).eq(KnowledgeBase::getBelongsToTeam, false);
        List<KnowledgeBase> list = knowledgeBaseDao.selectList(wrapper);
        long count = 0;
        for (KnowledgeBase kb : list) {
            count += paperDao.selectCount(new LambdaQueryWrapper<Paper>().eq(Paper::getKnowledgeBaseId, kb.getId()));
        }
        return count;
    }

    @Override
    public boolean download(Integer paperId, HttpServletResponse response) {
        // 查找 paper 所在的知识库
        Paper paper = paperDao.selectById(paperId);
        if (paper == null) {
            throw new NotExistException("下载的论文不存在，请重试");
        }
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("下载的知识库不存在，请重试");
        }
        // 调用第三方接口
        Map<String, String> params = new HashMap<>();
        params.put("knowledge_base_name", kb.getIndexUUID());
        params.put("file_name", paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        params.put("preview", "false");
        httpHelper.downloadFile(InterfaceUrlConstants.FILE_DOWNLOAD, params, response, paper.getName());
        return true;
    }

    @Override
    public boolean preview(Integer paperId, HttpServletResponse response) {
        // 查找 paper 所在的知识库
        Paper paper = paperDao.selectById(paperId);
        if (paper == null) {
            throw new NotExistException("删除的论文不存在，请重试");
        }
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("删除的知识库不存在，请重试");
        }
        // 调用第三方接口
        Map<String, String> params = new HashMap<>();
        params.put("knowledge_base_name", kb.getIndexUUID());
        params.put("file_name", paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        params.put("preview", "true");
        httpHelper.previewFile(InterfaceUrlConstants.FILE_DOWNLOAD, params, response, paper.getName());
        return true;
    }

    @Override
    public String translate(TranslationDTO translationDTO) {
        String result = httpHelper.translate(translationDTO.getQ(), translationDTO.getFrom(), translationDTO.getTo());
        JSONObject object = JSONObject.parseObject(result);
        return object.getJSONArray("trans_result").getJSONObject(0).getString("dst");
    }

    @Override
    public List<String> getQuestions(Integer paperId) {
        // 查找 paper 所在的知识库
        Paper paper = paperDao.selectById(paperId);
        if (paper == null) {
            throw new NotExistException("查找的论文不存在，请重试");
        }
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("查找的知识库不存在，请重试");
        }
        JSONObject json = new JSONObject();
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("file_name", paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        json.fluentPut("temperature", 0.7);
        String result = httpHelper.post(InterfaceUrlConstants.GET_QUESTIONS, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        // 解析问题列表
        if (object.containsKey("answer")) {
            String str = object.getString("answer");
            String[] sp = str.split("\\n");
            List<String> res = null;
            try {
                res = Arrays.stream(sp).filter(it -> it.matches("^\\s*\\d+.+$")).map(it -> it.trim().split(" ")[1]).toList();
            } catch (Exception e) {
                return getQuestions(paperId);
            }
            if (res.size() == 5) {
                return res;
            } else {
                return getQuestions(paperId);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<EchartDTO> countStudy(String kbId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        //返回前20个文章
        wrapper.eq(Paper::getKnowledgeBaseId, kbId).orderByDesc(Paper::getVisit).last("limit " + BaseConstants.STUDY_COUNT_NUM);
        List<Paper> list = paperDao.selectList(wrapper);
        List<EchartDTO> res = new ArrayList<>();
        for (Paper paper : list) {
            EchartDTO dto = new EchartDTO();
            dto.setName(paper.getName());
            dto.setValue(paper.getVisit());
            res.add(dto);
        }
        return res;
    }
}
