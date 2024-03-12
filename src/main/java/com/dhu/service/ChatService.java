package com.dhu.service;

import com.dhu.dto.KbChatDTO;
import com.dhu.dto.PaperChatDTO;

import java.util.List;

public interface ChatService {
    // 单论文对话
    boolean chatWithPaper(PaperChatDTO paperChat);

    // 多论文对话
    boolean chatWithKb(KbChatDTO kbChat);

    //查询知识库近期对话
    List<PaperChatDTO> queryPaperChatRecords(Integer paperId, Integer userId, int limit);

    //查询论文近期对话记录
    List<KbChatDTO> queryKbChatRecords(Integer kbId, Integer userId, int limit);

    //删除知识库所有对话记录
    boolean deleteChatByKb(Integer kbId);

    //删除论文所有对话记录
    boolean deleteChatByPaper(Integer paperId);

    //删除知识库所有对话记录
    boolean deleteChatByKb(Integer kbId, Integer userId);

    //删除论文所有对话记录
    boolean deleteChatByPaper(Integer paperId, Integer userId);
}
