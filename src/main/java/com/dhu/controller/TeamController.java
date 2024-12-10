package com.dhu.controller;

import com.dhu.dto.JoinTeamFormDTO;
import com.dhu.dto.TeamAddFormDTO;
import com.dhu.dto.TeamQueryDTO;
import com.dhu.entity.Team;
import com.dhu.entity.UserTeamRelation;
import com.dhu.service.LogService;
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
    private TeamService teamService;
    @Autowired
    private UserTeamRelationService userTeamRelationService;
    @Autowired
    private LogService logService;

    //查询单个的团队信息
    @GetMapping("/{teamId}")
    public Result get(@PathVariable Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("team", teamService.querySingle(teamId, UserHolder.getUser().getId()));
    }

    //查询个人的团队信息
    @GetMapping("/query")
    public Result queryList(@RequestParam int current, @RequestParam int size, @RequestParam("is_admin") Boolean isAdmin) {
        if (current <= 0 || size <= 0 || isAdmin == null) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("teams", teamService.queryTeams(current, size, UserHolder.getUser().getId(), isAdmin));
    }

    //查询团队中的成员列表
    @GetMapping("/members")
    public Result queryMembers(@RequestParam int current, @RequestParam int size, @RequestParam("team_id") Integer teamId, @RequestParam("is_admin") Boolean isAdmin) {
        if (current <= 0 || size <= 0 || teamId == null || teamId <= 0 || isAdmin == null) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("members", teamService.queryMembers(current, size, teamId, isAdmin));
    }

    //新建团队
    @PutMapping("/insert")
    public Result insert(@RequestBody TeamAddFormDTO teamAddForm) {
        if (!StringUtils.hasText(teamAddForm.getName()) || !StringUtils.hasText(teamAddForm.getInformation()) || teamAddForm.getAdminId() == null) {
            return Result.saveErr().setMsg("查询参数错误");
        }
        return Result.verifySave(teamService.insertTeam(teamAddForm));
    }

    //删除团队
    @DeleteMapping("/delete")
    public Result deleteTeam(@RequestParam("team_id") Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(teamService.deleteTeam(teamId));
    }

    //删除团队
    @DeleteMapping("/multdel")
    public Result deleteTeams(@RequestBody List<Integer> teamIds) {
        return Result.verifyDelete(teamService.deleteTeams(teamIds));
    }

    //修改团队信息
    @PostMapping("/update")
    public Result updateTeam(@RequestBody Team team) {
        team.setBuildTime(null);
        if (team.getId() == null || team.getId() <= 0) {
            return Result.updateErr().setMsg("查询参数错误");
        }
        return Result.verifyUpdate(teamService.updateTeam(team));
    }

    //修改团队信息
    @PostMapping("/admin_update_valid")
    public Result updateTeamAdmin(@RequestBody Team team) {
        team.setBuildTime(null);
        if (team.getId() == null || team.getId() <= 0) {
            return Result.updateErr().setMsg("查询参数错误");
        }
        return Result.verifyUpdate(teamService.updateTeamAdmin(team));
    }


    //生成团队邀请码
    @GetMapping("/code")
    public Result generateInvitationCode(@RequestParam("team_id") Integer teamId) {
        if (teamId == null || teamId <= 0) {
            return Result.getErr().setMsg("查询参数错误");
        }
        return Result.nullFilterData("code", teamService.generateInvitationCode(teamId));
    }

    //加入团队
    @PutMapping("/join")
    public Result joinInTeam(@RequestBody JoinTeamFormDTO joinTeamForm) {
        if (joinTeamForm.getUserId() == null || joinTeamForm.getUserId() <= 0 || !StringUtils.hasText(joinTeamForm.getInvitationCode())) {
            return Result.saveErr().setMsg("查询参数错误");
        }
        return Result.verifySave(userTeamRelationService.joinInTeam(joinTeamForm.getUserId(), joinTeamForm.getInvitationCode()));
    }

    //从团队中删除成员
    @DeleteMapping("/del_member")
    public Result deleteUserInTeam(@RequestParam("team_id") Integer teamId, @RequestParam("user_id") Integer userId) {
        if (teamId == null || teamId <= 0 || userId == null || userId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(userTeamRelationService.deleteUserInTeam(teamId, userId));
    }

    //批量删除一个团队中的成员
    @DeleteMapping("/multdel_member")
    public Result deleteUsersByTeam(@RequestBody List<Integer> userIds, @RequestParam("team_id") Integer teamId) {
        if (userIds == null || teamId == null || teamId <= 0) {
            return Result.deleteErr().setMsg("查询参数错误");
        }
        return Result.verifyDelete(userTeamRelationService.deleteUsersByTeam(userIds, teamId));
    }

    //统计个人团队数量
    @GetMapping("/count")
    public Result countTeam(@RequestParam("admin") boolean admin) {
        return Result.nullFilterData("count", teamService.countTeam(UserHolder.getUser().getId(), admin));
    }

    //统计个人团队数量
    @GetMapping("/count_sys")
    public Result countTeam() {
        return Result.nullFilterData("count", teamService.countTeam());
    }

    //统计个人团队数量
    @GetMapping("/count_check_sys")
    public Result countCheckTeam() {
        return Result.nullFilterData("count", teamService.countCheckTeam());
    }

    //将团队中的成员设置为管理员
    @PostMapping("/right")
    public Result setUserToAdmin(@RequestBody UserTeamRelation relation) {
        if (relation.getTeamId() == null || relation.getTeamId() <= 0 || relation.getUserId() == null || relation.getUserId() <= 0) {
            return Result.updateErr().setMsg("查询参数错误");
        }
        return Result.verifyUpdate(userTeamRelationService.setUserRight(relation));
    }

    //查询
    @PostMapping("/list")
    public Result listTeam(@RequestBody TeamQueryDTO teamQueryDTO) {
        return Result.nullFilterData("teams", teamService.getTeamPage(teamQueryDTO.getCurrent(), teamQueryDTO.getSize(), teamQueryDTO.getName(), teamQueryDTO.getEmail(), teamQueryDTO.getValid()));
    }
}
