package com.example.elderly.util;

import com.example.elderly.entity.User;
import com.example.elderly.mapper.ElderCaregiverMapper;
import com.example.elderly.mapper.ElderExpertMapper;
import com.example.elderly.mapper.ElderFamilyMapper;
import com.example.elderly.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 数据范围过滤工具：根据当前用户角色与服务关系，返回其可访问的老人ID集合。
 * <ul>
 *   <li>返回 null 表示不限制（admin/staff 管理员端全量可见）；</li>
 *   <li>返回集合（可能为空）表示仅能访问集合内的老人。</li>
 * </ul>
 */
@Component
public class DataScopeUtil {

    @Resource
    private UserMapper userMapper;
    @Resource
    private ElderCaregiverMapper elderCaregiverMapper;
    @Resource
    private ElderExpertMapper elderExpertMapper;
    @Resource
    private ElderFamilyMapper elderFamilyMapper;

    /**
     * 获取指定用户可访问的老人ID集合。
     *
     * @param userId 登录用户ID（user.id）
     * @return null=全部可见；否则为可访问的 elder_id 集合（可能为空集合）
     */
    public List<Long> getAccessibleElderIds(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        String role = user.getRole();
        // 管理员与员工：不限制
        if (role == null || "admin".equals(role) || "staff".equals(role)) {
            return null;
        }
        switch (role) {
            case "expert":
                return elderExpertMapper.selectElderIdsByExpertUserId(userId);
            case "caregiver":
            case "supervisor":
                return elderCaregiverMapper.selectElderIdsByCaregiverUserId(userId);
            case "elder":
                return elderFamilyMapper.selectElderIdsByFamilyUserId(userId);
            default:
                return Collections.emptyList();
        }
    }
}
