package com.flow.common.ip;

import cn.hutool.http.HttpUtil;
import com.flow.tool.Constants;
import com.flow.tool.JsonObjectTool;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class IpToolService {
    public String getIp() {
        return this.getIp(httpServletRequest);
    }

    public String getIp(HttpServletRequest request) {//获得客户端的IP,如果有更好的方法可以直接代替
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }

        return ipAddress;
    }


    /**
     * {
     * "ipAddress": "47.243.126.147",
     * "continent": "亚洲",
     * "enContinent": "Asia",
     * "autonomousSystemOrganization": "Alibaba US Technology Co., Ltd.",
     * "countryName": "香港",
     * "enCountryName": "Hong Kong",
     * "countryCode": "HK",
     * "provinceName": null,
     * "enProvinceName": "Central and Western District",
     * "provinceCode": "HCW",
     * "cityName": "香港",
     * "enCityName": "Hong Kong",
     * "postalCode": null,
     * "longitude": 114.1759,
     * "latitude": 22.2842
     * }
     */
    public IpAnalysisInfo ipAnalysis(String ip) {
        if (StringUtils.isBlank(ip)) return new IpAnalysisInfo();
        BoundValueOperations<String, Object> boundValueOps = redisTemplate.boundValueOps("ip_cache:".concat(ip));
        Object ipCacheData = boundValueOps.get();
        if (Objects.nonNull(ipCacheData)) {
            IpAnalysisInfo result = Constants.GSON.fromJson(ipCacheData.toString(), IpAnalysisInfo.class);
            if (Objects.nonNull(result)) return result;
        }
        String res = HttpUtil.get("http://101.47.131.219/api/ip/" + ip, 30000);
        JsonObject result = null;
        try {
            result = new Gson().fromJson(res, JsonObject.class);
        } catch (JsonSyntaxException e) {
            log.warn("IPUtils#ipAnalysis error, res:{}", res);
        }
        if (Objects.isNull(result)) {
            return null;
        }
        String country = JsonObjectTool.getAsString(result, "enCountryName");
        String region = JsonObjectTool.getAsString(result, "enProvinceName");
        String city = JsonObjectTool.getAsString(result, "enCityName");
        String latitude = JsonObjectTool.getAsString(result, "latitude");
        String longitude = JsonObjectTool.getAsString(result, "longitude");
        String organization = JsonObjectTool.getAsString(result, "autonomousSystemOrganization");
        String timezone = JsonObjectTool.getAsString(result, "timeZone");
        String continent_code = JsonObjectTool.getAsString(result, "countryCode");

        IpAnalysisInfo analysisInfo = IpAnalysisInfo.builder()
                .continent_code(StringUtils.isBlank(continent_code) ? "unknown" : continent_code)
                .country(StringUtils.isBlank(country) ? "unknown" : country)
                .region(StringUtils.isBlank(region) ? "unknown" : region)
                .city(StringUtils.isBlank(city) ? "unknown" : city)
                .longitude(StringUtils.isBlank(longitude) ? "unknown" : longitude)
                .latitude(StringUtils.isBlank(latitude) ? "unknown" : latitude)
                .organization(StringUtils.isBlank(organization) ? "unknown" : organization)
                .timezone(StringUtils.isBlank(timezone) ? "unknown" : timezone)
                .build();

        boundValueOps.set(Constants.GSON.toJson(analysisInfo), 60, TimeUnit.MINUTES);
        return analysisInfo;
    }

    @Resource
    private HttpServletRequest httpServletRequest;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
}
