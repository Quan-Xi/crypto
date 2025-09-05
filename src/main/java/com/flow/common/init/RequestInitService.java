package com.flow.common.init;

import com.flow.exception.ErrorCodeEnum;
import com.flow.common.ApplicationContextTool;
import com.flow.common.ip.IpToolService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;


@Service
public class RequestInitService {

    @Resource
    private UserLoginService userLoginService;

    public LocalDateTime now() {
        return get().getNow();
    }

    public Long _uid() {
        return get().getUid();
    }

    public long uid() {
        Long uid = _uid();
        if (uid == null) {
            ErrorCodeEnum.PLEASE_LOGIN.throwException();
        }
        return uid;
    }

    public boolean admin() {
        RequestInit requestInit = get();
        return requestInit.isAdmin();
    }

    public String requestId() {
        return get().getRequestId();
    }

    public String ip() {
        return get().getIp();
    }

    public void init(RequestInit requestInit) {
        REQUEST_INIT.set(requestInit);
    }

    public RequestInit get() {
        RequestInit requestInit = REQUEST_INIT.get();
        if (requestInit == null) return new RequestInit();
        return requestInit;
    }

    public void init(HttpServletRequest httpServletRequest) {
        RequestInit requestInit = new RequestInit();
        String loginId = userLoginService.getLogin();
        String ip = Objects.requireNonNull(ApplicationContextTool.getBean(IpToolService.class)).getIp();
        if (StringUtils.isNotBlank(loginId)) {
            if (UserLoginService.AIBOT_TOKEN.equals(loginId) && (ip.contains("0:0:0:0:0:0:0:1")||ip.contains("13.214.212.7") || ip.contains("199.254.199.108")) ){
                requestInit.setUid(UserLoginService.AIBOT_UID);
                requestInit.setAdmin(false);
            } else {
                String[] split = loginId.split("-");
                requestInit.setUid(Long.parseLong(split[0]));
                requestInit.setAdmin(Boolean.parseBoolean(split[1]));
            }

        }
        requestInit.setIp(ip);
        REQUEST_INIT.set(requestInit);
    }

    public void destroy() {
        REQUEST_INIT.remove();
    }

    public void init() {
        RequestInit requestInit = new RequestInit();
        REQUEST_INIT.set(requestInit);
    }

    public void setUid(long uid) {
        RequestInit requestInit = get();
        requestInit.setUid(uid);
    }

    public void setReqId(String reqId) {
        RequestInit requestInit = get();
        requestInit.setRequestId(reqId);
    }

    private final ThreadLocal<RequestInit> REQUEST_INIT = new ThreadLocal<>();
}
