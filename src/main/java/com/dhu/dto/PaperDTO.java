package com.dhu.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PaperDTO {
    private Integer id;
    private String name;
    private List<EchartDTO> freqList;
    private String indexUUID;
    private String builderName;
    private LocalDateTime buildTime;
    private Integer builderId;
    private Integer knowledgeBaseId;

    private Integer visit;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndexUUID() {
        return indexUUID;
    }

    public void setIndexUUID(String indexUUID) {
        this.indexUUID = indexUUID;
    }

    public String getBuilderName() {
        return builderName;
    }

    public void setBuilderName(String builderName) {
        this.builderName = builderName;
    }

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(LocalDateTime buildTime) {
        this.buildTime = buildTime;
    }

    public Integer getBuilderId() {
        return builderId;
    }

    public void setBuilderId(Integer builderId) {
        this.builderId = builderId;
    }

    public Integer getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public void setKnowledgeBaseId(Integer knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public List<EchartDTO> getFreqList() {
        return freqList;
    }

    public void setFreqList(List<EchartDTO> freqList) {
        this.freqList = freqList;
    }

    public Integer getVisit() {
        return visit;
    }

    public void setVisit(Integer visit) {
        this.visit = visit;
    }
}
