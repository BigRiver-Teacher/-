package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderly.entity.Notification;
import com.example.elderly.mapper.NotificationMapper;
import com.example.elderly.service.NotificationService;
import com.example.elderly.vo.NotificationVO;
import com.example.elderly.vo.PageVO;
import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Resource
    private NotificationMapper notificationMapper;
    @Override
    public PageVO<NotificationVO> listNotification(Long userId, Integer pageNum, Integer pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId,userId)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> dataPage = notificationMapper.selectPage(page, wrapper);
        List<NotificationVO> voList = dataPage.getRecords().stream().map(item -> {
            NotificationVO vo = new NotificationVO();
            vo.setId(item.getId());
            vo.setTitle(item.getTitle());
            vo.setContent(item.getContent());
            vo.setType(item.getType());
            vo.setTime(item.getCreateTime());
            vo.setRead(item.getRead());
            vo.setLink(item.getLink());
            return vo;
        }).collect(Collectors.toList());
        return new PageVO<>(dataPage.getTotal(),voList);
    }

    @Override
    public Long getUnReadCount(Long userId) {

        return notificationMapper.countUnRead(userId);
    }

    @Override
    public void markRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> update = new LambdaUpdateWrapper<>();
        update.eq(Notification::getId,id)
                .eq(Notification::getUserId,userId)
                .set(Notification::getRead,true);
        notificationMapper.update(null,update);
    }

    @Override
    public void markAllRead(Long userId) {
        LambdaUpdateWrapper<Notification> update = new LambdaUpdateWrapper<>();
        update.eq(Notification::getUserId,userId)
                .set(Notification::getRead,true);
        notificationMapper.update(null,update);
    }
}
