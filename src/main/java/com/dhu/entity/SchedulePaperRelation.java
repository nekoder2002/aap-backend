package com.dhu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("rel_schedule_paper")
public class SchedulePaperRelation {
    @TableField("paper_id")
    private Integer paperId;
    @TableField("schedule_id")
    private Integer scheduleId;
    @TableField("start_time")
    private LocalDateTime startTime;
    @TableField("fin_time")
    private LocalDateTime finTime;
    private Boolean finished;

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getFinTime() {
        return finTime;
    }

    public void setFinTime(LocalDateTime finTime) {
        this.finTime = finTime;
    }
}
