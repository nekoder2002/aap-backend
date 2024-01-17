package com.dhu.domain;

import java.time.LocalDateTime;

public class KnowledgeBase {
    private Integer id;
    private String name;
    private String information;
    private Integer teamId;
    private  Integer builderId;
    private  Boolean isBelongsToTeam;
    private LocalDateTime buildTime;
    private boolean isDeleted;

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

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getBuilderId() {
        return builderId;
    }

    public void setBuilderId(Integer builderId) {
        this.builderId = builderId;
    }

    public Boolean getBelongsToTeam() {
        return isBelongsToTeam;
    }

    public void setBelongsToTeam(Boolean belongsToTeam) {
        isBelongsToTeam = belongsToTeam;
    }

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(LocalDateTime buildTime) {
        this.buildTime = buildTime;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
