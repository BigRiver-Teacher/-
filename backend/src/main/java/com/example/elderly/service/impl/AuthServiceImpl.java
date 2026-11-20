package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthVerificationCodeService verificationCodeService;

    /** 统一认证失败文案，避免账号枚举 */
    private static final String AUTH_FAIL = "账号或密码不正确";

    @Override
    public Result<LoginVO> login(LoginDTO dto) {
        String username = dto.getEffectiveUsername();
        log.info("========== [AuthService] 登录流程开始 ==========");

        if (username == null || username.isBlank() || !StringUtils.hasText(dto.getPassword())) {
            return Result.fail(400, "手机号和密码不能为空");
        }

        User user;
        try {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, username));
        } catch (Exception e) {
            log.error("[AuthService] 数据库查询异常: {}", e.getMessage(), e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
        if (user == null) {
            // 统一错误文案：不区分账号不存在 / 密码错误
            log.warn("[AuthService] 登录失败(用户不存在): {}", username);
            return Result.fail(400, AUTH_FAIL);
        }
        if (!UserStatusEnum.ACTIVE.getCode().equals(user.getStatus())) {
            log.warn("[AuthService] 登录失败(账号禁用), userId={}", user.getId());
            return Result.fail(400, AUTH_FAIL);
        }

        String rawPwd = dto.getPassword();
        boolean matched = isPasswordMatched(rawPwd, user.getPassword());
        if (!matched) {
            log.warn("[AuthService] 登录失败(密码错误), userId={}", user.getId());
            return Result.fail(400, AUTH_FAIL);
        }

        // 旧 MD5 账号在登录成功后自动升级为 BCrypt（无感迁移）
        if (!isBcrypt(user.getPassword())) {
            try {
                user.setPassword(passwordEncoder.encode(rawPwd));
                userMapper.updateById(user);
                log.info("[AuthService] 旧口令已升级为 BCrypt, userId={}", user.getId());
            } catch (Exception e) {
                log.warn("[AuthService] 口令升级失败(不影响登录): {}", e.getMessage());
            }
        }

        String token;
        try {
            token = jwtUtil.generateToken(user.getId().toString(), user.getRole());
        } catch (Exception e) {
            log.error("[AuthService] JWT生成失败: {}", e.getMessage(), e);
            return Result.fail(500, "登录失败，令牌生成异常");
        }

        try {
            user.setLastLogin(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.warn("[AuthService] 更新最后登录时间失败（不影响登录）: {}", e.getMessage());
        }

        UserInfoVO info = new UserInfoVO();
        info.setId(user.getId());
        info.setUsername(user.getPhone());
        info.setRealName(user.getRealName());
        info.setRole(user.getRole());
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(info);
        log.info("========== [AuthService] 登录成功, userId={}, role={} ==========", user.getId(), user.getRole());
        return Result.success(vo);
    }

    /** 兼容 BCrypt 与历史无盐 MD5 */
    private boolean isPasswordMatched(String raw, String stored) {
        if (!StringUtils.hasText(stored)) {
            return false;
        }
        if (isBcrypt(stored)) {
            return passwordEncoder.matches(raw, stored);
        }
        return DigestUtils.md5DigestAsHex(raw.getBytes()).equals(stored);
    }

    private boolean isBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    @Override
    public Result<String> sendResetCode(SendResetCodeDTO dto) {
        if (!StringUtils.hasText(dto.getEmail())) {
            return Result.fail(400, "邮箱不能为空");
        }
        User user;
        try {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        } catch (Exception e) {
            log.error("[AuthService] 查询用户异常: {}", e.getMessage(), e);
            return Result.fail(500, "系统繁忙，请稍后重试");
        }
        // 不区分邮箱是否存在，统一返回成功，避免账号枚举
        if (user != null) {
            boolean sent = verificationCodeService.send("reset", dto.getEmail());
            if (!sent) {
                return Result.fail(429, "发送过于频繁，请稍后再试");
            }
        }
        return Result.success("如邮箱已注册，验证码已发送");
    }

    @Override
    public Result<String> register(RegisterDTO dto) {
        log.info("========== [AuthService] 注册流程开始 ==========");
        if (!StringUtils.hasText(dto.getPhone()) || !StringUtils.hasText(dto.getPassword())) {
            return Result.fail(400, "手机号和密码不能为空");
        }
        // 阶段1：强制校验验证码
        if (!verificationCodeService.verify("register", dto.getPhone(), dto.getVerifyCode())) {
            return Result.fail(400, "验证码错误或已失效");
        }

        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (exist != null) {
            return Result.fail(400, "手机号已注册");
        }

        User user = new User();
        user.setAccount(dto.getPhone());
        user.setPhone(dto.getPhone());
        user.setRealName("新用户");
        // 阶段1：新注册账号为待激活角色，不具备任何权限，需管理员在后台赋权
        user.setRole("pending");
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("[AuthService] 用户插入失败: {}", e.getMessage(), e);
            return Result.fail(500, "注册失败，请稍后重试");
        }
        log.info("========== [AuthService] 注册成功, userId={}, role=pending ==========", user.getId());
        return Result.success("注册成功，请等待管理员激活");
    }

    @Override
    public Result<String> resetPassword(ResetPasswordDTO dto) {
        if (!StringUtils.hasText(dto.getEmail()) || !StringUtils.hasText(dto.getNewPassword())) {
            return Result.fail(400, "邮箱和新密码不能为空");
        }
        if (!verificationCodeService.verify("reset", dto.getEmail(), dto.getVerifyCode())) {
            return Result.fail(400, "验证码错误或已失效");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            return Result.fail(400, AUTH_FAIL);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
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
        if (!StringUtils.hasText(dto.getPhone())) {
            return Result.fail(400, "手机号不能为空");
        }
        User exist = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (exist != null) {
            return Result.fail(400, "手机号已注册");
        }
        boolean sent = verificationCodeService.send("register", dto.getPhone());
        if (!sent) {
            return Result.fail(429, "发送过于频繁，请稍后再试");
        }
        return Result.success("验证码已发送");
    }
}
