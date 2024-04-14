package com.dhu.dto;

import com.baomidou.mybatisplus.annotation.TableField;

import java.time.LocalDateTime;

public class ScheduleAddFormDTO {
    private String name;
    private String description;
    private Integer builderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime realTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBuilderId() {
        return builderId;
    }

    public void setBuilderId(Integer builderId) {
        this.builderId = builderId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getRealTime() {
        return realTime;
    }

    public void setRealTime(LocalDateTime realTime) {
        this.realTime = realTime;
    }
}