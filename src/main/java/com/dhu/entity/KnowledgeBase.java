package com.dhu.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@TableName("tb_knowledge_base")
public class KnowledgeBase {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    @TableField("info")
    private String information;
    @TableField("index_uuid")
    private String indexUUID;
    @TableField("team_id")
    private Integer teamId;
    @TableField("builder_id")
    private Integer builderId;
    @TableField("is_team_kb")
    private Boolean belongsToTeam;
    @TableField("build_time")
    private LocalDateTime buildTime;
    @TableLogic
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

    @JsonIgnore
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

    @JsonIgnore
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
