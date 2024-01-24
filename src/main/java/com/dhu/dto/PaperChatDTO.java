package com.dhu.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PaperChatDTO {
    private Integer id;
    private String question;
    private String answer;
    private LocalDateTime chatTime;
    private Integer chatterId;
    private Integer paperId;
    private List<HistoryDTO> history;
    private List<PaperDocDTO> docs;

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

    public List<HistoryDTO> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryDTO> history) {
        this.history = history;
    }

    public List<PaperDocDTO> getDocs() {
        return docs;
    }

    public void setDocs(List<PaperDocDTO> docs) {
        this.docs = docs;
    }
}