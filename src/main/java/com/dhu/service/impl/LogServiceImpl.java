package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.constants.BaseConstants;
import com.dhu.dao.LogDao;
import com.dhu.dao.TeamDao;
import com.dhu.dao.UserDao;
import com.dhu.dto.LogDTO;
import com.dhu.dto.PaperDTO;
import com.dhu.entity.Log;
import com.dhu.entity.Paper;
import com.dhu.entity.Team;
import com.dhu.entity.User;
import com.dhu.service.LogService;
import com.dhu.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LogServiceImpl implements LogService {
    @Autowired
    private TeamDao teamDao;
    @Autowired
    private LogDao logDao;
    @Autowired
    private UserDao userDao;

    @Override
    public void log(String content, Integer teamId) {
        Log log = new Log();
        log.setTeamId(teamId);
        log.setUserId(UserHolder.getUser().getId());
        log.setContent(UserHolder.getUser().getName() + "：" + content);
        log.setTime(LocalDateTime.now());
        logDao.insert(log);
    }

    @Override
    public void log(String content, Integer userId, Integer teamId) {
        Log log = new Log();
        log.setTeamId(teamId);
        log.setUserId(userId);
        log.setContent(UserHolder.getUser().getName() + "：" + content);
        log.setTime(LocalDateTime.now());
        logDao.insert(log);
    }

    @Override
    public List<Log> queryTeamLog(Integer teamId) {
        LambdaQueryWrapper<Log> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Log::getTeamId, teamId).orderByDesc(Log::getTime).last("limit " + BaseConstants.LOG_SHOW_LIMIT);
        return logDao.selectList(queryWrapper);
    }

    @Override
    public List<Log> queryUserLog(Integer userId) {
        LambdaQueryWrapper<Log> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Log::getUserId, userId).orderByDesc(Log::getTime).last("limit " + BaseConstants.LOG_SHOW_LIMIT);
        return logDao.selectList(queryWrapper);
    }

    @Override
    public IPage<LogDTO> queryTeamLogPage(Integer teamId, LocalDateTime start, LocalDateTime end, int current, int size) {
        IPage<Log> page = new Page<>(current, size);
        IPage<LogDTO> dtoPage = new Page<>(current, size);
        List<LogDTO> dtoList = new ArrayList<>();
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(teamId != null, Log::getTeamId, teamId).ge(start != null, Log::getTime, start).le(end != null, Log::getTime, end);
        wrapper.orderByDesc(Log::getTime);
        logDao.selectPage(page, wrapper);
        List<Log> list = page.getRecords();
        for (Log log : list) {
            LogDTO dto = new LogDTO();
            BeanUtil.copyProperties(log, dto);
            User user = userDao.selectById(log.getUserId());
            if (log.getTeamId() != null) {
                Team team = teamDao.selectById(log.getTeamId());
                dto.setTeamName(team==null? "已删除团队" : team.getName());
            }else{
                dto.setTeamName("不属于团队");
            }
            dto.setUserName(user==null? "已删除用户" : user.getName());
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public IPage<LogDTO> queryUserLogPage(String userEmail, LocalDateTime start, LocalDateTime end, int current, int size) {
        IPage<Log> page = new Page<>(current, size);
        IPage<LogDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<Log> wrapper = new LambdaQueryWrapper<>();
        List<LogDTO> dtoList = new ArrayList<>();
        User tempUser = null;
        boolean flag = StringUtils.hasText(userEmail);
        if (flag) {
            tempUser = userDao.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, userEmail));
        }
        wrapper.eq(flag, Log::getUserId, tempUser == null ? null : tempUser.getId()).ge(start != null, Log::getTime, start).le(end != null, Log::getTime, end);
        wrapper.orderByDesc(Log::getTime);
        logDao.selectPage(page, wrapper);
        List<Log> list = page.getRecords();
        for (Log log : list) {
            LogDTO dto = new LogDTO();
            BeanUtil.copyProperties(log, dto);
            User user = userDao.selectById(log.getUserId());
            if (log.getTeamId() != null) {
                Team team = teamDao.selectById(log.getTeamId());
                dto.setTeamName(team==null? "已删除团队" : team.getName());
            }else{
                dto.setTeamName("不属于团队");
            }
            dto.setUserName(user==null? "已删除用户" : user.getName());
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}
