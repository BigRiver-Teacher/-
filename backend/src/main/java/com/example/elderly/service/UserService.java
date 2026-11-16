package com.example.elderly.service;


import com.example.elderly.common.Result;
import com.example.elderly.dto.user.UserQueryDTO;
import com.example.elderly.dto.user.UserSaveDTO;
import com.example.elderly.vo.PageVO;
import com.example.elderly.vo.UserInfoVO;
import com.example.elderly.vo.UserVO;


public interface UserService {
    Result<UserInfoVO> getProfile();
    Result<String[]>getPermission();
    Result<PageVO<UserVO>> list(UserQueryDTO dto);
    Result<Long> add(UserSaveDTO dto);
    Result<String> edit(Long id,UserSaveDTO dto);
    Result<String> delete(Long id);
    Result<String> toggleStatus(Long id);
    Result<String> resetPassword(Long id);
}
