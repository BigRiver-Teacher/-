package com.example.elderly.config;

import com.example.elderly.annotation.RequireRole;
import com.example.elderly.common.exception.BusinessException;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.util.LoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * 角色校验拦截器（阶段1）。
 *
 * <p>读取 Controller 类/方法上的 {@link RequireRole}，按数据库中当前用户的真实角色判断，
 * 不使用 JWT claim；无注解的接口不干预（沿用数据范围隔离）。</p>
 */
@Component
@RequiredArgsConstructor
public class RoleAuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        // 方法级注解优先，其次类级注解
        RequireRole ann = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), RequireRole.class);
        if (ann == null) {
            ann = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), RequireRole.class);
        }
        if (ann == null) {
            return true;
        }

        Long userId = LoginUserUtil.getUserId();
        User user = userMapper.selectById(userId);
        String role = user == null ? "" : user.getRole();
        boolean allowed = Arrays.stream(ann.value()).anyMatch(r -> r.equals(role));
        if (!allowed) {
            throw new BusinessException(403, "权限不足，请联系管理员");
        }
        return true;
    }
}
