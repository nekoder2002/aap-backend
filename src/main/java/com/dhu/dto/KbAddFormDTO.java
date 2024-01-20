package com.dhu.dto;

public class KbAddFormDTO {
    private String name;
    private String information;
    private Integer teamId;
    private Integer builderId;
    private Boolean belongsToTeam;

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

    public Boolean getBelongsToTeam() {
        return belongsToTeam;
    }

    public void setBelongsToTeam(Boolean belongsToTeam) {
        this.belongsToTeam = belongsToTeam;
    }

    public Integer getBuilderId() {
        return builderId;
    }

    public void setBuilderId(Integer builderId) {
        this.builderId = builderId;
    }
}
