package com.example.elderly.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult <T>{
    private Long total;

    private List<T> list;

    public static<T> PageResult<T> build(Long total,List<T> list){
        return new PageResult<>(total,list);
    }
    public static<T> PageResult<T> build(IPage<T> page){
        return new PageResult<>(page.getTotal(),page.getRecords());
    }
}
