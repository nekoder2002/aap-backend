package com.dhu.service;

import com.dhu.entity.SchedulePaperRelation;

import java.util.List;

public interface SchedulePaperRelationService {
    //添加论文
    boolean addPaperRelation(SchedulePaperRelation schedulePaperRelation);
    //批量添加论文
    boolean addPaperRelations(List<SchedulePaperRelation> schedulePaperRelationList);
    //删除论文
    boolean deletePaperRelation(Integer scheduleId, Integer paperId);
    //批量删除论文
    boolean deletePaperRelations(Integer scheduleId, List<Integer> paperIds);
    //根据scheduleId删除论文
    boolean deletePaperRelationsByScheduleId(Integer scheduleId);
    //设置论文状态
    boolean setPaperStatus(SchedulePaperRelation schedulePaperRelation);
    //查询progress
    int getProgress(Integer scheduleId);

}
