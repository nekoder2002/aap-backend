package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.InterfaceUrlConstants;
import com.dhu.dao.KnowledgeBaseDao;
import com.dhu.dto.KbAddFormDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.exception.HttpException;
import com.dhu.exception.NotExistException;
import com.dhu.service.KnowledgeBaseService;
import com.dhu.utils.HttpHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Resource
    private HttpHelper httpHelper;

    @Override
    public IPage<KnowledgeBase> queryTeamKnowledgeBases(int current, int size, Integer teamId) {
        IPage<KnowledgeBase> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getTeamId, teamId);
        return knowledgeBaseDao.selectPage(page, wrapper);
    }

    @Override
    public IPage<KnowledgeBase> queryUserKnowledgeBases(int current, int size, Integer userId) {
        IPage<KnowledgeBase> page = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getBuilderId, userId);
        return knowledgeBaseDao.selectPage(page, wrapper);
    }

    @Override
    public boolean insertKnowledgeBase(KbAddFormDTO kbAddForm) {
        KnowledgeBase kb = new KnowledgeBase();
        BeanUtil.copyProperties(kbAddForm, kb);
        //设置默认参数值
        kb.setBuildTime(LocalDateTime.now());
        //设置雪花算法
        kb.setIndexUUID(UUID.randomUUID().toString());
        //构造json
        JSONObject json = new JSONObject();
        // knowledge_base_name：string, 表示知识库的名称，非空且唯一不能重复；
        // vector_store_type：string, 表示向量库类型;
        // embed_model：string, 表示嵌入模型名称.
        json.fluentPut("knowledge_base_name", kb.getName());
        json.fluentPut("vector_store_type", "faiss");
        json.fluentPut("embed_model", "bge-large-zh");
        // 调用接口
        String result = httpHelper.post(InterfaceUrlConstants.ADD_KNOWLEDGE_BASE, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            return knowledgeBaseDao.insert(kb) > 0;
        } else {
            throw new HttpException("接口访问：新建数据库失败");
        }
    }

    @Override
    public boolean deleteKnowledgeBase(Integer kbId) {
        //查询uuid
        KnowledgeBase knowledgeBase = knowledgeBaseDao.selectById(kbId);
        if (knowledgeBase == null) {
            throw new NotExistException("删除的知识库对象不存在");
        }
        //调用接口
        String result = httpHelper.post(InterfaceUrlConstants.DEL_KNOWLEDGE_BASE, knowledgeBase.getIndexUUID());
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            return knowledgeBaseDao.deleteById(kbId) > 0;
        } else {
            throw new HttpException("接口访问：删除知识库失败");
        }
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBase kb) {
        return knowledgeBaseDao.updateById(kb) > 0;
    }

    @Override
    public boolean deleteKnowledgeBases(List<Integer> kbIds) {
        for (Integer kbId : kbIds) {
            //查询uuid
            KnowledgeBase knowledgeBase = knowledgeBaseDao.selectById(kbId);
            if (knowledgeBase == null) {
                throw new NotExistException("删除的知识库对象不存在");
            }
            //调用接口
            String result = httpHelper.post(InterfaceUrlConstants.DEL_KNOWLEDGE_BASE, knowledgeBase.getIndexUUID());
            JSONObject object = JSONObject.parseObject(result);
            if (object.getInteger("code") != 200) {
                throw new HttpException("接口访问：删除知识库失败");
            }
        }
        return knowledgeBaseDao.deleteBatchIds(kbIds) == kbIds.size();
    }

    @Override
    public long countTeamKnowledgeBases(Integer teamId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getTeamId, teamId);
        return knowledgeBaseDao.selectCount(wrapper);
    }

    @Override
    public long countUserKnowledgeBases(Integer userId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getBuilderId, userId);
        return knowledgeBaseDao.selectCount(wrapper);
    }
}
