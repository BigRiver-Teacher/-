package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderly.common.Result;
import com.example.elderly.common.UserStatusEnum;
import com.example.elderly.dto.user.UserQueryDTO;
import com.example.elderly.dto.user.UserSaveDTO;
import com.example.elderly.entity.User;
import com.example.elderly.mapper.UserMapper;
import com.example.elderly.service.UserService;
import com.example.elderly.util.LoginUserUtil;
import com.example.elderly.vo.PageVO;
import com.example.elderly.vo.UserInfoVO;
import com.example.elderly.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public Result<UserInfoVO> getProfile() {
        Long userId;
        try {
            userId = LoginUserUtil.getUserId();
        } catch (RuntimeException e) {
            return Result.fail(401, "用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return Result.success(vo);
    }

    @Override
    public Result<String[]> getPermission() {
        String[] perms={"system:user:list","system:user:add","system:user:edit"};
        return Result.success(perms);
    }

    @Override
    public Result<PageVO<UserVO>> list(UserQueryDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dto.getAccount()),User::getAccount,dto.getAccount());
        wrapper.eq(StringUtils.hasText(dto.getRole()),User::getRole,dto.getRole());
        wrapper.eq(StringUtils.hasText(dto.getCommunity()),User::getCommunity,dto.getCommunity());
        wrapper.eq(StringUtils.hasText(dto.getStatus()),User::getStatus,dto.getStatus());
        Page<User> page = new Page<>(dto.getPage(), dto.getSize());
        Page<User> pageData = userMapper.selectPage(page, wrapper);
        PageVO<UserVO> vo = new PageVO<>();
        vo.setTotal(pageData.getTotal());
        vo.setList(pageData.getRecords().stream().map(u->{
            UserVO vo1 = new UserVO();
            vo1.setId(u.getId());
            vo1.setAccount(u.getAccount());
            vo1.setName(u.getRealName());
            vo1.setRole(u.getRole());
            vo1.setCommunity(u.getCommunity());
            vo1.setStatus(u.getStatus());
            vo1.setLastLogin(u.getLastLogin());
            return vo1;
        }).collect(Collectors.toList()));
        return Result.success(vo);
    }

    @Override
    public Result<Long> add(UserSaveDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount,dto.getAccount());
        User exist = userMapper.selectOne(wrapper);
        if(exist!=null){
            return Result.fail(400,"用户已存在");
        }
        User user = new User();
        user.setAccount(dto.getAccount());
        user.setRealName(dto.getName());
        user.setRole(dto.getRole());
        user.setCommunity(dto.getCommunity());
        user.setStatus(UserStatusEnum.ACTIVE.getCode());
        if(dto.getPassword()!=null){
            user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        }
        userMapper.insert(user);
        return Result.success(user.getId());
    }

    @Override
    public Result<String> edit(Long id, UserSaveDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        user.setRealName(dto.getName());
        user.setRole(dto.getRole());
        user.setCommunity(dto.getCommunity());
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        return Result.success("编辑成功" );
    }

    @Override
    public Result<String> delete(Long id) {
        User user = userMapper.selectById(id);
        if(user==null){
            return Result.fail(404,"用户不存在");
        }
        userMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<String> toggleStatus(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        String newStatus = UserStatusEnum.ACTIVE.getCode().equals(user.getStatus())
                ? UserStatusEnum.INACTIVE.getCode()
                : UserStatusEnum.ACTIVE.getCode();
        user.setStatus(newStatus);
        userMapper.updateById(user);
        return Result.success("状态切换成功");
    }

    @Override
    public Result<String> resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        String initPassword = DigestUtils.md5DigestAsHex("123456".getBytes());
        user.setPassword(initPassword);
        userMapper.updateById(user);
        return Result.success("密码重置成功，新密码为：123456");
    }
}
