package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.Result;
import com.example.elderly.service.NotificationService;
import com.example.elderly.util.SecurityUtil;
import com.example.elderly.vo.NotificationVO;
import com.example.elderly.vo.PageVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "通知管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<PageVO<NotificationVO>> list(
            @RequestParam(defaultValue="1")Integer pageNum,
            @RequestParam(defaultValue="10")Integer pageSize
    ){
        Long userId = SecurityUtil.getLoginUserId();
        PageVO<NotificationVO> page = notificationService.listNotification(userId, pageNum, pageSize);
        return Result.success(page);
    }
    @GetMapping("/count")
    public Result<Map<String,Long>>getUnReadCount(){
        Long userId = SecurityUtil.getLoginUserId();
        Long count = notificationService.getUnReadCount(userId);
        Map<String, Long> map = new HashMap<String, Long>();
        map.put("count",count);
        return Result.success(map);
    }
    @PutMapping("/{id}/read")
    public Result<Boolean> markRead(@PathVariable Long id){
        Long userId = SecurityUtil.getLoginUserId();
        notificationService.markRead(id,userId);
        return Result.success(true);
    }
    @PutMapping("/read-all")
    public  Result<Boolean> markAllRead(){
        Long userId = SecurityUtil.getLoginUserId();
        notificationService.markAllRead(userId);
        return Result.success(true);
    }
}
