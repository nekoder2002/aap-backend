package com.dhu.controller;

import com.dhu.dto.LoginFormDTO;
import com.dhu.dto.RegisterFormDTO;
import com.dhu.dto.UserQueryDTO;
import com.dhu.entity.User;
import com.dhu.service.UserService;
import com.dhu.utils.EmailHelper;
import com.dhu.utils.UserHolder;
import com.dhu.utils.model.Result;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private EmailHelper emailHelper;
    @Autowired
    private UserService userService;

    //登录
    @GetMapping("/login")
    public Result login(LoginFormDTO loginForm) {
        String email = loginForm.getEmail();
        String password = loginForm.getPassword();
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return Result.getErr().setMsg("用户名和密码不能为空");
        }
        return Result.nullFilterData("user", userService.login(loginForm));
    }

    //注册
    @PutMapping("/register")
    public Result register(@RequestBody RegisterFormDTO registerForm) {
        String email = registerForm.getEmail();
        String name = registerForm.getName();
        String password = registerForm.getPassword();
        String captcha = registerForm.getCaptcha();
        if (!StringUtils.hasText(captcha) || !StringUtils.hasText(name) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return Result.saveErr().setMsg("用户名,密码,邮箱和验证码不能为空");
        }
        return Result.verifySave(userService.register(registerForm));
    }

    //获取Info
    @GetMapping("/me")
    public Result getInfo() {
        return Result.nullFilterData("user", UserHolder.getUser());
    }

    //根据id获取用户信息
    @GetMapping("/{userId}")
    public Result getInfoById(@PathVariable Integer userId) {
        return Result.nullFilterData("user", userService.getInfo(userId));
    }

    //更新
    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.updateErr().setMsg("用户id为空");
        }
        //防止更改注册时间
        user.setRegisterTime(null);
        return Result.verifyUpdate(userService.update(user));
    }

    //发送验证码
    @GetMapping("/captcha")
    public Result sendCaptcha(@RequestParam("email") String email) {
        if (!StringUtils.hasText(email)) {
            return Result.getErr().setMsg("邮箱为空");
        }
        if (emailHelper.verifyEmail(email)) {
            return Result.getErr().setMsg("邮箱格式不合法");
        }
        return Result.verifyGet(userService.sendCaptcha(email));
    }

    @PostMapping("/list")
    public Result getList(@RequestBody UserQueryDTO userQueryDTO) {
        return Result.nullFilterData("users", userService.getPage(userQueryDTO.getCurrent(), userQueryDTO.getSize(), userQueryDTO.getName(), userQueryDTO.getSchool(), userQueryDTO.getCollege(), userQueryDTO.getMajor()));
    }

    //批量删除
    @DeleteMapping("/multdel")
    Result deleteUsers(@RequestBody List<Integer> userIds) {
        return Result.verifyDelete(userService.deleteUsers(userIds));
    }

    //删除
    @DeleteMapping("/delete")
    Result deleteUser(@RequestParam("user_id") Integer paperId) {
        return Result.verifyDelete(userService.deleteUser(paperId));
    }

    //统计用户数
    @GetMapping("/count_sys")
    public Result countUser() {
        return Result.nullFilterData("count",userService.countUser());
    }
}
