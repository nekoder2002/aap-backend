package com.dhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dhu.constants.RedisConstants;
import com.dhu.constants.RightConstants;
import com.dhu.dao.TeamDao;
import com.dhu.dao.UserDao;
import com.dhu.dao.UserTeamRelationDao;
import com.dhu.entity.Team;
import com.dhu.entity.UserTeamRelation;
import com.dhu.exception.IllegalObjectException;
import com.dhu.exception.NotExistException;
import com.dhu.service.LogService;
import com.dhu.service.UserTeamRelationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserTeamRelationServiceImpl implements UserTeamRelationService {
    @Autowired
    private UserTeamRelationDao userTeamRelationDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private TeamDao teamDao;
    @Autowired
    private LogService logService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean joinInTeam(Integer userId, String invitationCode) {
        //验证用户是否存在
        if (userDao.selectById(userId) == null) {
            return false;
        }
        //检验邀请码是否正确
        String checkTeam = stringRedisTemplate.opsForValue().get(RedisConstants.JOIN_TEAM_KEY + invitationCode);
        if (checkTeam == null) {
            throw new NotExistException("邀请码不存在");
        }
        //验证团队是否存在
        int teamId = Integer.parseInt(checkTeam);
        if (teamDao.selectById(teamId) == null) {
            throw new NotExistException("加入的团队已解散");
        }
        //验证是否已存在关系
        if (userTeamRelationDao.selectOne(new QueryWrapper<UserTeamRelation>().eq("team_id", teamId).eq("user_id", userId)) != null) {
            throw new IllegalObjectException("已经加入过该团队");
        }
        //业务添加
        UserTeamRelation relation = new UserTeamRelation();
        relation.setUserId(userId);
        relation.setTeamId(teamId);
        relation.setUserRight(RightConstants.MEMBER);
        relation.setJoinTime(LocalDateTime.now());
        userTeamRelationDao.insert(relation);
        Team team = teamDao.selectById(teamId);
        logService.log("加入团队" + team.getName() + ">", team.getId());
        return true;
    }

    @Override
    public boolean deleteUserInTeam(Integer teamId, Integer userId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, userId).eq(UserTeamRelation::getTeamId, teamId);
        Team team = teamDao.selectById(teamId);
        logService.log(userDao.selectById(userId).getName() + "退出团队<" + team.getName() + ">", userId, team.getId());
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean deleteUsersByTeam(List<Integer> userIds, Integer teamId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserTeamRelation::getUserId, userIds).eq(UserTeamRelation::getTeamId, teamId);
        Team team = teamDao.selectById(teamId);
        for (Integer userId : userIds) {
            logService.log(userDao.selectById(userId).getName() + "退出团队<" + team.getName() + ">", userId, team.getId());
        }
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean deleteByUserId(Integer userId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, userId);
        return userTeamRelationDao.delete(wrapper) >= 0;
    }

    @Override
    public boolean deleteUsersInTeam(Integer teamId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getTeamId, teamId);
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean setUserRight(UserTeamRelation relation) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, relation.getUserId()).eq(UserTeamRelation::getTeamId, relation.getTeamId());
        Team team = teamDao.selectById(relation.getTeamId());
        if (relation.getUserRight() == RightConstants.ADMIN) {
            logService.log(userDao.selectById(relation.getUserId()).getName() + "设置为团队<" + team.getName() + ">管理员", relation.getUserId(), team.getId());
        } else {
            logService.log(userDao.selectById(relation.getUserId()).getName() + "设置为团队<" + team.getName() + ">成员", relation.getUserId(), team.getId());
        }
        return userTeamRelationDao.update(relation, wrapper) > 0;
    }
}
