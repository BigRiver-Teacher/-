package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.entity.Menu;
import com.example.elderly.mapper.MenuMapper;
import com.example.elderly.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {
    @Resource
    public MenuMapper menuMapper;

    @Override
    public List<Menu> getDynamicMenuByUserId(Long userId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getVisible,true).orderByAsc(Menu::getSort);
        List<Menu> allMenu = menuMapper.selectList(wrapper);
        Map<Long, List<Menu>> parentMap = allMenu.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));
        List<Menu> rootMenus = parentMap.getOrDefault(0L, new ArrayList<>());
        rootMenus.forEach(root->buildTree(root,parentMap));
        return rootMenus;
    }

    private void buildTree(Menu parent, Map<Long, List<Menu>> parentMap) {
        List<Menu> children = parentMap.get(parent.getId());
        if(children!=null && !children.isEmpty()) {
            parent.setChildren(children);
            children.forEach(child->buildTree(child,parentMap));
        }
    }
}
