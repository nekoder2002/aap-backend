package com.dhu.entity;

import java.time.LocalDateTime;

public class Team {
    private Integer id;
    private String name;
    private String information;
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

    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(LocalDateTime buildTime) {
        this.buildTime = buildTime;
    }

    public boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
