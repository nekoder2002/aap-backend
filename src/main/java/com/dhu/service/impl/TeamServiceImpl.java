package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.RedisConstants;
import com.dhu.constants.RightConstants;
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
import com.dhu.service.LogService;
import com.dhu.service.TeamService;
import com.dhu.service.UserTeamRelationService;
import com.dhu.utils.BaseUtils;
import com.dhu.utils.UserHolder;
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
    @Autowired
    private LogService logService;

    @Override
    public TeamDTO querySingle(Integer teamId, Integer userId) {
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        Team team = teamDao.selectById(teamId);
        TeamDTO dto = new TeamDTO();
        BeanUtil.copyProperties(team, dto);
        relWrapper.eq(UserTeamRelation::getTeamId, teamId).eq(UserTeamRelation::getUserRight, RightConstants.ADMIN);
        UserTeamRelation relation = userTeamRelationDao.selectOne(relWrapper);
        User user = userDao.selectById(relation.getUserId());
        dto.setAdminId(user == null ? -1 : user.getId());
        dto.setAdminName(user == null ? "已删除用户" : user.getName());
        if (UserHolder.getUser().getAdmin()){
            dto.setUserRight(RightConstants.ADMIN);
        }else{
            relWrapper.clear();
            relWrapper.eq(UserTeamRelation::getUserId, userId).eq(UserTeamRelation::getTeamId, teamId);
            relation = userTeamRelationDao.selectOne(relWrapper);
            if (relation == null) {
                dto.setUserRight(RightConstants.NOT_RIGHT);
            } else {
                dto.setUserRight(relation.getUserRight());
            }
        }
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
        relWrapper.eq(UserTeamRelation::getUserId, userId).ne(isAdmin, UserTeamRelation::getUserRight, RightConstants.MEMBER).orderByDesc(UserTeamRelation::getJoinTime);
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
            relWrapper.eq(UserTeamRelation::getUserRight, RightConstants.ADMIN).in(UserTeamRelation::getTeamId, relList.stream().map(UserTeamRelation::getTeamId).toList());
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
                dto.setAdminId(user == null ? -1 : user.getId());
                dto.setAdminName(user == null ? "已删除用户" : user.getName());
                dto.setUserRight(relation.getUserRight());
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
        relWrapper.eq(UserTeamRelation::getTeamId, teamId).ne(isAdmin, UserTeamRelation::getUserRight, RightConstants.MEMBER).orderByAsc(UserTeamRelation::getUserRight);
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
                dto.setUserRight(relation.getUserRight());
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
        team.setValid(false);
        if (teamDao.insert(team) <= 0) {
            throw new OperationException("添加团队失败");
        }
        //加入管理员
        UserTeamRelation relation = new UserTeamRelation();
        relation.setUserRight(RightConstants.ADMIN);
        relation.setTeamId(team.getId());
        relation.setUserId(teamAddForm.getAdminId());
        relation.setJoinTime(LocalDateTime.now());
        logService.log("创建团队<" + team.getName() + ">,等待管理员审核", team.getId());
        return userTeamRelationDao.insert(relation) > 0;
    }

    @Override
    public boolean deleteTeam(Integer teamId) {
        Team team = teamDao.selectById(teamId);
        //删除所有成员和知识库
        boolean flag = userTeamRelationService.deleteUsersInTeam(teamId) && deleteKnowledgeByTeam(teamId);
        logService.log("删除团队<" + team.getName() + ">", team.getId());
        return flag && teamDao.deleteById(teamId) > 0;
    }

    @Override
    public boolean deleteTeams(List<Integer> teamIds) {
        for (Integer teamId : teamIds) {
            deleteTeam(teamId);
        }
        return true;
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
        logService.log("更新团队<" + team.getName() + ">", team.getId());
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
    public IPage<TeamDTO> getTeamPage(int current, int size, String name, String email, boolean isValid) {
        IPage<Team> page = new Page<>(current, size);
        IPage<TeamDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserTeamRelation> relWrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Team::getName, name);
        }
        User user=userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user != null) {
            relWrapper.eq(UserTeamRelation::getUserId, user.getId()).eq(UserTeamRelation::getUserRight, RightConstants.ADMIN);
            List<Integer> teamIds = userTeamRelationDao.selectList(relWrapper).stream().map(UserTeamRelation::getTeamId).toList();
            if (!teamIds.isEmpty()) {
                wrapper.in(Team::getId, teamIds);
            }else{
                dtoPage.setRecords(List.of());
                dtoPage.setPages(0);
                dtoPage.setTotal(0);
                return dtoPage;
            }
        }
        wrapper.eq(Team::isValid, isValid);
        teamDao.selectPage(page, wrapper);
        List<Team> teams = page.getRecords();
        List<TeamDTO> dtoList = new ArrayList<>();
        for (Team team : teams) {
            TeamDTO dto = new TeamDTO();
            BeanUtil.copyProperties(team, dto);
            //获取user
            relWrapper.clear();
            relWrapper.eq(UserTeamRelation::getTeamId, team.getId()).eq(UserTeamRelation::getUserRight, RightConstants.ADMIN);
            UserTeamRelation relation = userTeamRelationDao.selectOne(relWrapper);
            User tempUser = userDao.selectById(relation.getUserId());
            dto.setUserRight(RightConstants.ADMIN);
            dto.setAdminId(tempUser == null ? -1 : tempUser.getId());
            dto.setAdminName(tempUser == null ? "已删除用户" : tempUser.getName());
            dtoList.add(dto);
        }
        dtoPage.setRecords(dtoList);
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        return dtoPage;
    }

    @Override
    public long countTeam(Integer userId, boolean isAdmin) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getUserId, userId).ne(isAdmin, UserTeamRelation::getUserRight, RightConstants.MEMBER);
        return userTeamRelationDao.selectCount(wrapper);
    }

    @Override
    public boolean updateTeamAdmin(Team team) {
        LambdaQueryWrapper<UserTeamRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTeamRelation::getTeamId, team.getId()).eq(UserTeamRelation::getUserRight, RightConstants.ADMIN);
        UserTeamRelation relation = userTeamRelationDao.selectOne(wrapper);
        User user = userDao.selectById(relation.getUserId());
        logService.log("管理员审核通过团队<" + team.getName() + ">", user == null ? -1 : user.getId(), team.getId());
        return teamDao.updateById(team) > 0;
    }

    @Override
    public long countTeam() {
        return teamDao.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    public long countCheckTeam() {
        return teamDao.selectCount(new LambdaQueryWrapper<Team>().eq(Team::isValid, false));
    }
}
