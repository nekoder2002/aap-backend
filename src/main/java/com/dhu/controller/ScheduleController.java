package com.dhu.controller;

import com.dhu.dto.ScheduleAddFormDTO;
import com.dhu.entity.Schedule;
import com.dhu.entity.SchedulePaperRelation;
import com.dhu.service.SchedulePaperRelationService;
import com.dhu.service.ScheduleService;
import com.dhu.utils.BaseUtils;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private SchedulePaperRelationService schedulePaperRelationService;

    //新建计划
    @PutMapping("/insert")
    public Result insert(@RequestBody ScheduleAddFormDTO scheduleAddFormDTO) {
        if (scheduleAddFormDTO.getBuilderId() == null || !StringUtils.hasText(scheduleAddFormDTO.getName()) || !StringUtils.hasText(scheduleAddFormDTO.getDescription())) {
            return Result.saveErr().setMsg("参数错误");
        }
        return Result.verifySave(scheduleService.insertSchedule(scheduleAddFormDTO));
    }

    //查询单个计划信息
    @GetMapping("/{schedule_id}")
    public Result querySingle(@PathVariable("schedule_id") Integer scheduleId) {
        return Result.nullFilterData("schedule", scheduleService.querySingle(scheduleId));
    }

    //查询个人的计划信息
    @GetMapping("/query")
    public Result querySchedule(@RequestParam int current, @RequestParam int size, @RequestParam String search, @RequestParam("finished") String isFinished) {
        return Result.nullFilterData("schedules", scheduleService.querySchedule(current, size,UserHolder.getUser().getId(), search, BaseUtils.toBoolean(isFinished)));
    }

    //查询计划中论文列表
    @GetMapping("/papers")
    public Result queryPaperBySchedule(@RequestParam int current, @RequestParam int size, @RequestParam("schedule_id") Integer scheduleId, @RequestParam("finished") String isFinished) {
        return Result.nullFilterData("papers", scheduleService.queryPaperBySchedule(current, size, scheduleId, BaseUtils.toBoolean(isFinished)));
    }

    //删除计划
    @DeleteMapping("/delete")
    public Result deleteSchedule(@RequestParam("schedule_id") Integer scheduleId) {
        return Result.verifyDelete(scheduleService.deleteSchedule(scheduleId));
    }

    //批量删除
    @DeleteMapping("/multdel")
    public Result deleteSchedules(@RequestBody List<Integer> ids) {
        return Result.verifyDelete(scheduleService.deleteSchedules(ids));
    }

    //修改计划信息
    @PostMapping("/update")
    public Result updateSchedule(@RequestBody Schedule schedule) {
        return Result.verifyUpdate(scheduleService.updateSchedule(schedule));
    }

    //添加论文到计划
    @PutMapping("/add_paper")
    public Result addPaperRelation(@RequestBody SchedulePaperRelation schedulePaperRelation) {
        return Result.verifySave(schedulePaperRelationService.addPaperRelation(schedulePaperRelation));
    }

    //批量添加论文
    @PutMapping("/add_papers")
    public Result addPaperRelations(@RequestBody List<SchedulePaperRelation> schedulePaperRelationList) {
        return Result.verifySave(schedulePaperRelationService.addPaperRelations(schedulePaperRelationList));
    }

    //删除论文
    @DeleteMapping("/delete_paper")
    public Result deletePaperRelation(@RequestParam("schedule_id") Integer scheduleId,@RequestParam("paper_id") Integer paperId) {
        return Result.verifyDelete(schedulePaperRelationService.deletePaperRelation(scheduleId, paperId));
    }

    //批量删除论文
    @DeleteMapping("/delete_papers")
    public Result deletePaperRelations(@RequestParam("schedule_id") Integer scheduleId, @RequestBody List<Integer> paperIds) {
        return Result.verifyDelete(schedulePaperRelationService.deletePaperRelations(scheduleId, paperIds));
    }

    @PostMapping("/rel_update")
    //设置论文状态
    public Result setPaperStatus(@RequestBody SchedulePaperRelation schedulePaperRelation) {
        return Result.verifyUpdate(schedulePaperRelationService.setPaperStatus(schedulePaperRelation));
    }
}
