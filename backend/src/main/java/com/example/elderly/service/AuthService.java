package com.example.elderly.service;

import com.example.elderly.common.Result;
import com.example.elderly.dto.*;
import com.example.elderly.vo.LoginVO;

public interface AuthService {
    Result<LoginVO> login(LoginDTO dto);
    Result<String> sendResetCode(SendResetCodeDTO dto);
    Result<String> register(RegisterDTO dto);
    Result<String> resetPassword(ResetPasswordDTO dto);
    Result<String>sendRegisterCode(SendRegisterCodeDTO dto);
}
