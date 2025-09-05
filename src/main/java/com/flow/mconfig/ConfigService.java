package com.flow.mconfig;

import com.flow.exception.ErrorCodeEnum;
import com.flow.mconfig.mapper.Config;
import com.flow.mconfig.mapper.ConfigMapper;
import com.flow.common.DataSecurityEthService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 17:37
 */
@Service
public class ConfigService {

    public String _get(String name) {
        Config config = configMapper.get(name);
        if (config == null) return null;
        else return config.getValue();
    }

    public String getWithCache(String name) {
        String config = configService._getWithCache(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        return config;
    }

    public String _getWithCache(String name) {
        Config config = configMapper.get(name);
        if (config == null) return null;
        else return config.getValue();
    }


    public String get(String name) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        return config;
    }

    public String getAndDecrypt(String name) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) ErrorCodeEnum.NOT_OPEN.throwException();
        if (StringUtils.startsWith(config, "{\"iv\"")) config = dataSecurityEthService.decryptWithPrivateKey(config);
        return config;
    }

    public String getAndDecryptOrDefault(String name, String value) {
        String config = configService._get(name);
        if (StringUtils.isEmpty(config)) return value;
        if (StringUtils.startsWith(config, "{")) {
            config = dataSecurityEthService.decryptWithPrivateKey(config);
        }
        return config;
    }


    public String getOrDefault(String name, String value) {
        String config = configService._get(name);
        if (config == null) {
            return value;
        }
        return config;
    }

    public String getOpenApiHmacKey() {
        String config = configService._get("hmac_key");
        if (config == null) {
            return "vUfV1n#JdyG^oKUp";
        }
        return config;
    }

    public String getOpenApiHmacKey2() {
        String config = configService._get("hmac_key2");
        if (config == null) {
            return "vUfV1n#JdyG^oKCb";
        }
        return config;
    }

    public String getDecryptOrDefault(String name, String value) {
        String config = configService._get(name);
        if (config == null) {
            return value;
        }
        if (StringUtils.startsWith(config, "{")) config = dataSecurityEthService.decryptWithPrivateKey(config);
        return config;
    }

    public String getOrDefaultWithCache(String name, String value) {
        String config = configService._get(name);
        if (config == null) {
            return value;
        }
        return config;
    }


    public long insert(Config config) {
        return configMapper.insert(config);
    }

    public boolean insertReal(Config config) {
        return configMapper.insertReal(config) > 0;
    }

    public long replace(String name, String val) {
        return configMapper.updateParam(name, val);
    }

    public boolean cas(String name, String oldValue, String newValue) {
        return configMapper.update(name, oldValue, newValue) > 0L;
    }

    public boolean incrementVal(String name, String incrementVal) {
        return configMapper.incrementVal(name, incrementVal) > 0L;
    }

    @Resource
    private ConfigService configService;
    @Resource
    private ConfigMapper configMapper;
    @Resource
    private DataSecurityEthService dataSecurityEthService;
}
