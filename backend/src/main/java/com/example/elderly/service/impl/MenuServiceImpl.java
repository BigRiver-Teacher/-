package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderly.entity.Menu;
import com.example.elderly.entity.RoleMenu;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.MenuMapper;
import com.example.elderly.mapper.RoleMenuMapper;
import com.example.elderly.mapper.UserMapper;
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
    @Resource
    public UserMapper userMapper;
    @Resource
    public RoleMenuMapper roleMenuMapper;

    @Override
    public List<Menu> getDynamicMenuByUserId(Long userId) {
        // 1. 查用户角色
        User user = userMapper.selectById(userId);
        String role = user == null ? "admin" : user.getRole();
        // staff 为管理员端工作人员，复用管理员菜单
        if ("staff".equals(role)) {
            role = "admin";
        }
        // 2. 查该角色可访问的菜单ID
        LambdaQueryWrapper<RoleMenu> rmWrapper = new LambdaQueryWrapper<>();
        rmWrapper.eq(RoleMenu::getRole, role);
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(rmWrapper);
        if (roleMenus == null || roleMenus.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        // 3. 查可见菜单并按排序号排序
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getId, menuIds).eq(Menu::getVisible, true).orderByAsc(Menu::getSort);
        List<Menu> allMenu = menuMapper.selectList(wrapper);
        // 4. 构建菜单树（parent_id=0 为顶级）
        Map<Long, List<Menu>> parentMap = allMenu.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));
        List<Menu> rootMenus = parentMap.getOrDefault(0L, new ArrayList<>());
        rootMenus.forEach(root -> buildTree(root, parentMap));
        return rootMenus;
    }

    private void buildTree(Menu parent, Map<Long, List<Menu>> parentMap) {
        List<Menu> children = parentMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            children.forEach(child -> buildTree(child, parentMap));
        }
    }
}
