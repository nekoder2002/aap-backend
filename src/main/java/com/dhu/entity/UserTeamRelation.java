package com.dhu.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("rel_user_team")
public class UserTeamRelation {
    private Integer userId;
    private Integer teamId;
    private boolean isAdmin;

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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
