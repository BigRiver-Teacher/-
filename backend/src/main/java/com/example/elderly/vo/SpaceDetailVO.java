package com.example.elderly.vo;

import com.example.elderly.entity.Space;
import com.example.elderly.entity.SpaceInspection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "空间详情")
    public class SpaceDetailVO {
    private Space space;
    private List<SpaceInspection> inspectionHistory;
}
