
package com.flow.common.init;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserLoginService {


    public String login(long userId, boolean admin) {
        String loginToken = UUID.randomUUID().toString();
        BoundValueOperations<String, Object> boundValueOps =
                redisTemplate.boundValueOps(LOGIN_TOKEN_CACHE_KEY + loginToken);
        boundValueOps.set(userId + "-" + admin, TOKEN_AGE, TimeUnit.HOURS);
        return loginToken;
    }

    public String getLogin() {
        return this.getLoginUid();
    }

    public void logout() {
        String loginToken = httpServletRequest.getHeader(TOKEN_H);
        if (StringUtils.isEmpty(loginToken)) return;
        redisTemplate.delete(LOGIN_TOKEN_CACHE_KEY + loginToken);
    }


    private String getLoginUid() {
        String token = httpServletRequest.getHeader(TOKEN_H);
        if(StringUtils.isBlank(token) || token.length() > 256){
            return null;
        }
        //aibot专属用户
        if (AIBOT_TOKEN.equals(token)){
            return AIBOT_TOKEN;
        }
        // 查询redis是否存在用户信息
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(LOGIN_TOKEN_CACHE_KEY + token);
        Object cacheId = ops.get();
        if (Objects.isNull(cacheId)) {
            return null;
        }

        // 更新缓存和cookie的过期时间
        ops.expire(TOKEN_AGE, TimeUnit.HOURS);
        // 返回用户id
        return cacheId.toString();
    }

    /**
     * 过期时间
     * 小时
     */
    private static final int TOKEN_AGE = 24;
    private static final String LOGIN_TOKEN_CACHE_KEY = "login_token:";
    private static final String TOKEN_H = "token";
    public static final String AIBOT_TOKEN="aibot-12345678";
    public static final Long AIBOT_UID=1000001L;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private HttpServletResponse httpServletResponse;
}
