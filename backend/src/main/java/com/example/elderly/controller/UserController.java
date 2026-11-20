package com.example.elderly.controller;

import com.example.elderly.annotation.RequireRole;
import com.example.elderly.common.Result;
import com.example.elderly.dto.user.UserQueryDTO;
import com.example.elderly.dto.user.UserSaveDTO;
import com.example.elderly.service.UserService;
import com.example.elderly.vo.PageVO;
import com.example.elderly.vo.UserInfoVO;
import com.example.elderly.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/profile")
    public Result<UserInfoVO> getProfile(){
        return userService.getProfile();
    }
    @GetMapping("/permissions")
    public Result<String[]> getPermissions(){
        return userService.getPermission();
    }
    @RequireRole({"admin", "staff"})
    @GetMapping("/list")
    public Result<PageVO<UserVO>> list(UserQueryDTO query){
        return userService.list(query);
    }
    @RequireRole({"admin", "staff"})
    @PostMapping
    public Result<Long> add(@Valid @RequestBody UserSaveDTO dto){

        return userService.add(dto);
    }
    @RequireRole({"admin", "staff"})
    @PutMapping("/{id}")
    public Result<String> edit(@PathVariable Long id,
                             @Valid @RequestBody UserSaveDTO dto){
       return userService.edit(id,dto);
    }
    @RequireRole({"admin", "staff"})
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        return userService.delete(id);
    }
    @RequireRole({"admin", "staff"})
    @PutMapping("/{id}/toggle-status")
    public Result<String> toggleStatus(@PathVariable Long id){

        return    userService.toggleStatus(id);
    }
    @RequireRole({"admin", "staff"})
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPwd(@PathVariable Long id){

        return  userService.resetPassword(id);
    }

}
