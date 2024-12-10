package com.dhu.controller;

import com.dhu.dto.LogQueryDTO;
import com.dhu.service.LogService;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/log")
public class LogController {
    @Autowired
    private LogService logService;

    @GetMapping("/list_team")
    public Result getTeamList(@RequestParam("team_id") Integer teamId) {
        return Result.nullFilterData("logs", logService.queryTeamLog(teamId));
    }

    @PostMapping("/query_team")
    public Result queryTeam(@RequestBody LogQueryDTO logQueryDTO) {
        Integer teamId = null;
        if (logQueryDTO.getContent() != null) {
            teamId = Integer.parseInt(logQueryDTO.getContent());
        }
        return Result.nullFilterData("logs", logService.queryTeamLogPage(teamId, logQueryDTO.getStart(), logQueryDTO.getEnd(), logQueryDTO.getCurrent(), logQueryDTO.getSize()));
    }

    @PostMapping("/query_user")
    public Result queryUser(@RequestBody LogQueryDTO logQueryDTO) {
        return Result.nullFilterData("logs", logService.queryUserLogPage(logQueryDTO.getContent(), logQueryDTO.getStart(), logQueryDTO.getEnd(), logQueryDTO.getCurrent(), logQueryDTO.getSize()));
    }

    @GetMapping("/list")
    public Result getList() {
        return Result.nullFilterData("logs", logService.queryUserLog(UserHolder.getUser().getId()));
    }
}
