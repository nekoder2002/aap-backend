package com.dhu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@TableName("tb_paper_chat")
public class PaperChat {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String question;
    private String answer;
    @TableField("chat_time")
    private LocalDateTime chatTime;
    @TableField("chatter_id")
    private Integer chatterId;
    @TableField("paper_id")
    private Integer paperId;
    private String data;
    @TableLogic
    private boolean isDeleted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getChatTime() {
        return chatTime;
    }

    public void setChatTime(LocalDateTime chatTime) {
        this.chatTime = chatTime;
    }

    public Integer getChatterId() {
        return chatterId;
    }

    public void setChatterId(Integer chatterId) {
        this.chatterId = chatterId;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
