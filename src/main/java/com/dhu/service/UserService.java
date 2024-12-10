package com.dhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dhu.dto.LoginFormDTO;
import com.dhu.dto.RegisterFormDTO;
import com.dhu.dto.UserDTO;
import com.dhu.entity.User;

import java.util.List;

public interface UserService {
    //登录
    UserDTO login(LoginFormDTO loginForm);

    //注册
    boolean register(RegisterFormDTO registerFormDTO);

    //获取Info
    User getInfo(Integer userId);

    //更新
    boolean update(User user);

    //发送验证码
    boolean sendCaptcha(String email);

    IPage<User> getPage(int current, int size, String username, String school, String college, String major);

    //删除
    boolean deleteUser(Integer userId);

    //批量删除
    boolean deleteUsers(List<Integer> userIds);

    long countUser();
}
