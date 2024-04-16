package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dhu.dao.*;
import com.dhu.dto.PaperRecordDTO;
import com.dhu.dto.ScheduleAddFormDTO;
import com.dhu.dto.ScheduleDTO;
import com.dhu.entity.KnowledgeBase;
import com.dhu.entity.Paper;
import com.dhu.entity.Schedule;
import com.dhu.entity.SchedulePaperRelation;
import com.dhu.exception.BlankObjectException;
import com.dhu.service.SchedulePaperRelationService;
import com.dhu.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleDao scheduleDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private KnowledgeBaseDao knowledgeBaseDao;
    @Autowired
    private SchedulePaperRelationDao schedulePaperRelationDao;
    @Autowired
    private SchedulePaperRelationService schedulePaperRelationService;

    @Override
    public ScheduleDTO querySingle(Integer scheduleId) {
        Schedule schedule = scheduleDao.selectById(scheduleId);
        ScheduleDTO dto = new ScheduleDTO();
        BeanUtil.copyProperties(schedule, dto);
        dto.setBuilderName(userDao.selectById(schedule.getBuilderId()).getName());
        dto.setProgress(schedulePaperRelationService.getProgress(scheduleId));
        return dto;
    }

    @Override
    public IPage<ScheduleDTO> querySchedule(int current, int size, Integer userId, String search, Boolean isFinished) {
        IPage<Schedule> page = new Page<>(current, size);
        IPage<ScheduleDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<Schedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Schedule::getBuilderId, userId).like(StringUtils.hasText(search), Schedule::getName, search).eq(isFinished != null, Schedule::getFinished, isFinished);
        scheduleDao.selectPage(page, queryWrapper);
        List<Schedule> list = page.getRecords();
        List<ScheduleDTO> dtoList = new ArrayList<>();
        for (Schedule schedule : list) {
            ScheduleDTO dto = new ScheduleDTO();
            BeanUtil.copyProperties(schedule, dto);
            dto.setBuilderName(userDao.selectById(schedule.getBuilderId()).getName());
            dto.setProgress(schedulePaperRelationService.getProgress(schedule.getId()));
            dtoList.add(dto);
        }
        dtoPage.setPages(page.getPages());
        dtoPage.setTotal(page.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public IPage<PaperRecordDTO> queryPaperBySchedule(int current, int size, Integer scheduleId, Boolean isFinished) {
        IPage<SchedulePaperRelation> relPage = new Page<>(current, size);
        IPage<PaperRecordDTO> dtoPage = new Page<>(current, size);
        LambdaQueryWrapper<SchedulePaperRelation> relWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Paper> paperWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<KnowledgeBase> kbWrapper = new LambdaQueryWrapper<>();
        Map<Integer, Paper> paperIndex = new HashMap<>();
        //查找关系表
        relWrapper.eq(SchedulePaperRelation::getScheduleId, scheduleId).eq(isFinished != null, SchedulePaperRelation::getFinished, isFinished).orderByAsc(SchedulePaperRelation::getFinished);
        schedulePaperRelationDao.selectPage(relPage, relWrapper);
        List<SchedulePaperRelation> relList = relPage.getRecords();
        if (!relList.isEmpty()) {        //查找论文
            paperWrapper.in(Paper::getId, relList.stream().map(SchedulePaperRelation::getPaperId).toList());
            List<Paper> papers = paperDao.selectList(paperWrapper);
            for (Paper paper : papers) {
                paperIndex.put(paper.getId(), paper);
            }
            //封装dto
            List<PaperRecordDTO> dtoList = new ArrayList<>();
            for (SchedulePaperRelation relation : relList) {
                PaperRecordDTO dto = new PaperRecordDTO();
                Paper paper = paperIndex.get(relation.getPaperId());
                BeanUtil.copyProperties(paper, dto);
                dto.setBuilderName(userDao.selectById(paper.getBuilderId()).getName());
                dto.setKnowledgeBaseName(knowledgeBaseDao.selectById(paper.getKnowledgeBaseId()).getName());
                dto.setFinished(relation.getFinished());
                dto.setFinTime(relation.getFinTime());
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
    public boolean insertSchedule(ScheduleAddFormDTO scheduleAddFormDTO) {
        Schedule schedule = new Schedule();
        BeanUtil.copyProperties(scheduleAddFormDTO, schedule);
        schedule.setBuildTime(LocalDateTime.now());
        schedule.setFinished(false);
        return scheduleDao.insert(schedule) > 0;
    }

    @Override
    public boolean deleteSchedule(Integer scheduleId) {
        return schedulePaperRelationService.deletePaperRelationsByScheduleId(scheduleId) && scheduleDao.deleteById(scheduleId) > 0;
    }

    @Override
    public boolean deleteSchedules(List<Integer> ids) {
        for (Integer id : ids) {
            if(schedulePaperRelationService.deletePaperRelationsByScheduleId(id)){
                throw new BlankObjectException("删除失败");
            }
        }
        return scheduleDao.deleteBatchIds(ids) > 0;
    }

    @Override
    public boolean updateSchedule(Schedule schedule) {
        return scheduleDao.updateById(schedule) > 0;
    }
}
