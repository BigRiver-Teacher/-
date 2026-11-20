package com.example.elderly.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解（阶段1）。
 *
 * <p>标注在 Controller 类或方法上，声明允许访问的角色。
 * 校验由 RoleAuthInterceptor 完成，角色来源为数据库中当前登录用户的真实角色，
 * 不信任 JWT 中的 role claim，避免伪造 claim 越权。</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许的角色编码，如 admin / staff / supervisor */
    String[] value();
}
