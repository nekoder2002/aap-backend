package com.dhu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.JoinTeamFormDTO;
import com.dhu.dto.MemberDTO;
import com.dhu.dto.TeamAddFormDTO;
import com.dhu.dto.TeamDTO;
import com.dhu.entity.Team;
import com.dhu.entity.UserTeamRelation;
import com.dhu.service.TeamService;
import com.dhu.service.UserTeamRelationService;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {
    @Autowired
    TeamService teamService;
    @Autowired
    UserTeamRelationService userTeamRelationService;

    //查询单个的团队信息
    @GetMapping("/{teamId}")
    Result get(@PathVariable Integer teamId){
        if (teamId==null||teamId<=0){
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("team",teamService.querySingle(teamId));
    }

    //查询个人的团队信息
    @GetMapping("/query")
    Result queryList(@RequestParam int current, @RequestParam int size, @RequestParam("is_admin") Boolean isAdmin) {
        if (current <= 0 || size <= 0 || isAdmin == null) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("teams", teamService.queryTeams(current, size, UserHolder.getUser().getId(), isAdmin));
    }

    //查询团队中的成员列表
    @GetMapping("/members")
    Result queryMembers(@RequestParam int current, @RequestParam int size, @RequestParam("team_id") Integer teamId, @RequestParam("is_admin") Boolean isAdmin) {
        if (current <= 0 || size <= 0 || teamId == null || teamId <= 0 || isAdmin == null) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("members", teamService.queryMembers(current, size, teamId, isAdmin));
    }

    //新建团队
    @PutMapping("/insert")
    Result insert(@RequestBody TeamAddFormDTO teamAddForm) {
        if (!StringUtils.hasText(teamAddForm.getName()) || !StringUtils.hasText(teamAddForm.getInformation()) || teamAddForm.getAdminId() == null) {
            return Result.saveErr().setMsg("查询参数错误");
        }
        return Result.verifySave(teamService.insertTeam(teamAddForm));
    }

    //删除团队
    @DeleteMapping("/delete")
    Result deleteTeam(@RequestParam("team_id") Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(teamService.deleteTeam(teamId));
    }

    //修改团队信息
    @PostMapping("/update")
    Result updateTeam(@RequestBody Team team) {
        team.setBuildTime(null);
        if (team.getId() == null || team.getId() <= 0) {
            return Result.updateErr().setMsg("查询参数错误");
        }
        return Result.verifyUpdate(teamService.updateTeam(team));
    }

    //生成团队邀请码
    @GetMapping("/code")
    Result generateInvitationCode(@RequestParam("team_id") Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("code", teamService.generateInvitationCode(teamId));
    }

    //查询团队中知识库数量
    Result countTeamKnowledgeBases(Integer teamId) {
        return null;
    }

    //查询团队中人员数量
    Result countTeamUsers(Integer teamId, boolean isAdmin) {
        return null;
    }

    //加入团队
    @PutMapping("/join")
    Result joinInTeam(@RequestBody JoinTeamFormDTO joinTeamForm) {
        if (joinTeamForm.getUserId() == null || joinTeamForm.getUserId() <= 0 || !StringUtils.hasText(joinTeamForm.getInvitationCode())) {
            return Result.saveErr().setMsg("查询参数错误");
        }
        return Result.verifySave(userTeamRelationService.joinInTeam(joinTeamForm.getUserId(), joinTeamForm.getInvitationCode()));
    }

    //从团队中删除成员
    @DeleteMapping("/del_member")
    Result deleteUserInTeam(@RequestParam("team_id") Integer teamId, @RequestParam("user_id") Integer userId) {
        if (teamId == null || teamId <= 0 || userId == null || userId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(userTeamRelationService.deleteUserInTeam(teamId, userId));
    }

    //批量删除一个团队中的成员
    @DeleteMapping("/multdel_member")
    Result deleteUsersByTeam(@RequestBody List<Integer> userIds, @RequestParam("team_id") Integer teamId) {
        if (userIds == null || teamId == null || teamId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(userTeamRelationService.deleteUsersByTeam(userIds, teamId));
    }

    //将团队中的成员设置为管理员
    @PostMapping("/admin")
    Result setUserToAdmin(@RequestBody UserTeamRelation relation){
        if (relation.getTeamId()==null||relation.getTeamId()<=0||relation.getUserId()==null||relation.getUserId()<=0){
            return Result.updateErr().setMsg("查询参数错误");
        }
        return Result.verifyUpdate(userTeamRelationService.setUserToAdmin(relation));
    }
}
