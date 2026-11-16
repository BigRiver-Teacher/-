package com.example.elderly.vo;

import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
@Schema(description = "分页结果")
    public class PageVO<T> {
    @Schema(description = "总数")
    private Long total;
    @Schema(description = "数据列表")
    private List<T> list;

    public PageVO(){
        this.total=total;
        this.list=list;
    }
}
