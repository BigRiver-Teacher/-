package com.example.elderly.service;

import com.example.elderly.vo.SearchVO;

import java.util.List;

public interface SearchService {
    List<SearchVO> globalSearch(String keyword);
}
