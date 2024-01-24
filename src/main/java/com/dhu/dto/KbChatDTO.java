package com.dhu.dto;

import java.time.LocalDateTime;
import java.util.List;

public class KbChatDTO {
    private Integer id;
    private String question;
    private String answer;
    private LocalDateTime chatTime;
    private Integer chatterId;
    private Integer knowledgeBaseId;
    private List<HistoryDTO> history;
    private List<KbDocDTO> docs;

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

    public Integer getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Integer knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public List<HistoryDTO> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryDTO> history) {
        this.history = history;
    }

    public List<KbDocDTO> getDocs() {
        return docs;
    }

    public void setDocs(List<KbDocDTO> docs) {
        this.docs = docs;
    }
}
