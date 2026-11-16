package com.example.elderly.service;

import com.example.elderly.vo.NotificationVO;
import com.example.elderly.vo.PageVO;

public interface NotificationService {
    PageVO<NotificationVO> listNotification(Long userId, Integer pageNum, Integer pageSize);

    Long getUnReadCount(Long userId);

    void markRead(Long id,Long userId);
     void markAllRead(Long userId);
}
