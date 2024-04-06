package com.dhu.dto;

import java.time.LocalDateTime;

public class KbDTO {
    private Integer id;
    private String name;
    private String information;
    private String indexUUID;
    private Integer teamId;
    private Integer builderId;
    private String builderName;
    private Boolean belongsToTeam;
    private Integer userRight;
    private LocalDateTime buildTime;

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

    public String getIndexUUID() {
        return indexUUID;
    }

    public void setIndexUUID(String indexUUID) {
        this.indexUUID = indexUUID;
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

    public String getBuilderName() {
        return builderName;
    }

    public void setBuilderName(String builderName) {
        this.builderName = builderName;
    }

    public Boolean getBelongsToTeam() {
        return belongsToTeam;
    }

    public void setBelongsToTeam(Boolean belongsToTeam) {
        this.belongsToTeam = belongsToTeam;
    }

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(LocalDateTime buildTime) {
        this.buildTime = buildTime;
    }

    public Integer getUserRight() {
        return userRight;
    }

    public void setUserRight(Integer userRight) {
        this.userRight = userRight;
    }
}
