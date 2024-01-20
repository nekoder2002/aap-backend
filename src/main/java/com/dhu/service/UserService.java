package com.dhu.service;

import com.dhu.dto.LoginFormDTO;
import com.dhu.dto.RegisterFormDTO;
import com.dhu.dto.UserDTO;
import com.dhu.entity.User;

public interface UserService {
    //登录
    UserDTO login(LoginFormDTO loginForm);

    //注册
    boolean register(RegisterFormDTO registerFormDTO);

    //获取Info
    User getInfo(Integer userId);

    //删除当前用户登录状态
    boolean deleteUserLoginStatus(String token);

    //更新
    boolean update(User user);

    //发送验证码
    boolean sendCaptcha(String email);
}
