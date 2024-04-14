package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhu.dao.ScheduleDao;
import com.dhu.dao.SchedulePaperRelationDao;
import com.dhu.dto.ScheduleDTO;
import com.dhu.entity.Schedule;
import com.dhu.entity.SchedulePaperRelation;
import com.dhu.exception.IllegalObjectException;
import com.dhu.exception.OperationException;
import com.dhu.service.SchedulePaperRelationService;
import com.dhu.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SchedulePaperRelationServiceImpl implements SchedulePaperRelationService {
    @Autowired
    private SchedulePaperRelationDao schedulePaperRelationDao;
    @Autowired
    private ScheduleDao scheduleDao;

    @Override
    public boolean addPaperRelation(SchedulePaperRelation schedulePaperRelation) {
        Schedule schedule = scheduleDao.selectById(schedulePaperRelation.getScheduleId());
        schedule.setFinished(false);
        scheduleDao.updateById(schedule);
        return schedulePaperRelationDao.insert(schedulePaperRelation) > 0;
    }

    @Override
    public boolean addPaperRelations(List<SchedulePaperRelation> schedulePaperRelationList) {
        int count = 0;
        if (schedulePaperRelationList.isEmpty()) {
            return false;
        }
        int scheduleId = schedulePaperRelationList.getFirst().getScheduleId();
        for (SchedulePaperRelation schedulePaperRelation : schedulePaperRelationList) {
            if (scheduleId == schedulePaperRelation.getScheduleId()) {
                throw new IllegalObjectException("scheduleId不一致");
            }
            schedulePaperRelationDao.insert(schedulePaperRelation);
            count++;
        }
        //更新schedule
        Schedule schedule = scheduleDao.selectById(scheduleId);
        schedule.setFinished(false);
        scheduleDao.updateById(schedule);
        return count == schedulePaperRelationList.size();
    }

    @Override
    public boolean deletePaperRelation(Integer scheduleId, Integer paperId) {
        LambdaQueryWrapper<SchedulePaperRelation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SchedulePaperRelation::getScheduleId, scheduleId).eq(SchedulePaperRelation::getPaperId, paperId);
        if (schedulePaperRelationDao.delete(lambdaQueryWrapper) == 0) {
            throw new OperationException("删除失败");
        }
        Schedule schedule = scheduleDao.selectById(scheduleId);
        schedule.setFinished(getProgress(scheduleId) == 100);
        return scheduleDao.updateById(schedule) > 0;
    }

    @Override
    public boolean deletePaperRelations(Integer scheduleId, List<Integer> paperIds) {
        LambdaQueryWrapper<SchedulePaperRelation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SchedulePaperRelation::getScheduleId, scheduleId).in(SchedulePaperRelation::getPaperId, paperIds);
        if (schedulePaperRelationDao.delete(lambdaQueryWrapper) != paperIds.size()) {
            throw new OperationException("删除失败");
        }
        Schedule schedule = scheduleDao.selectById(scheduleId);
        schedule.setFinished(getProgress(scheduleId) == 100);
        return scheduleDao.updateById(schedule) > 0;
    }

    @Override
    public boolean deletePaperRelationsByScheduleId(Integer scheduleId) {
        LambdaQueryWrapper<SchedulePaperRelation> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SchedulePaperRelation::getScheduleId, scheduleId);
        return schedulePaperRelationDao.delete(lambdaQueryWrapper) >= 0;
    }

    @Override
    public boolean setPaperStatus(SchedulePaperRelation schedulePaperRelation) {
        return schedulePaperRelationDao.updateById(schedulePaperRelation) > 0;
    }

    @Override
    public int getProgress(Integer scheduleId) {
        LambdaQueryWrapper<SchedulePaperRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SchedulePaperRelation::getScheduleId, scheduleId);
        long count = schedulePaperRelationDao.selectCount(queryWrapper);
        if (count == 0) {
            return 0;
        }
        long readedCount = schedulePaperRelationDao.selectCount(queryWrapper.eq(SchedulePaperRelation::getFinished, true));
        return (int) (readedCount * 100 / count);
    }
}
