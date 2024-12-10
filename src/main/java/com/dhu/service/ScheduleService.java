package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.PaperRecordDTO;
import com.dhu.dto.ScheduleAddFormDTO;
import com.dhu.dto.ScheduleDTO;
import com.dhu.entity.Schedule;

import java.util.List;

public interface ScheduleService {
    //查询单个计划信息
    ScheduleDTO querySingle(Integer scheduleId);

    //查询个人的计划信息
    IPage<ScheduleDTO> querySchedule(int current, int size, Integer userId, String search, Boolean isFinished);

    //查询计划中论文列表
    IPage<PaperRecordDTO> queryPaperBySchedule(int current, int size, Integer scheduleId, Boolean isFinished);

    //新建计划
    boolean insertSchedule(ScheduleAddFormDTO scheduleAddFormDTO);

    //删除计划
    boolean deleteSchedule(Integer scheduleId);

    //批量删除
    boolean deleteSchedules(List<Integer> ids);

    //修改计划信息
    boolean updateSchedule(Schedule schedule);

    //统计
    long countSchedule(Integer userId);
}
