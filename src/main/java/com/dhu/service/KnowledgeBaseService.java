package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.KbAddFormDTO;
import com.dhu.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseService {
    //获取团队的知识库列表
    IPage<KnowledgeBase> queryTeamKnowledgeBases(int current, int size, Integer teamId);

    //获取个人的知识库列表
    IPage<KnowledgeBase> queryUserKnowledgeBases(int current, int size, Integer userId);

    //插入知识库
    boolean insertKnowledgeBase(KbAddFormDTO kbAddForm);

    //删除知识库
    boolean deleteKnowledgeBase(Integer kbId);

    //修改知识库
    boolean updateKnowledgeBase(KnowledgeBase kb);

    //批量删除知识库
    boolean deleteKnowledgeBases(List<Integer> kbIds);

    //查询团队的知识库数量
    long countTeamKnowledgeBases(Integer teamId);

    //查询个人的知识库数量
    long countUserKnowledgeBases(Integer userId);
}
