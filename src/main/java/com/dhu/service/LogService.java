package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.LogDTO;
import com.dhu.entity.Log;

import java.time.LocalDateTime;
import java.util.List;

public interface LogService {
    void log(String content, Integer teamId);

    void log(String content, Integer userId, Integer teamId);

    List<Log> queryTeamLog(Integer teamId);

    List<Log> queryUserLog(Integer userId);

    IPage<LogDTO> queryTeamLogPage(Integer teamId, LocalDateTime start, LocalDateTime end, int current, int size);
    IPage<LogDTO> queryUserLogPage(String userEmail, LocalDateTime start, LocalDateTime end, int current, int size);
}
