package com.example.elderly.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.elderly.common.PageResult;
import com.example.elderly.common.Result;
import com.example.elderly.dto.fall.FallRecordAddDTO;
import com.example.elderly.dto.fall.FallRecordQueryDTO;
import com.example.elderly.dto.fall.FallRecordUpdateDTO;
import com.example.elderly.entity.FallRecord;
import com.example.elderly.vo.FallVO;
import java.util.stream.Collectors;
import com.example.elderly.service.IFallRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


@Tag(name = "跌倒记录")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fall")
public class FallRecordController {
    private final IFallRecordService iFallRecordService;
    @GetMapping("/list")
    public Result<PageResult<FallVO>> list(@Validated FallRecordQueryDTO dto){
        IPage<FallRecord> page = iFallRecordService.pageList(dto);
        PageResult<FallVO> result = PageResult.build(page.getTotal(),
                page.getRecords().stream().map(FallRecordController::toVO).collect(Collectors.toList()));
        return Result.success(result);
    }
    @PostMapping
    public Result<Long> add(@Validated @RequestBody FallRecordAddDTO dto){
        Long id = iFallRecordService.add(dto);
        return Result.success(id);
    }
    @GetMapping("/{id}")
    public Result<FallVO> detail(@PathVariable Long id){
        FallRecord record = iFallRecordService.getById(id);
        if(record == null){
            return Result.fail(404, "跌倒记录不存在");
        }
        return Result.success(toVO(record));
    }

    private static FallVO toVO(FallRecord r) {
        FallVO vo = new FallVO();
        vo.setId(r.getId());
        vo.setElderName(r.getElderName());
        vo.setDate(r.getFallTime());
        vo.setLocation(r.getLocation());
        vo.setCause(r.getCause());
        vo.setSeverity(r.getSeverity());
        vo.setStatus(r.getRecoverStatus());
        vo.setElderId(r.getElderId());
        vo.setHandler(r.getHandler());
        vo.setRemark(r.getTreatment());
        return vo;
    }
    @PutMapping("/{id}")
    public Result<Void> updat(@PathVariable
                              @NotNull(message = "id不能为空")Long id,
                              @Validated @RequestBody  FallRecordUpdateDTO dto){
        if(dto.getId() == null || !id.equals(dto.getId())){
            return Result.fail(400, "路径ID与请求体ID不一致");
        }
        Boolean flag = iFallRecordService.update(dto);
        if(!flag){
            return Result.fail(400, "更新失败");
        }
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable
                                   @NotNull(message = "id不能为空")Long id){
        Boolean flag = iFallRecordService.removeByIdValid(id);
        if(!flag){
            return Result.fail(400, "删除失败");
        }
        return Result.success();
    }

}
