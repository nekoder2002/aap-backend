package com.dhu.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.InterfaceUrlConstants;
import com.dhu.dao.KnowledgeBaseDao;
import com.dhu.dao.PaperDao;
import com.dhu.dto.PapersAddDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.entity.Paper;
import com.dhu.exception.HttpException;
import com.dhu.exception.NotExistException;
import com.dhu.service.PaperService;
import com.dhu.utils.HttpHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PaperServicreImpl implements PaperService {
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Resource
    private HttpHelper httpHelper;

    @Override
    public IPage<Paper> queryPapers(int current, int size, Integer kbId) {
        IPage<Paper> page = new Page<>(current, size);
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId);
        return paperDao.selectPage(page, wrapper);
    }

    @Override
    public boolean uploadPaper(PapersAddDTO papersAddDTO, MultipartFile[] files) {
        KnowledgeBase kb = knowledgeBaseDao.selectById(papersAddDTO.getKbId());
        if (kb == null) {
            throw new NotExistException("上传的知识库不存在，请重试");
        }
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < papersAddDTO.getNames().size(); i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("knowledge_base_name", kb.getIndexUUID());
        data.put("override", true);
        data.put("to_vector_store", true);
        data.put("chunk_size", 250);
        data.put("chunk_overlap", 50);
        data.put("zh_title_enhance", true);
        data.put("not_refresh_vs_cache", false);
        String result = httpHelper.upload(InterfaceUrlConstants.UPLOAD_FILE, files, data);
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            //插入数据库
            LocalDateTime now = LocalDateTime.now();
            int count = 0;
            for (String name : papersAddDTO.getNames()) {
                Paper paper = new Paper();
                paper.setName(name);
                paper.setIndexUUID(uuids.get(count));
                paper.setBuildTime(now);
                paper.setBuilderId(papersAddDTO.getBuilderId());
                paper.setKnowledgeBaseId(papersAddDTO.getKbId());
                count += paperDao.insert(paper);
            }
            return count == papersAddDTO.getNames().size();
        } else {
            //获取失败的文件列表
            throw new HttpException("接口访问：插入部分文件失败");
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
            //删除数据库
            return paperDao.deleteById(paperId) > 0;
        } else {
            throw new HttpException("接口访问：新建数据库失败");
        }
    }

    @Override
    public boolean deletePaperByKb(Integer kbId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId);
        return paperDao.delete(wrapper) > 0;
    }

    @Override
    public boolean updatePaper(Paper paper) {
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
            return paperDao.deleteBatchIds(paperIds) > 0;
        } else {
            //获取失败的文件列表
            List<String> failedFiles = object.getJSONObject("data").getJSONArray("failed_files").toJavaList(String.class);
            //剔除未删除的文件列表
            List<String> deleteIds = deleteUUIDs.stream().filter(id -> !failedFiles.contains(id)).toList();
            paperDao.deleteBatchIds(deleteIds);
            throw new HttpException("接口访问：删除部分文件失败");
        }
    }

    @Override
    public long countTeamKnowledgeBases(Integer kbId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Paper::getKnowledgeBaseId, kbId);
        return paperDao.selectCount(wrapper);
    }
}
