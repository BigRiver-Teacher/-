package com.example.elderly.controller;

import com.example.elderly.common.Result;
import com.example.elderly.entity.Menu;
import com.example.elderly.service.MenuService;
import com.example.elderly.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/menus")
@Tag(name = "菜单管理")
@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public Result<List<Menu>> getDynamicMenu(){
        Long loginUserId = SecurityUtil.getLoginUserId();
        List<Menu> menuTree = menuService.getDynamicMenuByUserId(loginUserId);
        return Result.success(menuTree);

    }
}
