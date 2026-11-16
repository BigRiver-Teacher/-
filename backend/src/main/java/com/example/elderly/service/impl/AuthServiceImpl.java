package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.elderly.common.Result;
import com.example.elderly.common.UserStatusEnum;
import com.example.elderly.dto.*;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.AuthService;
import com.example.elderly.util.JwtUtil;
import com.example.elderly.vo.LoginVO;
import com.example.elderly.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public Result<LoginVO> login(LoginDTO dto) {
        String username = dto.getEffectiveUsername();
        log.info("========== [AuthService] 登录流程开始 ==========");
        log.info("[AuthService] 输入用户名(phone): {}", username);

        // 兜底校验：手机号为空直接返回
        if (username == null || username.isBlank()) {
            log.warn("[AuthService] 手机号为空，登录失败");
            return Result.fail(400, "手机号不能为空");
        }

        // Step 1: 查询用户（按手机号）
        log.info("[AuthService] Step1: 查询用户, phone={}", username);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, username);
        User user;
        try {
            user = userMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.error("[AuthService] 数据库查询异常: {}", e.getMessage(), e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
        log.info("[AuthService] Step1: 查询结果, user={}",
                user == null ? "null" : "id=" + user.getId() + ", phone=" + user.getPhone());

        // Step 2: 检查用户是否存在
        if (user == null) {
            log.warn("[AuthService] Step2: 用户不存在, username={}", username);
            return Result.fail(404, "账号不存在，请检查手机号是否正确");
        }
        log.info("[AuthService] Step2: 用户存在, status={}, role={}", user.getStatus(), user.getRole());

        // Step 3: 检查账号状态
        String activeCode = UserStatusEnum.ACTIVE.getCode();
        log.info("[AuthService] Step3: 检查账号状态, 期望={}, 实际={}", activeCode, user.getStatus());
        if (!activeCode.equals(user.getStatus())) {
            log.warn("[AuthService] Step3: 账号已禁用, userId={}, status={}", user.getId(), user.getStatus());
            return Result.fail(403, "账号已被禁用，请联系管理员");
        }
        log.info("[AuthService] Step3: 状态检查通过");

        // Step 4: 密码验证
        String pwdMd5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        String dbPwd = user.getPassword();
        log.info("[AuthService] Step4: 密码验证, 输入MD5={}, 数据库MD5={}, 匹配={}",
                pwdMd5, dbPwd, pwdMd5.equals(dbPwd));
        if (!pwdMd5.equals(dbPwd)) {
            log.warn("[AuthService] Step4: 密码错误, userId={}", user.getId());
            return Result.fail(400, "密码错误，请重新输入");
        }
        log.info("[AuthService] Step4: 密码验证通过");

        // Step 5: 生成JWT
        log.info("[AuthService] Step5: 生成JWT, userId={}", user.getId());
        String token;
        try {
            token = jwtUtil.generateToken(user.getId().toString());
        } catch (Exception e) {
            log.error("[AuthService] JWT生成失败: {}", e.getMessage(), e);
            return Result.fail(500, "登录失败，令牌生成异常");
        }
        log.info("[AuthService] Step5: JWT生成成功, token前20位={}...",
                token.substring(0, Math.min(20, token.length())));

        // Step 6: 更新最后登录时间
        try {
            user.setLastLogin(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.warn("[AuthService] 更新最后登录时间失败（不影响登录）: {}", e.getMessage());
        }

        // Step 7: 组装返回数据
        UserInfoVO info = new UserInfoVO();
        info.setId(user.getId());
        info.setUsername(user.getPhone());
        info.setRealName(user.getRealName());
        info.setRole(user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(info);

        log.info("========== [AuthService] 登录成功, userId={}, role={} ==========",
                user.getId(), user.getRole());
        return Result.success(vo);
    }

    @Override
    public Result<String> sendResetCode(SendResetCodeDTO dto) {
        log.info("[AuthService] 发送重置验证码, 邮箱: {}", dto.getEmail());
        // TODO: 实际发送邮件逻辑
        log.info("[AuthService] 重置验证码已发送(模拟)");
        return Result.success("验证码已发送至邮箱");
    }

    @Override
    public Result<String> register(RegisterDTO dto) {
        log.info("========== [AuthService] 注册流程开始 ==========");
        log.info("[AuthService] 注册手机号: {}", dto.getPhone());

        // Step 1: 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User exist;
        try {
            exist = userMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.error("[AuthService] 数据库查询异常: {}", e.getMessage(), e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
        log.info("[AuthService] Step1: 查询已有用户, exist={}", exist != null);
        if (exist != null) {
            log.warn("[AuthService] Step1: 手机号已注册, phone={}", dto.getPhone());
            return Result.fail(400, "手机号已注册");
        }

        // Step 2: 创建新用户
        User user = new User();
        user.setAccount(dto.getPhone());
        user.setPhone(dto.getPhone());
        user.setRealName("新用户");
        user.setRole("staff");
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        String pwdMd5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        user.setPassword(pwdMd5);
        log.info("[AuthService] Step2: 创建用户, phone={}, role=staff, status={}",
                dto.getPhone(), UserStatusEnum.ACTIVE.getCode());

        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("[AuthService] 用户插入失败: {}", e.getMessage(), e);
            return Result.fail(500, "注册失败，请稍后重试");
        }
        log.info("========== [AuthService] 注册成功, userId={} ==========", user.getId());
        return Result.success("注册成功");
    }

    @Override
    public Result<String> resetPassword(ResetPasswordDTO dto) {
        log.info("========== [AuthService] 重置密码流程开始 ==========");
        log.info("[AuthService] 重置密码邮箱: {}", dto.getEmail());

        // Step 1: 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        User user;
        try {
            user = userMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.error("[AuthService] 数据库查询异常: {}", e.getMessage(), e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
        log.info("[AuthService] Step1: 查询结果, user={}",
                user == null ? "null" : "id=" + user.getId());
        if (user == null) {
            log.warn("[AuthService] Step1: 用户不存在, email={}", dto.getEmail());
            return Result.fail(404, "该邮箱未注册");
        }

        // Step 2: 更新密码
        String newPwdMd5 = DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes());
        user.setPassword(newPwdMd5);
        try {
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("[AuthService] 密码更新失败: {}", e.getMessage(), e);
            return Result.fail(500, "密码重置失败，请稍后重试");
        }
        log.info("========== [AuthService] 密码重置成功, userId={} ==========", user.getId());
        return Result.success("密码重置成功");
    }

    @Override
    public Result<String> sendRegisterCode(SendRegisterCodeDTO dto) {
        log.info("[AuthService] 发送注册验证码, 手机号: {}", dto.getPhone());
        // TODO: 实际发送短信逻辑
        log.info("[AuthService] 注册验证码已发送(模拟)");
        return Result.success("注册验证码已发送");
    }
}
