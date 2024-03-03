package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.MemberDTO;
import com.dhu.dto.TeamAddFormDTO;
import com.dhu.dto.TeamDTO;
import com.dhu.entity.Team;

public interface TeamService {
    //查询个人的团队信息
    IPage<TeamDTO> queryTeams(int current, int size, Integer userId, boolean isAdmin);

    //查询团队中的成员列表
    IPage<MemberDTO> queryMembers(int current, int size, Integer teamId, boolean isAdmin);

    //新建团队
    boolean insertTeam(TeamAddFormDTO teamAddForm);

    //删除团队
    boolean deleteTeam(Integer teamId);

    //删除团队中所有知识库
    boolean deleteKnowledgeByTeam(Integer teamId);

    //修改团队信息
    boolean updateTeam(Team team);

    //生成团队邀请码
    String generateInvitationCode(Integer teamId);

    //查询团队中知识库数量
    long countTeamKnowledgeBases(Integer teamId);

    //查询团队中人员数量
    long countTeamUsers(Integer teamId, boolean isAdmin);
}
