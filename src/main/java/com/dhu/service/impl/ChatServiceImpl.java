package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.InterfaceUrlConstants;
import com.dhu.dao.*;
import com.dhu.dto.KbChatDTO;
import com.dhu.dto.KbDocDTO;
import com.dhu.dto.PaperChatDTO;
import com.dhu.dto.PaperDocDTO;
import com.dhu.entity.*;
import com.dhu.exception.NotExistException;
import com.dhu.service.ChatService;
import com.dhu.utils.HttpHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private PaperChatDao paperChatDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Autowired
    private KnowledgeBaseChatDao knowledgeBaseChatDao;
    @Resource
    private HttpHelper httpHelper;

    @Override
    public boolean chatWithPaper(PaperChatDTO paperChat) {
        //查询paper所在的知识库
        Paper paper = paperDao.selectById(paperChat.getPaperId());
        if (paper == null) {
            throw new NotExistException("目标论文不存在，对话失败");
        }
        KnowledgeBase kb = knowledgeBaseDao.selectById(paper.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("目标知识库不存在，对话失败");
        }
        JSONArray history = new JSONArray();
        history.addAll(paperChat.getHistory());
        //调用接口
        JSONObject json = new JSONObject();
        json.fluentPut("query", paperChat.getQuestion());
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("file_name", paper.getIndexUUID() + BaseConstants.PAPER_TYPE);
        json.fluentPut("top_k", 5);
        json.fluentPut("history", history);
        json.fluentPut("stream", false);
        json.fluentPut("model_name", "chatglm3-6b");
        json.fluentPut("temperature", 0.7);
        json.fluentPut("max_tokens", 1024);
        String result = httpHelper.post(InterfaceUrlConstants.PAPER_CHAT, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        String answer = object.getString("answer");
        paperChat.setAnswer(answer);
        PaperChat chat = new PaperChat();
        BeanUtil.copyProperties(paperChat, chat);
        if (!object.getString("docs").contains("未找到")) {
            JSONArray docs = object.getJSONArray("docs");
            paperChat.setDocs(docs.toJavaList(PaperDocDTO.class));
            chat.setData(docs.toString());
        } else {
            paperChat.setDocs(List.of());
            chat.setData("[]");
        }
        return paperChatDao.insert(chat) > 0;
    }

    @Override
    public boolean chatWithKb(KbChatDTO kbChat) {
        KnowledgeBase kb = knowledgeBaseDao.selectById(kbChat.getKnowledgeBaseId());
        if (kb == null) {
            throw new NotExistException("目标知识库不存在，对话失败");
        }
        JSONArray history = new JSONArray();
        history.addAll(kbChat.getHistory());
        //调用接口
        JSONObject json = new JSONObject();
        json.fluentPut("query", kbChat.getQuestion());
        json.fluentPut("knowledge_base_name", kb.getIndexUUID());
        json.fluentPut("top_k", 4);
        json.fluentPut("score_threshold", 0.5);
        json.fluentPut("history", history);
        json.fluentPut("stream", false);
        json.fluentPut("model_name", "chatglm3-6b");
        json.fluentPut("temperature", 0.7);
        json.fluentPut("max_tokens", 1024);
        json.fluentPut("prompt_name", "default");
        String result = httpHelper.post(InterfaceUrlConstants.KB_CHAT, json.toString());
        JSONObject object = JSONObject.parseObject(result);
        String answer = object.getString("answer");        //存入数据库
        kbChat.setAnswer(answer);
        KnowledgeBaseChat chat = new KnowledgeBaseChat();
        BeanUtil.copyProperties(kbChat, chat);
        if (!object.getString("docs").contains("未找到")) {
            JSONArray docs = object.getJSONArray("docs");
            kbChat.setDocs(docs.toJavaList(KbDocDTO.class));
            chat.setData(docs.toString());
        } else {
            kbChat.setDocs(List.of());
            chat.setData("[]");
        }
        return knowledgeBaseChatDao.insert(chat) > 0;
    }

    @Override
    public List<PaperChatDTO> queryPaperChatRecords(Integer paperId, int limit) {
        List<PaperChatDTO> result = new LinkedList<>();
        LambdaQueryWrapper<PaperChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaperChat::getPaperId, paperId).orderByAsc(PaperChat::getChatTime).last("limit " + limit);
        List<PaperChat> paperChats = paperChatDao.selectList(wrapper);
        for (PaperChat chat : paperChats) {
            PaperChatDTO dto = new PaperChatDTO();
            BeanUtil.copyProperties(chat, dto);
            User user = userDao.selectById(chat.getChatterId());
            dto.setChatterName(user==null?"<已删除用户>":user.getName());
            //封装文档数组
            dto.setDocs(JSONArray.parseArray(chat.getData(), PaperDocDTO.class));
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<KbChatDTO> queryKbChatRecords(Integer kbId, int limit) {
        List<KbChatDTO> result = new LinkedList<>();
        LambdaQueryWrapper<KnowledgeBaseChat> chatWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Paper> paperWrapper = new LambdaQueryWrapper<>();
        chatWrapper.eq(KnowledgeBaseChat::getKnowledgeBaseId, kbId).orderByAsc(KnowledgeBaseChat::getChatTime).last("limit " + limit);
        List<KnowledgeBaseChat> paperChats = knowledgeBaseChatDao.selectList(chatWrapper);
        for (KnowledgeBaseChat chat : paperChats) {
            KbChatDTO dto = new KbChatDTO();
            BeanUtil.copyProperties(chat, dto);
            User user = userDao.selectById(chat.getChatterId());
            dto.setChatterName(user==null?"<已删除用户>":user.getName());
            //封装文档数组
            dto.setDocs(JSONArray.parseArray(chat.getData(), KbDocDTO.class));
            for (KbDocDTO doc : dto.getDocs()) {
                Paper paper = paperDao.selectOne(paperWrapper.eq(Paper::getIndexUUID, doc.getFileName().split("[.]")[0]));
                if (paper != null) {
                    doc.setFileName(paper.getName());
                    doc.setId(paper.getId());
                } else {
                    doc.setFileName("已删除文件");
                    doc.setId(-1);
                }
                paperWrapper.clear();
            }
            result.add(dto);
        }
        return result;
    }

    @Override
    public boolean deleteChatByKb(Integer kbId) {
        LambdaQueryWrapper<KnowledgeBaseChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseChat::getKnowledgeBaseId, kbId);
        return knowledgeBaseChatDao.delete(wrapper) >= 0;
    }

    @Override
    public boolean deleteChatByPaper(Integer paperId) {
        LambdaQueryWrapper<PaperChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaperChat::getPaperId, paperId);
        return paperChatDao.delete(wrapper) >= 0;
    }

    @Override
    public boolean deleteChatByKb(Integer kbId, Integer userId) {
        LambdaQueryWrapper<KnowledgeBaseChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseChat::getKnowledgeBaseId, kbId).eq(KnowledgeBaseChat::getChatterId, userId);
        return knowledgeBaseChatDao.delete(wrapper) >= 0;
    }

    @Override
    public boolean deleteChatByPaper(Integer paperId, Integer userId) {
        LambdaQueryWrapper<PaperChat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaperChat::getPaperId, paperId).eq(PaperChat::getChatterId, userId);
        return paperChatDao.delete(wrapper) >= 0;
    }
}
