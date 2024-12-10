package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.InterfaceUrlConstants;
import com.dhu.constants.RightConstants;
import com.dhu.dao.KnowledgeBaseDao;
import com.dhu.dao.UserDao;
import com.dhu.dao.UserTeamRelationDao;
import com.dhu.dto.KbAddFormDTO;
import com.dhu.dto.KbDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.entity.User;
import com.dhu.entity.UserTeamRelation;
import com.dhu.exception.HttpException;
import com.dhu.exception.NotExistException;
import com.dhu.exception.OperationException;
import com.dhu.service.ChatService;
import com.dhu.service.KnowledgeBaseService;
import com.dhu.service.LogService;
import com.dhu.service.PaperService;
import com.dhu.utils.HttpHelper;
import com.dhu.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {
    @Autowired
    private UserTeamRelationDao userTeamRelationDao;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PaperService paperService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private LogService logService;
    @Resource
    private HttpHelper httpHelper;

    @Override
    public KbDTO querySingle(Integer kbId, Integer userId) {
        KnowledgeBase knowledgeBase = knowledgeBaseDao.selectById(kbId);
        KbDTO dto = new KbDTO();
        BeanUtil.copyProperties(knowledgeBase, dto);
        User user = userDao.selectById(knowledgeBase.getBuilderId());
        dto.setBuilderName(user==null?"已删除用户":user.getName());
        if (knowledgeBase.getBelongsToTeam()) {
            if (UserHolder.getUser().getAdmin()){
                dto.setUserRight(RightConstants.ADMIN);
            }else{
                LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(UserTeamRelation::getTeamId, knowledgeBase.getTeamId()).eq(UserTeamRelation::getUserId, userId);
                UserTeamRelation relation = userTeamRelationDao.selectOne(wrapper);
                if (relation == null) {
                    dto.setUserRight(RightConstants.NOT_RIGHT);
                }
                if (knowledgeBase.getBuilderId().equals(userId)) {
                    dto.setUserRight(RightConstants.ADMIN);
                } else {
                    dto.setUserRight(relation.getUserRight());
                }
            }
        } else {
            dto.setUserRight(RightConstants.ADMIN);
        }
        return dto;
    }

    @Override
    public List<KbDTO> queryKbLimit(Integer userId, int limit) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getBuilderId, userId).eq(KnowledgeBase::getBelongsToTeam, false).orderByDesc(KnowledgeBase::getId).last("limit " + limit);
        List<KnowledgeBase> list = knowledgeBaseDao.selectList(wrapper);
        List<KbDTO> dtoList = new ArrayList<>();
        User user = userDao.selectById(userId);
        for (KnowledgeBase kb : list) {
            KbDTO kbDTO = new KbDTO();
            BeanUtil.copyProperties(kb, kbDTO);
            kbDTO.setUserRight(RightConstants.ADMIN);
            kbDTO.setBuilderName(user==null?"已删除用户":user.getName());
            dtoList.add(kbDTO);
        }
        return dtoList;
    }

    @Override
    public IPage<KbDTO> queryTeamKnowledgeBases(int current, int size, Integer teamId, Integer userId, String search) {
        IPage<KnowledgeBase> page = new Page<>(current, size);
        IPage<KbDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(UserTeamRelation::getUserId, userId).eq(UserTeamRelation::getTeamId, teamId);
        UserTeamRelation relation = userTeamRelationDao.selectOne(relWrapper);
        Integer userRight = relation == null ? RightConstants.NOT_RIGHT : relation.getUserRight();
        wrapper.eq(KnowledgeBase::getTeamId, teamId).eq(KnowledgeBase::getBelongsToTeam, true).like(StringUtils.hasText(search), KnowledgeBase::getName, search).orderByDesc(KnowledgeBase::getId);
        knowledgeBaseDao.selectPage(page, wrapper);
        List<KnowledgeBase> list = page.getRecords();
        List<KbDTO> dtoList = new ArrayList<>();
        for (KnowledgeBase kb : list) {
            KbDTO dto = new KbDTO();
            BeanUtil.copyProperties(kb, dto);
            if (kb.getBuilderId().equals(userId)||UserHolder.getUser().getAdmin()) {
                dto.setUserRight(RightConstants.ADMIN);
            } else {
                dto.setUserRight(userRight);
            }
            User user = userDao.selectById(kb.getBuilderId());
            dto.setBuilderName(user==null?"已删除用户":user.getName());
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public IPage<KbDTO> queryUserKnowledgeBases(int current, int size, Integer userId, String search) {
        IPage<KnowledgeBase> page = new Page<>(current, size);
        IPage<KbDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getBuilderId, userId).eq(KnowledgeBase::getBelongsToTeam, false).like(StringUtils.hasText(search), KnowledgeBase::getName, search).orderByDesc(KnowledgeBase::getId);
        knowledgeBaseDao.selectPage(page, wrapper);
        List<KnowledgeBase> list = page.getRecords();
        List<KbDTO> dtoList = new ArrayList<>();
        User user = userDao.selectById(userId);
        for (KnowledgeBase kb : list) {
            KbDTO dto = new KbDTO();
            BeanUtil.copyProperties(kb, dto);
            dto.setBuilderName(user==null?"已删除用户":user.getName());
            dto.setUserRight(RightConstants.ADMIN);
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
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
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("vector_store_type", "faiss");
        json.fluentPut("embed_model", "bge-large-zh");
        // 调用接口
        String result = httpHelper.post(InterfaceUrlConstants.ADD_KNOWLEDGE_BASE, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {

            logService.log("添加知识库<" + kb.getName() + '>', kb.getTeamId());

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
        //这里必须要用双引号包裹，不知道是为什么
        String result = httpHelper.post(InterfaceUrlConstants.DEL_KNOWLEDGE_BASE, "\"" + knowledgeBase.getIndexUUID() + "\"");
        JSONObject object = JSONObject.parseObject(result);
        if (object.getInteger("code") == 200) {
            //临时添加
            logService.log("删除知识库<" + knowledgeBase.getName() + '>', knowledgeBase.getTeamId());
            return chatService.deleteChatByKb(kbId) && paperService.deletePaperByKb(kbId) && knowledgeBaseDao.deleteById(kbId) > 0;
        } else {
            throw new HttpException("接口访问：删除知识库失败");
        }
    }

    @Override
    public boolean updateKnowledgeBase(KnowledgeBase kb) {
        //临时添加
        logService.log("更新知识库<" + kb.getName() + '>', kb.getTeamId());
        return knowledgeBaseDao.updateById(kb) > 0;
    }

    @Override
    public boolean deleteKnowledgeBases(List<Integer> kbIds) {
        if (!kbIds.isEmpty()) {
            for (Integer kbId : kbIds) {
                //查询uuid
                KnowledgeBase knowledgeBase = knowledgeBaseDao.selectById(kbId);
                if (knowledgeBase == null) {
                    throw new NotExistException("删除的知识库对象不存在");
                }
                //调用接口
                //这里必须要用双引号包裹，不知道是为什么
                String result = httpHelper.post(InterfaceUrlConstants.DEL_KNOWLEDGE_BASE, "\"" + knowledgeBase.getIndexUUID() + "\"");
                JSONObject object = JSONObject.parseObject(result);
                if (object.getInteger("code") != 200) {
                    throw new HttpException("接口访问：删除知识库失败");
                }
                //临时添加
                logService.log("更新知识库<" + knowledgeBase.getName() + '>', knowledgeBase.getTeamId());
            }
            for (Integer kbId : kbIds) {
                if (!paperService.deletePaperByKb(kbId) && !chatService.deleteChatByKb(kbId)) {
                    throw new OperationException("删除知识库出现错误");
                }
            }
            return knowledgeBaseDao.deleteBatchIds(kbIds) == kbIds.size();
        } else {
            return true;
        }
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
