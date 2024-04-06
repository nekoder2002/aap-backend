package com.dhu.service;

import com.dhu.entity.UserTeamRelation;

import java.util.List;

public interface UserTeamRelationService {
    //加入团队
    boolean joinInTeam(Integer userId,String invitationCode);

    //从团队中删除成员
    boolean deleteUserInTeam(Integer teamId,Integer userId);

    //批量删除一个团队中的成员
    boolean deleteUsersByTeam(List<Integer> userIds, Integer teamId);

    //删除团队所有成员
    boolean deleteUsersInTeam(Integer teamId);

    //将团队中的成员设置为管理员
    boolean setUserRight(UserTeamRelation relation);
}
