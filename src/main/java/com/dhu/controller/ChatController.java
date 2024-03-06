package com.dhu.controller;


import com.dhu.constants.BaseConstants;
import com.dhu.dto.KbChatDTO;
import com.dhu.dto.PaperChatDTO;
import com.dhu.service.ChatService;
import com.dhu.utils.BaseUtils;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;


    // 单论文对话
    @PostMapping("/paper")
    Result chatWithPaper(@RequestBody PaperChatDTO paperChat) {
        return Result.verifySave(chatService.chatWithPaper(paperChat));
    }

    // 多论文对话
    @PostMapping("/kb")
    Result chatWitHkb(@RequestBody KbChatDTO kbChat) {
        System.out.println(BaseUtils.getEncoding(kbChat.getQuestion()));
        System.out.println(kbChat.getQuestion());
        return Result.verifySave(chatService.chatWithKb(kbChat));
    }

    //查询论文近期对话
    @GetMapping("/paper_list")
    Result queryPaperChatRecords(@RequestParam("paper_id") Integer paperId) {
        return Result.nullFilterData("chats", chatService.queryPaperChatRecords(paperId, UserHolder.getUser().getId(), BaseConstants.CHAT_NUM));
    }

    //查询知识库近期对话记录
    @GetMapping("/kb_list")
    Result queryKbChatRecords(@RequestParam("kb_id") Integer kbId) {
        return Result.nullFilterData("chats", chatService.queryKbChatRecords(kbId, UserHolder.getUser().getId(), BaseConstants.CHAT_NUM));
    }

    //删除知识库所有对话记录
    @DeleteMapping("/delete_kb")
    Result deleteChatByKb(@RequestParam("kb_id") Integer kbId, @RequestParam("user_id") Integer userId) {
        return Result.verifyDelete(chatService.deleteChatByKb(kbId, userId));
    }

    //删除论文所有对话记录
    @DeleteMapping("/delete_paper")
    Result deleteChatByPaper(@RequestParam("paper_id") Integer paperId, @RequestParam("user_id") Integer userId) {
        return Result.verifyDelete(chatService.deleteChatByPaper(paperId, userId));
    }
}
