package com.example.elderly.service;

import com.example.elderly.entity.Menu;

import java.util.List;

public interface  MenuService {

    List<Menu> getDynamicMenuByUserId(Long userId);
}
