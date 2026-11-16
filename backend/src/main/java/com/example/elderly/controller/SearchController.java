package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.common.Result;
import com.example.elderly.service.SearchService;
import com.example.elderly.vo.SearchVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "全局搜索")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public Result<List<SearchVO>> globalSearch(String keyword){
        List<SearchVO> result = searchService.globalSearch(keyword);
        return Result.success(result);
    }
}
