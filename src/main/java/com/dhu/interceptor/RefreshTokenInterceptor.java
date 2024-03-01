package com.dhu.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.dhu.constants.RedisConstants;
import com.dhu.dto.UserDTO;
import com.dhu.exception.NotLoginException;
import com.dhu.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate redisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取token
        String token = request.getHeader("authorization");
        if (!StringUtils.hasText(token)) {
            return true;
        }
        // 获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(key);
        if(userMap.isEmpty()){
            throw new NotLoginException("登录过期，请重新登录");
        }
        // 将结果存入userDTO
        UserDTO dto = BeanUtil.mapToBean(userMap, UserDTO.class, true, null);
        //保存用户
        UserHolder.saveUser(dto);
        //刷新token有效期
        redisTemplate.expire(key, Duration.ofMinutes(RedisConstants.LOGIN_USER_MIN_TTL));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
