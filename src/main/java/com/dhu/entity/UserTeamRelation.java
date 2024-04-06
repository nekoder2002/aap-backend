package com.dhu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("rel_user_team")
public class UserTeamRelation {
    @TableField("user_id")
    private Integer userId;
    @TableField("team_id")
    private Integer teamId;
    @TableField("user_right")
    private Integer userRight;
    @TableField("join_time")
    private LocalDateTime joinTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getUserRight() {
        return userRight;
    }

    public void setUserRight(Integer userRight) {
        this.userRight = userRight;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
}
