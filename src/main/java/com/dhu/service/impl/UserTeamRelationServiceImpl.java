package com.dhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dhu.constants.RedisConstants;
import com.dhu.dao.TeamDao;
import com.dhu.dao.UserDao;
import com.dhu.dao.UserTeamRelationDao;
import com.dhu.entity.UserTeamRelation;
import com.dhu.exception.NotExistException;
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
            return false;
        }
        //业务添加
        UserTeamRelation relation = new UserTeamRelation();
        relation.setUserId(userId);
        relation.setTeamId(teamId);
        relation.setAdmin(false);
        relation.setJoinTime(LocalDateTime.now());
        userTeamRelationDao.insert(relation);
        return true;
    }

    @Override
    public boolean deleteUserInTeam(Integer teamId, Integer userId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, userId).eq(UserTeamRelation::getTeamId, teamId);
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean deleteUsersByTeam(List<Integer> userIds, Integer teamId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserTeamRelation::getUserId, userIds).eq(UserTeamRelation::getTeamId, teamId);
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean deleteUsersInTeam(Integer teamId) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getTeamId, teamId);
        return userTeamRelationDao.delete(wrapper) > 0;
    }

    @Override
    public boolean setUserToAdmin(UserTeamRelation relation) {
        relation.setAdmin(true);
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, relation.getUserId()).eq(UserTeamRelation::getTeamId, relation.getTeamId());
        return userTeamRelationDao.update(relation, wrapper) > 0;
    }
}
