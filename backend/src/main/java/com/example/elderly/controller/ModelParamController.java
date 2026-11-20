package com.example.elderly.controller;

import lombok.RequiredArgsConstructor;
import com.example.elderly.annotation.RequireRole;
import com.example.elderly.common.Result;
import com.example.elderly.dto.model.ModelParamSaveDTO;
import com.example.elderly.service.ModelParamService;
import com.example.elderly.vo.model.ModelParamVO;
import com.example.elderly.vo.model.ModelParamVersionVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "模型参数")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/model")
public class ModelParamController {
    private final ModelParamService modelParamService;
    @GetMapping("/params")
    public Result<ModelParamVO> getParams(){
        ModelParamVO vo = modelParamService.getCurrentParams();
        return Result.success(vo);
    }
    @RequireRole({"admin", "staff"})
    @PutMapping("/params")
    public Result<Void>saveParams(@Validated @RequestBody ModelParamSaveDTO dto, HttpServletRequest request){
        String operator = request.getHeader("username");
        modelParamService.saveParams(dto,operator);
        return Result.success();
    }
    @RequireRole({"admin", "staff"})
    @PutMapping("/reset")
    public Result<Void> resetDefault(HttpServletRequest request){
        String operator = request.getHeader("username");
        modelParamService.resetDefault(operator);
        return Result.success();
    }
    @GetMapping("/history")
    public Result<List<ModelParamVersionVO>>getHistory(){
        List<ModelParamVersionVO> list= modelParamService.getVersionHistory();
          return Result.success(list);
    }
    @RequireRole({"admin", "staff"})
    @PostMapping("/rollback/{version}")
    public Result<Void>rollback(@PathVariable String version,HttpServletRequest request){
        String operator = request.getHeader("username");
        modelParamService.rollbackVersion(version,operator);
        return Result.success();
    }
}
