package com.dhu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dhu.constants.BaseConstants;
import com.dhu.constants.RedisConstants;
import com.dhu.dao.UserDao;
import com.dhu.dto.LoginFormDTO;
import com.dhu.dto.RegisterFormDTO;
import com.dhu.dto.UserDTO;
import com.dhu.entity.User;
import com.dhu.exception.NotMatchException;
import com.dhu.service.UserService;
import com.dhu.utils.EmailHelper;
import com.dhu.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private EmailHelper emailHelper;
    @Autowired
    private UserDao userDao;

    @Override
    public UserDTO login(LoginFormDTO loginForm) {
        //查询用户账号
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, loginForm.getEmail()).eq(User::getPassword, loginForm.getPassword());
        User user = userDao.selectOne(wrapper);
        if (user == null) {
            return null;
        }
        //封装DTO
        UserDTO dto = new UserDTO();
        BeanUtil.copyProperties(user, dto);
        //userDTO转为map
        Map<String, Object> dtoMap = BeanUtil.beanToMap(dto, new HashMap<>(), CopyOptions.create().setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        //生成token，作为登录令牌
        String token = UUID.randomUUID().toString();
        //存入redis
        stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, dtoMap);
        //设置有效期
        stringRedisTemplate.expire(token, Duration.ofMinutes(RedisConstants.LOGIN_USER_MIN_TTL));
        dto.setToken(token);
        return dto;
    }

    @Override
    public boolean register(RegisterFormDTO registerFormDTO) {
        // 查询用户是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(User::getId, User::getEmail, User::getName).eq(User::getEmail, registerFormDTO.getEmail()).eq(User::getPassword, registerFormDTO.getPassword());
        if (userDao.selectOne(wrapper) != null) {
            return false;
        }
        // 检查验证码是否正确
        String key = RedisConstants.REGISTER_CAPTCHA + registerFormDTO.getEmail();
        String checkCap = stringRedisTemplate.opsForValue().get(key);
        // 不匹配
        if (!registerFormDTO.getCaptcha().equals(checkCap)) {
            throw new NotMatchException("验证码不正确");
        }
        //清除缓存
        stringRedisTemplate.delete(key);
        // 存入数据库
        User user = new User();
        BeanUtil.copyProperties(registerFormDTO, user);
        user.setRegisterTime(LocalDateTime.now());
        return userDao.insert(user) > 0;
    }

    @Override
    public User getInfo(Integer userId) {
        return userDao.selectById(userId);
    }

    @Override
    public boolean deleteUserLoginStatus(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + UserHolder.getUser().getToken()));
    }

    @Override
    public boolean update(User user) {
        return userDao.updateById(user) > 0;
    }

    @Override
    public boolean sendCaptcha(String email) {
        //生成验证码
        String captcha = emailHelper.generateCaptcha(BaseConstants.CAPTCHA_LENGTH);
        // 发送信息
        String text = "您的验证码为:<br><h1>" + captcha + "</h1><br>发送时间：" + LocalDateTime.now() + " ," + RedisConstants.REGISTER_CAPTCHA_MIN_TTL + "分钟内有效";
        emailHelper.sendMessage(email, "<" + BaseConstants.PROJECT_NAME + "> 注册验证邮件", text);
        //将验证码存入redis
        String key = RedisConstants.REGISTER_CAPTCHA + email;
        stringRedisTemplate.opsForValue().set(key, captcha);
        stringRedisTemplate.expire(key, Duration.ofMinutes(RedisConstants.REGISTER_CAPTCHA_MIN_TTL));
        return true;
    }
}
