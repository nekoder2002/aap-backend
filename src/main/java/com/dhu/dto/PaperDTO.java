package com.dhu.dto;

import java.time.LocalDateTime;

public class PaperDTO {
    private Integer id;
    private String name;
    private String indexUUID;
    private String builderName;
    private LocalDateTime buildTime;
    private Integer builderId;
    private Integer knowledgeBaseId;

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
}
