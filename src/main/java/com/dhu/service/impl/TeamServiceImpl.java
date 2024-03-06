package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.RedisConstants;
import com.dhu.dao.KnowledgeBaseDao;
import com.dhu.dao.TeamDao;
import com.dhu.dao.UserDao;
import com.dhu.dao.UserTeamRelationDao;
import com.dhu.dto.MemberDTO;
import com.dhu.dto.TeamAddFormDTO;
import com.dhu.dto.TeamDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.entity.Team;
import com.dhu.entity.User;
import com.dhu.entity.UserTeamRelation;
import com.dhu.exception.OperationException;
import com.dhu.service.KnowledgeBaseService;
import com.dhu.service.TeamService;
import com.dhu.service.UserTeamRelationService;
import com.dhu.utils.BaseUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {
    @Autowired
    private TeamDao teamDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Autowired
    private UserTeamRelationDao userTeamRelationDao;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private UserTeamRelationService userTeamRelationService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public TeamDTO querySingle(Integer teamId) {
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        Team team = teamDao.selectById(teamId);
        TeamDTO dto = new TeamDTO();
        BeanUtil.copyProperties(team, dto);
        relWrapper.eq(UserTeamRelation::getTeamId, teamId).eq(UserTeamRelation::getAdmin, true);
        UserTeamRelation relation = userTeamRelationDao.selectOne(relWrapper);
        User user = userDao.selectById(relation.getUserId());
        dto.setAdminId(user.getId());
        dto.setAdminName(user.getName());
        return dto;
    }

    @Override
    public IPage<TeamDTO> queryTeams(int current, int size, Integer userId, boolean isAdmin) {
        IPage<UserTeamRelation> relPage = new Page<>(current, size);
        IPage<TeamDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Team> teamWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        Map<Integer, User> userIndex = new HashMap<>();
        Map<Integer, Team> teamIndex = new HashMap<>();
        Map<Integer, User> utMap = new HashMap<>();
        //查找关系表
        relWrapper.eq(UserTeamRelation::getUserId, userId).eq(isAdmin, UserTeamRelation::getAdmin, isAdmin).orderByDesc(UserTeamRelation::getJoinTime);
        userTeamRelationDao.selectPage(relPage, relWrapper);
        List<UserTeamRelation> relList = relPage.getRecords();
        //查找团队
        if (!relList.isEmpty()) {
            teamWrapper.in(Team::getId, relList.stream().map(UserTeamRelation::getTeamId).toList());
            List<Team> teams = teamDao.selectList(teamWrapper);
            for (Team team : teams) {
                teamIndex.put(team.getId(), team);
            }
            //查找管理员
            relWrapper.clear();
            relWrapper.eq(UserTeamRelation::getAdmin, true).in(UserTeamRelation::getTeamId, relList.stream().map(UserTeamRelation::getTeamId).toList());
            List<UserTeamRelation> adminList = userTeamRelationDao.selectList(relWrapper);
            userWrapper.in(User::getId, adminList.stream().map(UserTeamRelation::getUserId).toList());
            List<User> users = userDao.selectList(userWrapper);
            for (User user : users) {
                userIndex.put(user.getId(), user);
            }
            for (UserTeamRelation relation : adminList) {
                utMap.put(relation.getTeamId(), userIndex.get(relation.getUserId()));
            }
            //封装dto
            List<TeamDTO> dtoList = new ArrayList<>();
            for (UserTeamRelation relation : relList) {
                TeamDTO dto = new TeamDTO();
                User user = utMap.get(relation.getTeamId());
                BeanUtil.copyProperties(teamIndex.get(relation.getTeamId()), dto);
                dto.setAdminName(user.getName());
                dto.setAdminId(user.getId());
                dtoList.add(dto);
            }
            dtoPage.setRecords(dtoList);
        } else {
            dtoPage.setRecords(List.of());
        }
        dtoPage.setPages(relPage.getPages());
        dtoPage.setTotal(relPage.getTotal());
        return dtoPage;
    }

    @Override
    public IPage<MemberDTO> queryMembers(int current, int size, Integer teamId, boolean isAdmin) {
        IPage<UserTeamRelation> relPage = new Page<>(current, size);
        IPage<MemberDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        Map<Integer, User> userIndex = new HashMap<>();
        //查找关系表
        relWrapper.eq(UserTeamRelation::getTeamId, teamId).eq(isAdmin, UserTeamRelation::getAdmin, isAdmin).orderByDesc(UserTeamRelation::getAdmin);
        userTeamRelationDao.selectPage(relPage, relWrapper);
        List<UserTeamRelation> relList = relPage.getRecords();
        if (!relList.isEmpty()) {        //查找用户
            userWrapper.in(User::getId, relList.stream().map(UserTeamRelation::getUserId).toList());
            List<User> users = userDao.selectList(userWrapper);
            for (User user : users) {
                userIndex.put(user.getId(), user);
            }
            //封装dto
            List<MemberDTO> dtoList = new ArrayList<>();
            for (UserTeamRelation relation : relList) {
                MemberDTO dto = new MemberDTO();
                User user = userIndex.get(relation.getUserId());
                BeanUtil.copyProperties(user, dto);
                dto.setAdmin(relation.getAdmin());
                dto.setTime(relation.getJoinTime());
                dtoList.add(dto);
            }
            dtoPage.setRecords(dtoList);
        } else {
            dtoPage.setRecords(List.of());
        }
        dtoPage.setPages(relPage.getPages());
        dtoPage.setTotal(relPage.getTotal());
        return dtoPage;
    }

    @Override
    public boolean insertTeam(TeamAddFormDTO teamAddForm) {
        Team team = new Team();
        BeanUtil.copyProperties(teamAddForm, team);
        team.setBuildTime(LocalDateTime.now());
        if (teamDao.insert(team) <= 0) {
            throw new OperationException("添加团队失败");
        }
        //加入管理员
        UserTeamRelation relation = new UserTeamRelation();
        relation.setAdmin(true);
        relation.setTeamId(team.getId());
        relation.setUserId(teamAddForm.getAdminId());
        relation.setJoinTime(LocalDateTime.now());
        return userTeamRelationDao.insert(relation) > 0;
    }

    @Override
    public boolean deleteTeam(Integer teamId) {
        //删除所有成员和知识库
        boolean flag = userTeamRelationService.deleteUsersInTeam(teamId) && deleteKnowledgeByTeam(teamId);
        return flag && teamDao.deleteById(teamId) > 0;
    }

    @Override
    public boolean deleteKnowledgeByTeam(Integer teamId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getTeamId, teamId);
        List<KnowledgeBase> list = knowledgeBaseDao.selectList(wrapper);
        //删除
        return knowledgeBaseService.deleteKnowledgeBases(list.stream().map(KnowledgeBase::getId).toList());
    }


    @Override
    public boolean updateTeam(Team team) {
        return teamDao.updateById(team) > 0;
    }

    @Override
    public String generateInvitationCode(Integer teamId) {
        //生成邀请码
        String code = BaseUtils.generateRandomCode(BaseConstants.TEAM_CODE_LENGTH);
        String key = RedisConstants.JOIN_TEAM_KEY + code;
        //存入redis
        stringRedisTemplate.opsForValue().set(key, Integer.toString(teamId));
        //设置有效期
        stringRedisTemplate.expire(key, Duration.ofMinutes(RedisConstants.LOGIN_USER_MIN_TTL));
        return code;
    }

    @Override
    public long countTeamKnowledgeBases(Integer teamId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getTeamId, teamId);
        return knowledgeBaseDao.selectCount(wrapper);
    }

    @Override
    public long countTeamUsers(Integer teamId, boolean isAdmin) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getTeamId, teamId).eq(isAdmin, UserTeamRelation::getAdmin, isAdmin);
        return userTeamRelationDao.selectCount(wrapper);
    }
}
